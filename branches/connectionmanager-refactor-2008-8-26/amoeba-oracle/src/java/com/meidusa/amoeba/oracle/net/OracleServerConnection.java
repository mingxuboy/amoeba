package com.meidusa.amoeba.oracle.net;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;
import com.meidusa.amoeba.oracle.net.packet.AcceptPacket;
import com.meidusa.amoeba.oracle.net.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.net.packet.Packet;
import com.meidusa.amoeba.oracle.net.packet.RedirectPacket;
import com.meidusa.amoeba.oracle.net.packet.RefusePacket;
import com.meidusa.amoeba.oracle.net.packet.SQLnetDef;
import com.meidusa.amoeba.oracle.net.packet.T4C7OversionDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C7OversionResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8OcloseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIdtyDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIdtyResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIproDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8TTIproResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIMsgPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIfunPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthKeyDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthKeyResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIoAuthResponseDataPacket;
import com.meidusa.amoeba.oracle.util.ByteUtil;

/**
 * 
 * @author struct
 *
 */
public class OracleServerConnection extends OracleConnection implements
		PoolableObject, SQLnetDef {
	private static Logger    logger         = Logger.getLogger(OracleServerConnection.class);
	private boolean active;
	private ObjectPool objectPool;
	private int responsedMsgCount;
	private Packet lastPacketRequest;
	public OracleServerConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}

	@Override
	protected void init() {
		ConnectPacket packet = genConnectPacket();
		this.postMessage(packet.toByteBuffer(this));
		lastPacketRequest = packet;
	}

	public void handleMessage(Connection conn, byte[] buffer) {
		OracleServerConnection serverConn = (OracleServerConnection) conn;
		Packet packet = null;
		Packet response = null;
		try{
			switch (buffer[4]) {
				case SQLnetDef.NS_PACKT_TYPE_ACCEPT: // '\002'//2
					AcceptPacket acceptpacket = new AcceptPacket();
					acceptpacket.init(buffer, conn);
		            packet = new T4C8TTIproDataPacket();
					break;
				case NS_PACKT_TYPE_RESEND: // '\013'//11
					packet = genConnectPacket();
					break;
				case NS_PACKT_TYPE_REDIRECT: // '\005'//5
					RedirectPacket redirectpacket = new RedirectPacket();
					establishConnection(redirectpacket.getData());
					break;
				case NS_PACKT_TYPE_REFUTE: // '\004'//4
					RefusePacket refusepacket = new RefusePacket();
					refusepacket.init(buffer, conn);
					this.setAuthenticated(false);
					this.postClose(null);
					break;
				case 3: // '\003'
				case NS_PACKT_TYPE_DATA: // '\006'
				{
					if (lastPacketRequest instanceof T4C8TTIproDataPacket) {
						T4C8TTIproResponseDataPacket pro = new T4C8TTIproResponseDataPacket();
						response = pro; 
						response.init(buffer, conn);
						setConnectionField(serverConn,pro);
						packet = new T4C8TTIdtyDataPacket();
		            } else if (lastPacketRequest instanceof T4C8TTIdtyDataPacket) {
		            	T4C8TTIdtyResponseDataPacket idty = new T4C8TTIdtyResponseDataPacket();
		            	response = idty;
		            	response.init(buffer, conn);
		            	serverConn.setBasicTypes();
		            	packet = new T4C7OversionDataPacket();
		            } else if (lastPacketRequest instanceof T4C7OversionDataPacket) {
		            	T4C7OversionResponseDataPacket version = new T4C7OversionResponseDataPacket();
		            	response = version;
		            	response.init(buffer, conn);
		            	T4CTTIoAuthKeyDataPacket authekey = new T4CTTIoAuthKeyDataPacket();
		            	packet = authekey;
		            	authekey.user = this.getUser();
		            	authekey.pid = "1223";
		            	authekey.program_nm = "amoeba proxy";
		            	authekey.sid = this.getSchema();
		            	authekey.terminal = "unkonw";
		            	authekey.machine = "unknow";
		            } else if (lastPacketRequest instanceof T4CTTIoAuthKeyDataPacket) {
		            	T4CTTIoAuthKeyResponseDataPacket  authkey = new T4CTTIoAuthKeyResponseDataPacket();
		            	response = authkey;
		            	response.init(buffer, conn);
		            	T4CTTIoAuthDataPacket auth = new T4CTTIoAuthDataPacket();
		            	packet = auth;
		            	this.encryptedSK = authkey.encryptedSK;
		            	auth.encryptedSK = authkey.encryptedSK;
		            	auth.userStr = this.getUser();
		            	auth.passwordStr = this.getPassword();
		            } else if (lastPacketRequest instanceof T4CTTIoAuthDataPacket) {
		            	T4CTTIoAuthResponseDataPacket auth = new T4CTTIoAuthResponseDataPacket();
		            	response = auth;
		            	response.init(buffer, conn);
		            	if(auth.oer.retCode == 0){
		            		this.setAuthenticated(true);
		            	}
		            } else if (T4CTTIfunPacket.isFunType(buffer, T4CTTIMsgPacket.TTIPFN, T4CTTIfunPacket.OCCA)) {
		                Packet packet1 = new T4C8OcloseDataPacket();
		                packet1.init(buffer, conn);
		                if (logger.isDebugEnabled()) {
		                    System.out.println("packet1:" + packet1);
		                }
		            }
					break;
				}
				case 7: // '\007'
				case 8: // '\b'
				case 9: // '\t'
				case 10: // '\n'
				default:{
					this.postClose(null);
					return;
				}
			}
			
			if(packet != null){
				this.postMessage(packet.toByteBuffer(conn));
			}
			responsedMsgCount++;
		}finally{
            if (logger.isDebugEnabled()) {
            	if(response != null){
    				byte[] message = response.toByteBuffer(conn).array();
    				System.out.println("@response source:" + ByteUtil.toHex(message, 0, message.length));
    				System.out.println("@response packet:" + response);
            	}
            	
            	if(packet != null){
    				byte[] message = packet.toByteBuffer(conn).array();
    				System.out.println("@request source:" + ByteUtil.toHex(message, 0, message.length));
    				System.out.println("@request packet:" + packet);
            	}
            }
            
			lastPacketRequest = packet;
		}
	}

	private void establishConnection(String data) {

	}

	public ObjectPool getObjectPool() {
		return objectPool;
	}

	public boolean isActive() {
		return active;
	}

	public synchronized void setObjectPool(ObjectPool pool) {
		this.objectPool = pool;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isRemovedFromPool() {
		return objectPool == null;
	}

	protected void close(Exception exception) {
		super.close(exception);
		final ObjectPool tmpPool = objectPool;
		objectPool = null;
		try {
			if (tmpPool != null) {

				/**
				 * 处于active 状态的 poolableObject，可以用ObjectPool.invalidateObject
				 * 方式从pool中销毁 否则只能等待被borrow 或者 idle time out
				 */
				if (isActive()) {
					tmpPool.invalidateObject(this);
				}
			}
		} catch (Exception e) {

		}
	}
	
	public ConnectPacket genConnectPacket(){
		ConnectPacket packet = new ConnectPacket();
		StringBuilder builder = new StringBuilder(); 
		builder.append("(DESCRIPTION=(CONNECT_DATA=(SID=");
		builder.append(this.getSchema());
		builder.append(")");
		builder.append("(CID=(PROGRAM=)(HOST=__jdbc__)(USER=)))(ADDRESS=(PROTOCOL=tcp)(HOST=");
		builder.append(this.getChannel().socket().getInetAddress().getHostAddress());
		builder.append(")(PORT=");
		builder.append(this.getChannel().socket().getPort());
		builder.append(")))");
		  //(DESCRIPTION=(CONNECT_DATA=(SID=test)(CID=(PROGRAM=)(HOST=__jdbc__)(USER=)))(ADDRESS=(PROTOCOL=tcp)(HOST=10.0.65.204)(PORT=1521)))
		//(DESCRIPTION=(CONNECT_DATA=(SID=ocntest)(CID=(PROGRAM=)(HOST=__jdbc__)(USER=)))(ADDRESS=(PROTOCOL=tcp)(HOST=127.0.0.1)(PORT=8066)))
		packet.data = builder.toString();
		return packet;
	}

}
