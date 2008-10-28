package com.meidusa.amoeba.oracle.net.packet;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.util.ByteUtil;

/**
 * @author hexianmao
 * @version 2008-8-21 下午06:28:49
 */
public class T4C8OcloseDataPacket extends T4CTTIfunPacket {

    public static final int DATA_LENGTH = 18;

    private static Logger   logger      = Logger.getLogger(T4C8OcloseDataPacket.class);

    protected int           cursorToCloseOffset;
    protected int[]         cursorToClose;

    protected int           queryToCloseOffset;
    protected int[]         queryToClose;

    public T4C8OcloseDataPacket(){
        super(TTIPFN, OCANA, (byte) 0);
    }

    public void initCloseQuery() {
        this.funCode = OCANA;
    }

    public void initCloseStatement() {
        this.funCode = OCCA;
    }

    public int getCursorToCloseOffset() {
        return cursorToCloseOffset;
    }

    public void setCursorToCloseOffset(int cursorToCloseOffset) {
        this.cursorToCloseOffset = cursorToCloseOffset;
    }

    public int[] getCursorToClose() {
        return cursorToClose;
    }

    public void setCursorToClose(int[] cursorToClose) {
        this.cursorToClose = cursorToClose;
    }

    public int getQueryToCloseOffset() {
        return queryToCloseOffset;
    }

    public void setQueryToCloseOffset(int queryToCloseOffset) {
        this.queryToCloseOffset = queryToCloseOffset;
    }

    public int[] getQueryToClose() {
        return queryToClose;
    }

    public void setQueryToClose(int[] queryToClose) {
        this.queryToClose = queryToClose;
    }

    @Override
    protected void unmarshal(AbstractPacketBuffer buffer) {
        super.unmarshal(buffer);
        parsePacket((T4CPacketBuffer) buffer);
    }

    protected void parsePacket(T4CPacketBuffer meg) {
        if (msgCode == TTIPFN && (funCode == OCCA || funCode == OCANA)) {
            unmarshalPart(meg);
            msgCode = (byte) meg.unmarshalUB1();
            funCode = meg.unmarshalUB1();
            seqNumber = (byte) meg.unmarshalUB1();
            parsePacket(meg);
        }
    }

    private void unmarshalPart(T4CPacketBuffer meg) {
        meg.unmarshalPTR();
        if (funCode == OCCA) {
            cursorToCloseOffset = (int) meg.unmarshalUB4();
            cursorToClose = new int[cursorToCloseOffset];
            for (int i = 0; i < cursorToCloseOffset; i++) {
                cursorToClose[i] = (int) (meg.unmarshalUB4() & 0xffffffff);
            }
        }
        if (funCode == OCANA) {
            queryToCloseOffset = (int) meg.unmarshalUB4();
            queryToClose = new int[queryToCloseOffset];
            for (int i = 0; i < queryToCloseOffset; i++) {
                queryToClose[i] = (int) (meg.unmarshalUB4() & 0xffffffff);
            }
        }
    }

    @Override
    protected void marshal(AbstractPacketBuffer buffer) {
        super.marshal(buffer);
        marshalPart((T4CPacketBuffer) buffer);
    }

    private void marshalPart(T4CPacketBuffer meg) {
        meg.marshalPTR();
        if (funCode == OCCA) {
            meg.marshalUB4(cursorToCloseOffset);
            for (int j = 0; j < cursorToCloseOffset; j++) {
                meg.marshalUB4(cursorToClose[j]);
            }
        }
        if (funCode == OCANA) {
            meg.marshalUB4(queryToCloseOffset);
            for (int j = 0; j < queryToCloseOffset; j++) {
                meg.marshalUB4(queryToClose[j]);
            }
        }
    }

    // //////////////////////////////////// static method /////////////////////////////////////////

    public static boolean isColseResultSet(byte[] message) {
        if (T4CTTIfunPacket.isFunType(message, T4CTTIMsgPacket.TTIPFN, T4CTTIfunPacket.OCANA)) {
            return true;
        }
        return false;
    }

    public static boolean isColseStatement(byte[] message) {
        if (T4CTTIfunPacket.isFunType(message, T4CTTIMsgPacket.TTIPFN, T4CTTIfunPacket.OCCA)) {
            return true;
        }
        return false;
    }

    /**
     * 过滤掉T4C8OcloseDataPacket数据包
     */
    public static byte[] filter(Connection conn, byte[] message) {
        T4CPacketBuffer meg = new T4CPacketBuffer(message);
        meg.init(conn);
        meg.setPosition(DATA_PACKET_HEADER_SIZE);

        byte msgCode = 0;
        byte funCode = 0;
        do {
            meg.unmarshalUB1();
            meg.unmarshalUB1();
            meg.unmarshalUB1();
            meg.unmarshalPTR();
            long l = meg.unmarshalUB4();
            for (int i = 0; i < l; i++) {
                meg.unmarshalUB4();
            }
            msgCode = message[meg.getPosition()];
            funCode = message[meg.getPosition() + 1];
        } while ((msgCode == TTIPFN) && (funCode == OCCA || funCode == OCANA));

        byte[] msg = new byte[message.length - meg.getPosition() + DATA_PACKET_HEADER_SIZE];
        System.arraycopy(message, 0, msg, 0, DATA_PACKET_HEADER_SIZE);
        System.arraycopy(message, meg.getPosition(), msg, DATA_PACKET_HEADER_SIZE, msg.length - DATA_PACKET_HEADER_SIZE);
        ByteUtil.toByte16BE((short) msg.length, msg, 0);

        if (logger.isDebugEnabled()) {
            byte[] ignore = new byte[meg.getPosition() - DATA_PACKET_HEADER_SIZE];
            System.arraycopy(message, DATA_PACKET_HEADER_SIZE, ignore, 0, ignore.length);
            logger.debug("");
            logger.debug("#filter close packet from client:" + ByteUtil.toHex(ignore, 0, ignore.length));
        }
        return msg;
    }

    public static void sendClosePacket(int cursor, Connection conn) {
        T4C8OcloseDataPacket close = new T4C8OcloseDataPacket();
        close.initCloseStatement();
        close.setCursorToCloseOffset(1);
        close.setCursorToClose(new int[] { cursor });
        T4CPacketBuffer meg = new T4CPacketBuffer(T4C8OcloseDataPacket.DATA_LENGTH);
        meg.init(conn);
        close.marshal(meg);

        byte[] ab = close.toByteBuffer(conn).array();
        if (logger.isDebugEnabled()) {
            int size = ((ab[0] & 0xff) << 8) | (ab[1] & 0xff);
            logger.debug(">>send to server close packet[" + size + "]:" + ByteUtil.toHex(ab, 0, ab.length));
        }
        conn.postMessage(ab);
    }

}
