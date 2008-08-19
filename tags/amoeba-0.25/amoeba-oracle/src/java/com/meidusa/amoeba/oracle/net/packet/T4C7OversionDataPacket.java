package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * 数据库版本信息数据包
 * 
 * @author hexianmao
 * @version 2008-8-14 下午07:32:33
 */
public class T4C7OversionDataPacket extends T4CTTIfunPacket {

	public byte                        rdbmsVersion[]                              = { 78, 111, 116, 32, 100, 101, 116, 101, 114, 109, 105, 110, 101, 100, 32, 121, 101, 116 };
	public boolean                     rdbmsVersionO2U = true;
	public int                         bufLen = 256;
	public boolean                     retVerLenO2U = true;
	public int                         retVerLen = 0;
	public boolean                     retVerNumO2U = true;
	public long                        retVerNum = 0L;
    
    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        
        T4CPacketBuffer meg = (T4CPacketBuffer)buffer;
        //rdbmsVersionO2U = meg.unmarshalO2U();
        meg.marshalSWORD(bufLen);
        meg.marshalO2U(retVerLenO2U);
        meg.marshalO2U(retVerNumO2U);
        
        /*T4CPacketBuffer meg = (T4CPacketBuffer)buffer;
        boolean flag = false;
        T4CTTIoer oer = new T4CTTIoer(meg);
        while (true) {
            byte byte0 = meg.unmarshalSB1();
            switch (byte0) {
                case 4:
                    oer.init();
                    oer.unmarshal();
                    //oer.processError();
                    break;
                case 8:
                    if (flag){
                        //DatabaseError.throwSqlException(401);
                    }
                    retVerLen = meg.unmarshalUB2();
                    rdbmsVersion = meg.unmarshalCHR(retVerLen);
                    if (rdbmsVersion == null){
                        //DatabaseError.throwSqlException(438);
                    }
                    retVerNum = meg.unmarshalUB4();
                    T4CPacketBuffer.versionNumber = getVersionNumber();
                    flag = true;
                    continue;
                case 9:
                    if (getVersionNumber() >= 10000) {
                        short word0 = (short) meg.unmarshalUB2();
                        //connection.endToEndECIDSequenceNumber = word0;
                    }
                    break;
                default:{
                    //DatabaseError.throwSqlException(401);
                }
            }
            break;
        }*/
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer)buffer;
        meg.marshalO2U(rdbmsVersionO2U);
        meg.marshalSWORD(bufLen);
        meg.marshalO2U(retVerLenO2U);
        meg.marshalO2U(retVerNumO2U);
        
    }

}
