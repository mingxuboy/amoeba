/**
 * <pre>
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * </pre>
 */
package com.meidusa.amoeba.mysql.net;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.filter.IOFilter;
import com.meidusa.amoeba.mysql.filter.PacketFilterInvocation;
import com.meidusa.amoeba.mysql.filter.PacketIOFilter;
import com.meidusa.amoeba.mysql.handler.PreparedStatmentInfo;
import com.meidusa.amoeba.net.AuthingableConnectionManager;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.util.ThreadLocalMap;

/**
 * 负责连接到 proxy server的客户端连接对象包装
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class MysqlClientConnection extends MysqlConnection {

	private static Logger logger = Logger
			.getLogger(MysqlClientConnection.class);

	static List<IOFilter> filterList = new ArrayList<IOFilter>();
	static {
		filterList.add(new PacketIOFilter());
	}

	// 保存服务端发送的随机用于客户端加密的字符串
	protected String seed;

	// 保存客户端返回的加密过的字符串
	protected byte[] authenticationMessage;

	private List<byte[]> longDataList = new ArrayList<byte[]>();

	private List<byte[]> unmodifiableLongDataList = Collections
			.unmodifiableList(new ArrayList<byte[]>());

	/** 存储sql,statmentId对 */
	private final Map<String, Long> SQL_STATMENT_ID_MAP = Collections
			.synchronizedMap(new HashMap<String, Long>(256));
	private AtomicLong atomicLong = new AtomicLong(1);

	/**
	 * 采用LRU缓存这些preparedStatment信息 key=statmentId value=PreparedStatmentInfo
	 * object
	 */
	@SuppressWarnings("unchecked")
	private final Map<Long, PreparedStatmentInfo> PREPARED_STATMENT_MAP = Collections
			.synchronizedMap(new LRUMap(256) {

				private static final long serialVersionUID = 1L;

				protected boolean removeLRU(LinkEntry entry) {
					PreparedStatmentInfo info = (PreparedStatmentInfo) entry
							.getValue();
					SQL_STATMENT_ID_MAP.remove(info.getPreparedStatment());
					return true;
				}

				public PreparedStatmentInfo remove(Object key) {
					PreparedStatmentInfo info = (PreparedStatmentInfo) super
							.remove(key);
					SQL_STATMENT_ID_MAP.remove(info.getPreparedStatment());
					return info;
				}

				public Object put(Object key, Object value) {
					PreparedStatmentInfo info = (PreparedStatmentInfo) value;
					SQL_STATMENT_ID_MAP.put(info.getPreparedStatment(),
							(Long) key);
					return super.put(key, value);
				}

				public void putAll(Map map) {
					for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
						Map.Entry<Long, PreparedStatmentInfo> entry = (Map.Entry<Long, PreparedStatmentInfo>) it
								.next();
						SQL_STATMENT_ID_MAP.put(entry.getValue()
								.getPreparedStatment(), entry.getKey());
					}
					super.putAll(map);
				}
			});

	public MysqlClientConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}

	public PreparedStatmentInfo getPreparedStatmentInfo(long id) {
		return PREPARED_STATMENT_MAP.get(id);
	}

	public PreparedStatmentInfo getPreparedStatmentInfo(String preparedSql) {
		Long id = SQL_STATMENT_ID_MAP.get(preparedSql);
		PreparedStatmentInfo info = null;
		if (id == null) {
			info = new PreparedStatmentInfo(this, atomicLong.getAndIncrement(),
					preparedSql);
			PREPARED_STATMENT_MAP.put(info.getStatmentId(), info);
		} else {
			info = getPreparedStatmentInfo(id);
		}
		return info;
	}

	public String getSeed() {
		return seed;
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}

	public byte[] getAuthenticationMessage() {
		return authenticationMessage;
	}

	public void handleMessage(Connection conn, byte[] message) {
		// 在未验证通过的时候
		/** 此时接收到的应该是认证数据，保存数据为认证提供数据 */
		this.authenticationMessage = message;
		((AuthingableConnectionManager) _cmgr).getAuthenticator()
				.authenticateConnection(this);
	}

	protected void messageProcess(final byte[] msg) {
		final PacketFilterInvocation invocation = new PacketFilterInvocation(
				filterList, this, msg) {

			@Override
			protected Result doProcess() {
				ProxyRuntimeContext.getInstance().getClientSideExecutor()
						.execute(new Runnable() {

							public void run() {
								try {
									MysqlClientConnection.this
											.getMessageHandler().handleMessage(
													MysqlClientConnection.this,
													msg);
								} finally {
									ThreadLocalMap.reset();
								}
							}
						});
				return null;
			}
		};
		invocation.invoke();
	}

	public void addLongData(byte[] longData) {
		longDataList.add(longData);
	}

	public void clearLongData() {
		longDataList.clear();
	}

	public List<byte[]> getLongDataList() {
		return unmodifiableLongDataList;
	}

	/**
	 * 正在处于验证的Connection Idle时间可以设置相应的少一点。
	 */
	public boolean checkIdle(long now) {
		if (isAuthenticated()) {
			return false;
		} else {
			long idleMillis = now - _lastEvent;
			if (idleMillis < 5000) {
				return false;
			}
			if (isClosed()) {
				return true;
			}

			logger.warn("Disconnecting non-communicative client [conn=" + this
					+ ", idle=" + idleMillis + "ms].");
			return true;
		}
	}
}
