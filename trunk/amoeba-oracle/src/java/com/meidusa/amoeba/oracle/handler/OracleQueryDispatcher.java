package com.meidusa.amoeba.oracle.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.oracle.io.OraclePacketConstant;
import com.meidusa.amoeba.oracle.net.OracleClientConnection;
import com.meidusa.amoeba.oracle.net.packet.DataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4C8OallDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CTTIfunPacket;
import com.meidusa.amoeba.route.QueryRouter;

public class OracleQueryDispatcher implements MessageHandler {
	
	protected static Logger logger = Logger.getLogger(OracleQueryDispatcher.class);
	
    private byte[]	tmpBuffer           = null;
    private boolean	isFirstClientPacket = true;
    private List<byte[]> listBuffer = new ArrayList<byte[]>();
	public OracleQueryDispatcher(OracleClientConnection clientConn){
		clientConn.setMessageHandler(this);
	}
	
	public void handleMessage(Connection conn, byte[] message) {
		if (!T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OALL8)) {
			logger.error("unkonwn message......");
			return;
	    }
		
		if (DataPacket.isPacketEOF(message)){
			if (isFirstClientPacket){
				tmpBuffer = message;
			}else{
				listBuffer.add(message);
                mergeClientMessage(message);
			}
		}else{
			listBuffer.add(message);
			mergeClientMessage(message);
			return;
		}
		
		//根据
		if (DataPacket.isDataEOF(tmpBuffer)) {
            if (logger.isDebugEnabled()) {
                System.out.println("type:DataEOFPacket");
            }
        } else if (T4C8OallDataPacket.isParseable(tmpBuffer)) {
        	T4C8OallDataPacket dataPacket = new T4C8OallDataPacket();
    		dataPacket.init(tmpBuffer, conn);
    		
    		QueryRouter router = ProxyRuntimeContext.getInstance().getQueryRouter();
    		
    		Object[] parameters = new Object[dataPacket.getParamBytes().length];
    		
    		for(int i=0;i<parameters.length;i++){
    			parameters[i] = dataPacket.accessors[i].getObject(dataPacket.getParamBytes()[i]);
    		}
    		
    		ObjectPool[] pools = router.doRoute((DatabaseConnection)conn, dataPacket.sqlStmt, false, parameters);
    		OracleQueryMessageHandler handler = new OracleQueryMessageHandler(conn, pools);
            try {
            	handler.startSession();
            	if (isFirstClientPacket){
            		handler.handleMessage(conn, tmpBuffer);
            	}else{
    	        	for(byte[] clientMessage : listBuffer){
    	        		handler.handleMessage(conn, clientMessage);
    	        	}
            	}
            } catch (Exception e) {
            	logger.error("start quer:["+dataPacket.sqlStmt +"] error",e);
            	handler.endSession();
            }
        } else {
            if (logger.isDebugEnabled()) {
                System.out.println("type:OtherClientPacket");
            }
        }
        tmpBuffer = null;
    	isFirstClientPacket = true;
    	listBuffer.clear();
		
	}
	
    /**
     * 合并客户端数据包
     */
    private void mergeClientMessage(byte[] message) {
        if (!DataPacket.isDataType(message)) {
            return;
        }
        if (isFirstClientPacket) {
            tmpBuffer = new byte[message.length];
            System.arraycopy(message, 0, tmpBuffer, 0, message.length);
            isFirstClientPacket = false;
        } else {
            int appendLength = message.length - OraclePacketConstant.DATA_PACKET_HEADER_SIZE;
            byte[] newBytes = new byte[tmpBuffer.length + appendLength];
            System.arraycopy(tmpBuffer, 0, newBytes, 0, tmpBuffer.length);
            System.arraycopy(message, OraclePacketConstant.DATA_PACKET_HEADER_SIZE, newBytes, tmpBuffer.length, appendLength);
            tmpBuffer = newBytes;
        }
    }

}
