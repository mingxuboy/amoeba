package com.meidusa.amoeba.mongodb.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;

/**
 * <h6><a name="MongoWireProtocol-OPMSG"></a>OP_MSG <a name="MongoWireProtocol-OPMSG"></a></h6>
 * 
 * <p>Deprecated. OP_MSG sends a diagnostic message to the database.&nbsp; The database sends back a fixed resonse.&nbsp; The format is</p>
 * 
 * <div class="code panel" style="border-width: 1px;"><div class="codeContent panelContent">
 * <pre class="code-java">struct {
 *     MsgHeader header;  <span class="code-comment">// standard message header
 * </span>    cstring   message; <span class="code-comment">// message <span class="code-keyword">for</span> the database
 * </span>}
 * </pre>
 * @author Struct
 * @deprecated
 */
public class MessageMongodbPacket extends AbstractMongodbPacket {

	public String message;
	public MessageMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_MSG;
	}
	
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		message = buffer.readCString();
	}
	
	protected void write2Buffer(MongodbPacketBuffer buffer)
	throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeCString(message);
	}
}
