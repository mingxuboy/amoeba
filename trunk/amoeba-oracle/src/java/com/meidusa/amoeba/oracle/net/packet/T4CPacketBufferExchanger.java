package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.oracle.net.MessageQueuedHandler;
import com.meidusa.amoeba.oracle.net.OracleServerConnection;
import com.meidusa.amoeba.util.StaticString;
import com.meidusa.amoeba.util.ThreadLocalMap;

public class T4CPacketBufferExchanger extends T4CPacketBuffer {

    public T4CPacketBufferExchanger(byte[] buf){
        super(buf);
    }

    public T4CPacketBufferExchanger(int size){
        super(size);
    }

    public int readBytes(byte[] ab, int offset, int len) {
        needNextPacket(len);
        return super.readBytes(ab, offset, len);
    }

    public byte readByte() {
        needNextPacket(1);
        return super.readByte();
    }

    @SuppressWarnings("unchecked")
    private void needNextPacket(int len) {
        if (this.remaining() < len) {
            MessageQueuedHandler<byte[]> handler = (MessageQueuedHandler<byte[]>) ThreadLocalMap.get(StaticString.HANDLER);
            if (handler != null) {
                byte[] exchangedBuffer = handler.pop((OracleServerConnection) this.oconn);
                byte[] newBuffer = new byte[this.remaining() + exchangedBuffer.length - 10];
                System.arraycopy(buffer, position, newBuffer, 0, remaining());
                System.arraycopy(exchangedBuffer, 10, newBuffer, remaining(), exchangedBuffer.length - 10);
                this.buffer = newBuffer;
                this.setPacketLength(buffer.length);
                this.setPosition(0);
            }
        }
    }

    public static void main(String[] args) {
        byte[] buffer, exchangedBuffer;
        buffer = exchangedBuffer = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
        int position = 17;
        int head = 10;
        int remaining = (buffer.length - position);
        byte[] newBuffer = new byte[remaining + exchangedBuffer.length - head];
        System.arraycopy(buffer, position, newBuffer, 0, remaining);
        System.arraycopy(exchangedBuffer, head, newBuffer, remaining, exchangedBuffer.length - head);
        // System.out.println(Arrays.toString(newBuffer));
    }
}
