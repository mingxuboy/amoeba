package com.meidusa.amoeba.oracle.net.packet;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.accessor.Accessor;
import com.meidusa.amoeba.oracle.accessor.T4CCharAccessor;
import com.meidusa.amoeba.oracle.accessor.T4CDateAccessor;
import com.meidusa.amoeba.oracle.accessor.T4CVarcharAccessor;
import com.meidusa.amoeba.oracle.accessor.T4CVarnumAccessor;
import com.meidusa.amoeba.oracle.util.ByteUtil;

/**
 * @author hexianmao
 * @version 2008-8-20 ÏÂÎç03:02:04
 */
public class T4C8OallDataPacket extends T4CTTIfunPacket {

    private static Logger  logger = Logger.getLogger(T4C8OallDataPacket.class);

    long                   options;
    int                    cursor;
    public int             numberOfBindPositions;
    public byte[][]        bindParams;
    int                    defCols;

    int                    al8i4Length;
    final long[]           al8i4  = new long[13];
    public int             sqlStmtLength;
    public byte[]          sqlStmt;
    public T4CTTIoac[]     oacdefBindsSent;
    T4CTTIoac[]            oacdefDefines;
    Accessor[]             definesAccessors;
    int                    receiveState;
    boolean                plsql;

    T4CTTIrxdDataPacket    rxd;
    T4C8TTIrxhDataPacket   rxh;
    T4CTTIoac              oac;
    T4CTTIdcbDataPacket    dcb;
    T4CTTIofetchDataPacket ofetch;
    T4CTTIoexecDataPacket  oexec;
    T4CTTIfobDataPacket    fob;

    public T4C8OallDataPacket(){
        super(OALL8);
        this.receiveState = 0;
        this.sqlStmt = new byte[0];
        this.plsql = false;
        this.defCols = 0;
    }

