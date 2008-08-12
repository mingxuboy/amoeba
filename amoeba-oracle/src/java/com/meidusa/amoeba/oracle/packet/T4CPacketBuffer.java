package com.meidusa.amoeba.oracle.packet;

import com.meidusa.amoeba.oracle.io.OraclePacketConstant;
import com.meidusa.amoeba.packet.AbstractPacketBuffer;

/**
 * Oracle,T4C格式的数据包buffer解析
 * 
 * @author hexianmao
 * @version 2008-8-13 上午01:02:40
 */
public class T4CPacketBuffer extends AbstractPacketBuffer implements OraclePacketConstant {

    public T4CPacketBuffer(byte[] buf){
        super(buf);
    }

    public T4CPacketBuffer(int size){
        super(size);
    }

    /**
     * 发送带符号的byte
     */
    void marshalSB1(byte b) {
        writeByte(b);
    }

    byte unmarshalSB1() {
        return 0;
    }

    /**
     * 发送无符号的byte
     */
    void marshalUB1(short s) {
        writeByte((byte) (s & 0xff));
    }

    short unmarshalUB1() {
        return 0;
    }

    void marshalSB2(short s) {
    }

    short unmarshalSB2() {
        return 0;
    }

    void marshalUB2(int i) {
    }

    int unmarshalUB2() {
        return 0;
    }

    void marshalSB4(int i) {
    }

    int unmarshalSB4() {
        return 0;
    }

    void marshalUB4(long l) {
    }

    long unmarshalUB4() {
        return 0;
    }

    void marshalSB8(long l) {
    }

    long unmarshalSB8() {
        return 0;
    }

    void marshalSWORD(int i) {
    }

    int unmarshalSWORD() {
        return 0;
    }

    void marshalUWORD(long l) {
    }

    long unmarshalUWORD() {
        return 0;
    }

    void marshalB1Array(byte[] ab) {
    }

    void marshalB1Array(byte[] ab, int i, int j) {
    }

    void marshalUB4Array(long[] al) {
    }

    void marshalO2U(boolean flag) {
    }

    void marshalNULLPTR() {
    }

    void marshalPTR() {
    }

    void marshalCHR(byte[] ab) {
    }

    byte[] unmarshalCHR(int i) {
        return null;
    }

    void marshalCHR(byte[] ab, int i, int j) {
    }

    void unmarshalCLR(byte[] ab, int i, int[] ai) {
    }

    void marshalCLR(byte[] ab, int i, int j) {
    }

    byte[] unmarshalCLR(int i, int[] ai) {
        return null;
    }

    void marshalCLR(byte[] ab, int i) {
    }

    void unmarshalCLR(byte[] ab, int i, int[] ai, int j) {
    }

    void marshalKEYVAL(byte[][] ab, int[] ai, byte[][] ab1, int[] ai1, byte[] ab2, int i) {
    }

    int unmarshalKEYVAL(byte[][] ab0, byte[][] ab1, int i) {
        return 0;
    }

    void marshalKEYVAL(byte[][] ab, byte[][] ab1, byte[] ab2, int i) {
    }

    void marshalDALC(byte[] ab) {
    }

    long unmarshalDALC(byte[] ab, int i, int[] ai) {
        return 0;
    }

    byte[] unmarshalDALC() {
        return null;
    }

    byte[] unmarshalDALC(int[] ai) {
        return null;
    }

    void addPtr(byte b) {
    }

}
