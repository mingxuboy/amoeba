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
package com.meidusa.amoeba.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.runtime.Shutdowner;

/**
 * 指定一个端口,创建一个serverSocket. 将该ServerSocket所创建的Connection加入管理
 * 该manager只负责socket accept netEvent,socket的 IO netEvent 由 {@link ServerableConnectionManager#connMgr} 负责
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class ServerableConnectionManager extends AuthingableConnectionManager implements Shutdowner {

    protected static Logger       log = Logger.getLogger(ServerableConnectionManager.class);
    
    protected int                 port;
    protected ServerSocketChannel ssocket;
    protected String              ipAddress;
    protected ConnectionFactory   connFactory;
    private ConnectionManager manager;

    public ConnectionManager getManager() {
		return manager;
	}

	public void setManager(ConnectionManager manager) {
		this.manager = manager;
	}

	public ServerableConnectionManager() throws IOException{
    	this.setDaemon(false);
    }

    public void setConnectionFactory(ConnectionFactory connFactory) {
        this.connFactory = connFactory;
    }

    protected void initServerSocket(){
        try {
            // create a listening socket and add it to the select set
            ssocket = ServerSocketChannel.open();
            ssocket.configureBlocking(false);

            InetSocketAddress isa = null;
            if (ipAddress != null) {
                isa = new InetSocketAddress(ipAddress, port);
            } else {
                isa = new InetSocketAddress(port);
            }

            ssocket.socket().bind(isa);
            registerServerChannel(ssocket);

            Level level = log.getLevel();
            log.setLevel(Level.INFO);
            log.info(this.getName()+" listening on " + isa + ".");
            log.setLevel(level);

        } catch (IOException ioe) {
            log.error("Failure listening to socket on port '" + port + "'.", ioe);
            System.err.println("Failure listening to socket on port '" + port + "'.");
            ioe.printStackTrace();
            System.exit(-1);
        }
    }
    
    // documentation inherited
    protected void willStart() {
        super.willStart();
        initServerSocket();
    }

    protected void registerServerChannel(final ServerSocketChannel listener) throws IOException {

        // register this listening socket and map its select key to a net event
        // handler that will
        // accept new connections
        NetEventHandler serverNetEvent = new NetEventHandler() {

            private SelectionKey key;

            public int handleEvent(long when) {
                acceptConnection(listener);
                return 0;
            }

            public boolean checkIdle(long now) {
                return false; // we're never idle
            }

            public SelectionKey getSelectionKey() {
                return key;
            }

            public void setSelectionKey(SelectionKey key) {
                this.key = key;
            }

            public boolean doWrite() {
                return true;
            }
        };
        SelectionKey sk = listener.register(_selector, SelectionKey.OP_ACCEPT, serverNetEvent);
        serverNetEvent.setSelectionKey(sk);
        postRegisterNetEventHandler(serverNetEvent, SelectionKey.OP_ACCEPT);
    }
    
    protected void acceptConnection(ServerSocketChannel listener) {
        SocketChannel channel = null;
        try {
            channel = listener.accept();
            if (channel == null) {
                log.info("Psych! Got ACCEPT_READY, but no connection.");
                return;
            }

            if (!(channel instanceof SelectableChannel)) {
                try {
                    log.warn("Provided with un-selectable socket as result of accept(), can't " + "cope [channel=" + channel + "].");
                } catch (Error err) {
                    log.warn("Un-selectable channel also couldn't be printed.");
                }
                // stick a fork in the socket
                channel.socket().close();
                return;
            }
            Connection connection = connFactory.createConnection(channel, System.currentTimeMillis());
            if(connection instanceof AuthingableConnection){
            	((AuthingableConnection)connection).setAuthenticator(this.getAuthenticator());
            	((AuthingableConnection)connection).beforeAuthing();
            }
            
            if(manager != null){
            	manager.postRegisterNetEventHandler(connection,SelectionKey.OP_READ);
            }else{
            	this.postRegisterNetEventHandler(connection,SelectionKey.OP_READ);
            }
            
        } catch (Exception e) {
            if (channel != null) {
                try {
                    channel.socket().close();
                } catch (IOException ioe) {
                    log.warn("Failed closing aborted connection: " + ioe);
                }
            }
        }
    }

    public void closeAll() {
        super.closeAll();
        try {
            ssocket.close();
        } catch (IOException e) {
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public void shutdown(){
    	Level level = log.getLevel();
        log.setLevel(Level.INFO);
    	super.shutdown();
    	try {
			ssocket.close();
		} catch (IOException e) {
		}
    	log.warn(this.getName()+" shutdown completed!");
    	log.setLevel(level);
    }

	@Override
	public int getShutdownPriority() {
		return 10;
	}
}
