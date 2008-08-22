package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-21 ÏÂÎç06:28:49
 */
public class T4C8OcloseDataPacket extends T4CTTIfunPacket {

    protected int   cursorToCloseOffset;
    protected int[] cursorToClose;

    protected int   queryToCloseOffset;
    protected int[] queryToClose;

    public T4C8OcloseDataPacket(){
        super(TTIPFN, OCANA, (byte) 0);
    }

    @Override
    protected void marshal(AbstractPacketBuffer buffer) {
        super.marshal(buffer);

    }

    @Override
    protected void unmarshal(AbstractPacketBuffer buffer) {
        super.unmarshal(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
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

    void initCloseQuery() {
        this.funCode = OCANA;
    }

    void initCloseStatement() {
        this.funCode = OCCA;
    }

}