    @Override
    protected void unmarshal(AbstractPacketBuffer buffer) {
        super.unmarshal(buffer);

        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        if (msgCode == TTIFUN) {
            switch (funCode) {
                case OALL8:
                    parseOALL8(meg);
                    break;
                case OFETCH:
                    parseOFETCH(meg);
                    break;
                case OLOBOPS:
                    parseOLOBOPS(meg);
                    break;
                default:
                    if (logger.isDebugEnabled()) {
                        System.out.println("type:OtherFunPacket");
                    }
            }
        } else if (msgCode == TTIPFN) {
            if (funCode == OCCA) {
                parseOCCA(meg);
                msgCode = (byte) meg.unmarshalUB1();
                funCode = meg.unmarshalUB1();
                seqNumber = (byte) meg.unmarshalUB1();

                if (msgCode == TTIFUN) {
                    switch (funCode) {
                        case OALL8:
                            parseOALL8(meg);
                            break;
                        case OLOGOFF:
                            parseOLOGOFF(meg);
                            break;
                        default:
                            if (logger.isDebugEnabled()) {
                                System.out.println("type:OtherFunPacket");
                            }
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        System.out.println("type:OtherColsePacket");
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    System.out.println("type:OtherTTIPFNPacket");
                }
            }
        } else {
            if (logger.isDebugEnabled()) {
                System.out.println("type:OtherPacket");
            }
        }
    }

    void parseOALL8(T4CPacketBuffer meg) {
        unmarshalPisdef(meg);
        sqlStmt = meg.unmarshalCHR(sqlStmtLength);
        meg.unmarshalUB4Array(al8i4);
        unmarshalBindsTypes(meg);

        if (meg.versionNumber >= 9000 && defCols > 0) {
            oacdefDefines = new T4CTTIoac[defCols];
            for (int i = 0; i < defCols; i++) {
                oacdefDefines[i] = new T4CTTIoac(meg);
                oacdefDefines[i].unmarshal();
            }
        }

        // unmarshalBinds(meg);

        if (logger.isDebugEnabled()) {
            System.out.println("type:T4CTTIfunPacket.OALL8");
            System.out.println("sqlStmt:" + new String(sqlStmt));
            System.out.println("numberOfBindPositions:" + numberOfBindPositions);
            for (int i = 0; bindParams != null && i < bindParams.length; i++) {
                System.out.println("params_" + i + ":" + ByteUtil.toHex(bindParams[i], 0, bindParams[i].length));
            }
        }
    }

    void parseOFETCH(T4CPacketBuffer meg) {
        ofetch = new T4CTTIofetchDataPacket();
        // ofetch.init(buffer);
        this.cursor = ofetch.cursor;
        this.al8i4[1] = ofetch.al8i4_1;

        if (logger.isDebugEnabled()) {
            System.out.println("type:T4CTTIfunPacket.OFETCH");
        }
    }

    void parseOEXEC(T4CPacketBuffer meg) {
        oexec = new T4CTTIoexecDataPacket();
        this.cursor = oexec.cursor;
        this.al8i4[1] = oexec.al8i4_1;
        // int[] binds = null;

        if (logger.isDebugEnabled()) {
            System.out.println("type:T4CTTIfunPacket.OEXEC");
        }
    }

    void parseOLOBOPS(T4CPacketBuffer meg) {
        if (logger.isDebugEnabled()) {
            System.out.println("type:T4CTTIfunPacket.OLOBOPS");
        }
    }

    void parseOCCA(T4CPacketBuffer meg) {
        T4C8OcloseDataPacket packet = new T4C8OcloseDataPacket();
        packet.unmarshalPart(meg);
    }

    void parseOLOGOFF(T4CPacketBuffer meg) {
        if (logger.isDebugEnabled()) {
            System.out.println("type:T4CTTIfunPacket.OLOGOFF");
        }
    }

    // /////////////////////////////////////////////////////////////////////////////

    private void unmarshalPisdef(T4CPacketBuffer meg) {
        options = meg.unmarshalUB4();
        cursor = meg.unmarshalSWORD();
        meg.unmarshalPTR();
        sqlStmtLength = meg.unmarshalSWORD();
        meg.unmarshalPTR();
        al8i4Length = meg.unmarshalSWORD();
        meg.unmarshalPTR();
        meg.unmarshalPTR();
        meg.unmarshalUB4();
        meg.unmarshalUB4();
        meg.unmarshalUB4();

        meg.unmarshalPTR();
        numberOfBindPositions = meg.unmarshalSWORD();

        meg.unmarshalPTR();
        meg.unmarshalPTR();
        meg.unmarshalPTR();
        meg.unmarshalPTR();
        meg.unmarshalPTR();

        if (meg.versionNumber >= 9000) {
            meg.unmarshalPTR();
            defCols = meg.unmarshalSWORD();
        }
    }

    private void unmarshalBindsTypes(T4CPacketBuffer meg) {
        if (numberOfBindPositions <= 0) {
            return;
        }
        oacdefBindsSent = new T4CTTIoac[numberOfBindPositions];
        for (int i = 0; i < numberOfBindPositions; i++) {
            oacdefBindsSent[i] = new T4CTTIoac(meg);
            oacdefBindsSent[i].unmarshal();
        }
    }

    private void unmarshalBinds(T4CPacketBuffer meg) {
        if (numberOfBindPositions <= 0) {
            return;
        }
        short msgCode = meg.unmarshalUB1();
        if (msgCode == TTIRXD) {
            bindParams = new byte[numberOfBindPositions][];
            for (int i = 0; i < numberOfBindPositions; i++) {
                bindParams[i] = meg.unmarshalCLRforREFS();
            }
        } else {
            throw new RuntimeException();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    static byte[][] desc = { { (byte) 0x06, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x16, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x06, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x16, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x06, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x0b, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x0c, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x08, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x11, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x04, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x07, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x0c, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x04, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x09, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x0c, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 } };

    static byte[][] data = { { (byte) 0x06, (byte) 0xc5, (byte) 0x02, (byte) 0x17, (byte) 0x2b, (byte) 0x62, (byte) 0x28 }, { (byte) 0x05, (byte) 0x63, (byte) 0x68, (byte) 0x69, (byte) 0x6e, (byte) 0x61 }, { (byte) 0x05, (byte) 0xc4, (byte) 0x02, (byte) 0x04, (byte) 0x29, (byte) 0x39 }, { (byte) 0x06, (byte) 0x65, (byte) 0x78, (byte) 0x70, (byte) 0x69, (byte) 0x72, (byte) 0x65 }, { (byte) 0x0b, (byte) 0x61, (byte) 0x62, (byte) 0x63, (byte) 0x5f, (byte) 0x73, (byte) 0x75, (byte) 0x62, (byte) 0x6a, (byte) 0x65, (byte) 0x63, (byte) 0x74 }, { (byte) 0x0c, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39, (byte) 0x2b, (byte) 0x2b, (byte) 0x70 }, { (byte) 0x08, (byte) 0x68, (byte) 0x7a, (byte) 0x5f, (byte) 0x63, (byte) 0x68, (byte) 0x69, (byte) 0x6e, (byte) 0x61 }, { (byte) 0x11, (byte) 0x68, (byte) 0x65, (byte) 0x78, (byte) 0x69, (byte) 0x61, (byte) 0x6e, (byte) 0x6d, (byte) 0x61, (byte) 0x6f, (byte) 0x40, (byte) 0x31, (byte) 0x36, (byte) 0x33, (byte) 0x2e, (byte) 0x63, (byte) 0x6f, (byte) 0x6d }, { (byte) 0x04, (byte) 0x33, (byte) 0x35, (byte) 0x31, (byte) 0x34 }, { (byte) 0x07, (byte) 0x61, (byte) 0x6c, (byte) 0x69, (byte) 0x62, (byte) 0x61, (byte) 0x62, (byte) 0x61 }, { (byte) 0x02, (byte) 0x43, (byte) 0x4e }, { (byte) 0x02, (byte) 0x68, (byte) 0x65 }, { (byte) 0x05, (byte) 0x67, (byte) 0x6f, (byte) 0x6f, (byte) 0x64, (byte) 0x21 }, { (byte) 0x07, (byte) 0x78, (byte) 0x6c, (byte) 0x07, (byte) 0x1e, (byte) 0x01, (byte) 0x01, (byte) 0x01 }, { (byte) 0x04, (byte) 0x53, (byte) 0x41, (byte) 0x4c, (byte) 0x45 }, { (byte) 0x09, (byte) 0x70, (byte) 0x75, (byte) 0x62, (byte) 0x6c, (byte) 0x69, (byte) 0x73, (byte) 0x68, (byte) 0x65, (byte) 0x64 }, { (byte) 0x07, (byte) 0x78, (byte) 0x6c, (byte) 0x07, (byte) 0x1e, (byte) 0x01, (byte) 0x01, (byte) 0x01 } };

    static void fillupAccessors() {
        for (int i = 0; i < desc.length; i++) {
            switch (desc[i][0] & 0xff) {
                case Accessor.CHAR:
                    System.out.print("Accessor.CHAR");
                    System.out.println(T4CCharAccessor.getString(data[i], desc[i][5], desc[i][5]));
                    System.out.println();
                    break;

                case Accessor.NUMBER:
                    System.out.print("Accessor.NUMBER");
                    // T4CNumberAccessor
                    System.out.println();
                    break;

                case Accessor.VARCHAR:
                    System.out.print("Accessor.VARCHAR:");
                    System.out.println(T4CVarcharAccessor.getString(data[i], desc[i][5], desc[i][5]));
                    System.out.println();
                    break;

                case Accessor.LONG:
                    System.out.print("Accessor.LONG");
                    // T4CLongAccessor
                    System.out.println();
                    break;

                case Accessor.VARNUM:
                    System.out.print("Accessor.VARNUM:");
                    System.out.println(T4CVarnumAccessor.getLong(data[i]));
                    System.out.println();
                    break;

                case Accessor.BINARY_FLOAT:
                    System.out.print("Accessor.BINARY_FLOAT");
                    // T4CBinaryFloatAccessor
                    System.out.println();
                    break;

                case Accessor.BINARY_DOUBLE:
                    System.out.print("Accessor.BINARY_DOUBLE");
                    // T4CBinaryDoubleAccessor
                    System.out.println();
                    break;

                case Accessor.RAW:
                    System.out.print("Accessor.RAW");
                    // T4CRawAccessor
                    System.out.println();
                    break;

                case Accessor.LONG_RAW:
                    System.out.print("Accessor.LONG_RAW");
                    // T4CPacketBuffer.versionNumber >= 9000 T4CRawAccessor
                    // T4CLongRawAccessor
                    System.out.println();

                    break;

                case Accessor.ROWID:
                    System.out.print("Accessor.ROWID");
                    System.out.println();

                case Accessor.UROWID:
                    System.out.print("Accessor.UROWID");
                    // T4CRowidAccessor
                    System.out.println();
                    break;

                case Accessor.RESULT_SET:
                    System.out.print("Accessor.RESULT_SET");
                    // T4CResultSetAccessor
                    System.out.println();
                    break;

                case Accessor.DATE:
                    System.out.print("Accessor.DATE:");
                    System.out.println(T4CDateAccessor.getDate(data[i]));
                    System.out.println();

                    break;

                case Accessor.BLOB:
                    System.out.print("Accessor.BLOB");
                    // l1 == -4 && T4CPacketBuffer.versionNumber >= 9000 T4CLongRawAccessor
                    // l1 == -3 && T4CPacketBuffer.versionNumber >= 9000 T4CRawAccessor
                    // T4CBlobAccessor
                    System.out.println();
                    break;

                case Accessor.CLOB:
                    System.out.print("Accessor.CLOB");
                    // l1 == -1 && T4CPacketBuffer.versionNumber >= 9000 T4CLongAccessor
                    // (l1 == 12 || l1 == 1) && T4CPacketBuffer.versionNumber >= 9000
                    // T4CVarcharAccessor
                    // T4CClobAccessor
                    System.out.println();
                    break;

                case Accessor.BFILE:
                    System.out.print("Accessor.BFILE");
                    // T4CBfileAccessor
                    System.out.println();
                    break;

                case Accessor.NAMED_TYPE:
                    System.out.print("Accessor.NAMED_TYPE");
                    // T4CNamedTypeAccessor
                    System.out.println();
                    break;

                case Accessor.REF_TYPE:
                    System.out.print("Accessor.REF_TYPE");
                    // T4CRefTypeAccessor
                    System.out.println();
                    break;

                case Accessor.TIMESTAMP:
                    System.out.print("Accessor.TIMESTAMP");
                    // T4CTimestampAccessor
                    System.out.println();
                    break;

                case Accessor.TIMESTAMPTZ:
                    System.out.print("Accessor.TIMESTAMPTZ");
                    // T4CTimestamptzAccessor
                    System.out.println();
                    break;

                case Accessor.TIMESTAMPLTZ:
                    System.out.print("Accessor.TIMESTAMPLTZ");
                    // T4CTimestampltzAccessor
                    System.out.println();
                    break;

                case Accessor.INTERVALYM:
                    System.out.print("Accessor.INTERVALYM");
                    // T4CIntervalymAccessor
                    System.out.println();
                    break;

                case Accessor.INTERVALDS:
                    System.out.print("Accessor.INTERVALDS");
                    // T4CIntervaldsAccessor
                    System.out.println();
                    break;
                default:
                    throw new RuntimeException("unknown data type!");
            }
        }
    }

    public static void main(String[] args) {

        long st = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            fillupAccessors();
        }
        long et = System.currentTimeMillis();

        System.out.println(desc.length + ":" + (et - st) + " ms");
    }

}
