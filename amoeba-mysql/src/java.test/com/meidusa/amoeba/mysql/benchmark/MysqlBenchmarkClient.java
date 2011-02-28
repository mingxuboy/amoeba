package com.meidusa.amoeba.mysql.benchmark;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.benchmark.AbstractBenchmarkClient;
import com.meidusa.amoeba.benchmark.AbstractBenchmark.TaskRunnable;
import com.meidusa.amoeba.config.ParameterMapping;
import com.meidusa.amoeba.mysql.handler.session.SessionStatus;
import com.meidusa.amoeba.mysql.net.packet.AbstractPacket;
import com.meidusa.amoeba.mysql.net.packet.CommandPacket;
import com.meidusa.amoeba.mysql.net.packet.EOFPacket;
import com.meidusa.amoeba.mysql.net.packet.ErrorPacket;
import com.meidusa.amoeba.mysql.net.packet.FieldPacket;
import com.meidusa.amoeba.mysql.net.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.mysql.net.packet.OkPacket;
import com.meidusa.amoeba.mysql.net.packet.QueryCommandPacket;
import com.meidusa.amoeba.mysql.net.packet.ResultSetHeaderPacket;
import com.meidusa.amoeba.mysql.net.packet.RowDataPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.util.CmdLineParser;
import com.meidusa.amoeba.util.StringUtil;
import com.meidusa.amoeba.util.CmdLineParser.Option;

/**
 * 
 * @author Struct
 *
 */
public class MysqlBenchmarkClient extends AbstractBenchmarkClient<AbstractPacket> {
	private static Logger	logger        = Logger.getLogger(MysqlBenchmarkClient.class);
	public MysqlBenchmarkClient(Connection connection,CountDownLatch requestLatcher,CountDownLatch responseLatcher,TaskRunnable task) {
		super(connection,requestLatcher,responseLatcher,task);
	}

	final Map<String ,String > parameterMap = new HashMap<String,String>(); 
	final Map<String ,Object > beanParameterMap = new HashMap<String,Object>();
	private byte commandType;
	private int packetIndex;
	private int statusCode; 
	
	public AbstractPacket createRequestPacket() {
		Properties properties = this.getRequestProperties();
		AbstractPacket packet = null;
		try {
			packet = (AbstractPacket)Class.forName((String)properties.get("class")).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		Map<String,Object> map = this.getNextRequestContextMap();
		ParameterMapping.mappingObjectField(packet, beanParameterMap,map,this, AbstractPacket.class);
		if(packet instanceof CommandPacket){
			commandType = ((CommandPacket)packet).command;
		}
		packetIndex = 0;
		statusCode = 0;
		return packet;
	}
	
	public void init(){
		super.init();
		Properties properties = this.getRequestProperties();
		for(Map.Entry<Object,Object> entry : properties.entrySet()){
			if(entry.getKey().toString().startsWith("parameterMap.")){
				parameterMap.put(entry.getKey().toString().substring("parameterMap.".length()), entry.getValue().toString());
			}else{
				beanParameterMap.put(entry.getKey().toString(), entry.getValue());
			}
		}
		CmdLineParser parser = this.getBenchmark().getCmdLineParser();
		Option option = parser.getOption("sql");
		String sql = (String)parser.getOptionValue(option);
		if(!StringUtil.isEmpty(sql)){
			beanParameterMap.put("query", sql);
		}
	}
	
	public AbstractPacket decodeRecievedPacket(byte[] buffer) {
		AbstractPacket packet = null;
		if (packetIndex == 0 && MysqlPacketBuffer.isErrorPacket(buffer)){
			packet = new ErrorPacket();
		} else if (packetIndex == 0 && MysqlPacketBuffer.isOkPacket(buffer)) {
			packet = new OkPacket();
		} else if (MysqlPacketBuffer.isEofPacket(buffer)) {
			packet = new EOFPacket();
		}else{
			if((statusCode & SessionStatus.EOF_FIELDS) >0){
				packet = new RowDataPacket(false);
			}else if(packetIndex == 0){
				packet = new ResultSetHeaderPacket();
			}else{
				packet = new FieldPacket();
			}
		}
		
		packet.init(buffer, this.getConnection());
		return packet;
	}
	
	protected void afterMessageRecieved(byte[] message){
		packetIndex ++ ;
	}

	protected boolean responseIsCompleted(byte[] buffer){
		if (this.commandType == QueryCommandPacket.COM_QUERY) {
            boolean isCompleted = false;
            if (packetIndex == 0 && MysqlPacketBuffer.isErrorPacket(buffer)) {
                statusCode |= SessionStatus.ERROR;
                statusCode |= SessionStatus.COMPLETED;
                isCompleted = true;
            } else if (packetIndex == 0 && MysqlPacketBuffer.isOkPacket(buffer)) {
                statusCode |= SessionStatus.OK;
                statusCode |= SessionStatus.COMPLETED;
                isCompleted = true;
            } else if (MysqlPacketBuffer.isEofPacket(buffer)) {
                if ((statusCode & SessionStatus.EOF_FIELDS) > 0) {
                    statusCode |= SessionStatus.EOF_ROWS;
                    statusCode |= SessionStatus.COMPLETED;
                    isCompleted = true;
                } else {
                    statusCode |= SessionStatus.EOF_FIELDS;
                    isCompleted = false;
                }
            } else {
                if (statusCode == SessionStatus.QUERY) {
                    statusCode |= SessionStatus.RESULT_HEAD;
                }
            }
            return isCompleted;
        } else {
        	if(this.commandType == QueryCommandPacket.COM_INIT_DB){
    			boolean isCompleted = false; 
    			if(packetIndex == 0 && MysqlPacketBuffer.isErrorPacket(buffer)){
    				statusCode |= SessionStatus.ERROR;
    				statusCode |= SessionStatus.COMPLETED;
    				isCompleted = true;
    			}else if(packetIndex == 0 && MysqlPacketBuffer.isOkPacket(buffer)){
    				statusCode |= SessionStatus.OK;
    				statusCode |= SessionStatus.COMPLETED;
    				isCompleted = true;
    			}
    			return isCompleted;
    		}else{
    			return false;
    		}
        }
		
	}
	
}
