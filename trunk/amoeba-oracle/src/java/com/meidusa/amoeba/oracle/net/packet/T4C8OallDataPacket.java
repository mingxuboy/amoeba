package com.meidusa.amoeba.oracle.net.packet;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.accessor.Accessor;
import com.meidusa.amoeba.oracle.accessor.T4CDateAccessor;
import com.meidusa.amoeba.oracle.accessor.T4CVarcharAccessor;
import com.meidusa.amoeba.oracle.accessor.T4CVarnumAccessor;

/**
 * @author hexianmao
 * @version 2008-8-20 ÏÂÎç03:02:04
 */
public class T4C8OallDataPacket extends T4CTTIfunPacket {

    private static Logger  logger = Logger.getLogger(T4C8OallDataPacket.class);

    public byte[]          sqlStmt;
    public int             numberOfBindPositions;
    public T4CTTIoac[]     oacBind;
    public String[]        paramType;
    public String[]        paramValue;

    long                   options;
    int                    cursor;
    int                    defCols;
    int                    al8i4Length;
    final long[]           al8i4  = new long[13];
    public int             sqlStmtLength;
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
            parseFunPacket(meg);
        } else if (msgCode == TTIPFN && funCode == OCCA) {
            T4C8OcloseDataPacket closePacket = new T4C8OcloseDataPacket();
            closePacket.initCloseStatement();
            closePacket.parsePacket(meg);
            msgCode = closePacket.msgCode;
            funCode = closePacket.funCode;
            seqNumber = closePacket.seqNumber;
            parseFunPacket(meg);
        } else {
            if (logger.isDebugEnabled()) {
                System.out.println("type:OtherPacket msgCode:" + msgCode + " funCode:" + funCode);
            }
        }
    }

    void parseFunPacket(T4CPacketBuffer meg) {
        switch (funCode) {
            case OALL8:
                parseOALL8(meg);
                break;
            case OFETCH:
                parseOFETCH(meg);
                break;
            case OEXEC:
                parseOEXEC(meg);
                break;
            case OLOBOPS:
                parseOLOBOPS(meg);
                break;
            case OLOGOFF:
                parseOLOGOFF(meg);
                break;
            default:
                if (logger.isDebugEnabled()) {
                    System.out.println("type:OtherFunPacket funCode:" + funCode);
                }
        }
    }

    // /////////////////////////////////////////////////////////////////////////////

    private void parseOALL8(T4CPacketBuffer meg) {
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

        unmarshalBinds(meg);

        if (logger.isDebugEnabled()) {
            System.out.println("type:T4CTTIfunPacket.OALL8");
            System.out.println("sqlStmt:" + new String(sqlStmt));
            System.out.println("numberOfBindPositions:" + numberOfBindPositions);
            for (int i = 0; i < numberOfBindPositions; i++) {
                System.out.println("param_info_" + i + ":" + oacBind[i]);
                System.out.print("param_value_" + i + ": [" + paramType[i] + "]");
                if (paramValue[i] != null && paramValue[i].length() > 1000) {
                    System.out.println("-[" + paramValue[i].substring(0, 1000) + "... #dataLength:" + paramValue[i].length() + "]");
                } else {
                    System.out.println("-[" + paramValue[i] + "]");
                }
            }
        }
    }

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
        oacBind = new T4CTTIoac[numberOfBindPositions];
        paramType = new String[numberOfBindPositions];
        paramValue = new String[numberOfBindPositions];

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
        for (int i = 0; i < numberOfBindPositions; i++) {
            oacBind[i] = new T4CTTIoac(meg);
            oacBind[i].unmarshal();
        }
    }

    private void unmarshalBinds(T4CPacketBuffer meg) {
        if (numberOfBindPositions <= 0) {
            return;
        }
        short msgCode = meg.unmarshalUB1();
        if (msgCode == TTIRXD) {
            for (int i = 0; i < numberOfBindPositions; i++) {
                parseParam(i, oacBind[i], meg.unmarshalCLRforREFS());
            }
        } else {
            throw new RuntimeException();
        }
    }

    private void parseOFETCH(T4CPacketBuffer meg) {
        ofetch = new T4CTTIofetchDataPacket();
        // ofetch.init(buffer);
        this.cursor = ofetch.cursor;
        this.al8i4[1] = ofetch.al8i4_1;

        if (logger.isDebugEnabled()) {
            System.out.println("type:T4CTTIfunPacket.OFETCH");
        }
    }

    private void parseOEXEC(T4CPacketBuffer meg) {
        oexec = new T4CTTIoexecDataPacket();
        this.cursor = oexec.cursor;
        this.al8i4[1] = oexec.al8i4_1;
        // int[] binds = null;

        if (logger.isDebugEnabled()) {
            System.out.println("type:T4CTTIfunPacket.OEXEC");
        }
    }

    private void parseOLOBOPS(T4CPacketBuffer meg) {
        if (logger.isDebugEnabled()) {
            System.out.println("type:T4CTTIfunPacket.OLOBOPS");
        }
    }

    private void parseOLOGOFF(T4CPacketBuffer meg) {
        if (logger.isDebugEnabled()) {
            System.out.println("type:T4CTTIfunPacket.OLOGOFF");
        }
    }

    private void parseParam(int idx, T4CTTIoac oac, byte[] data) {
        switch (oac.oacdty) {
            case Accessor.CHAR:
                paramType[idx] = "CHAR";
                // System.out.println(T4CCharAccessor.getString(data, desc[i][5], desc[i][5]));
                break;

            case Accessor.NUMBER:
                paramType[idx] = "NUMBER";
                // T4CNumberAccessor
                break;

            case Accessor.VARCHAR:
                paramType[idx] = "VARCHAR";
                paramValue[idx] = T4CVarcharAccessor.getString(data, oac.oacmxl, oac.oacmxl);
                break;

            case Accessor.LONG:
                paramType[idx] = "LONG";
                // T4CLongAccessor
                break;

            case Accessor.VARNUM:
                paramType[idx] = "VARNUM";
                paramValue[idx] = Long.toString(T4CVarnumAccessor.getLong(data));
                // System.out.println(T4CVarnumAccessor.getLong(data));
                break;

            case Accessor.BINARY_FLOAT:
                paramType[idx] = "BINARY_FLOAT";
                // T4CBinaryFloatAccessor
                break;

            case Accessor.BINARY_DOUBLE:
                paramType[idx] = "BINARY_DOUBLE";
                // T4CBinaryDoubleAccessor
                break;

            case Accessor.RAW:
                paramType[idx] = "RAW";
                // T4CRawAccessor
                break;

            case Accessor.LONG_RAW:
                paramType[idx] = "LONG_RAW";
                // T4CPacketBuffer.versionNumber >= 9000 T4CRawAccessor
                // T4CLongRawAccessor

                break;

            case Accessor.ROWID:
                paramType[idx] = "ROWID";

            case Accessor.UROWID:
                paramType[idx] = "UROWID";
                // T4CRowidAccessor
                break;

            case Accessor.RESULT_SET:
                paramType[idx] = "RESULT_SET";
                // T4CResultSetAccessor
                break;

            case Accessor.DATE:
                paramType[idx] = "DATE";
                paramValue[idx] = T4CDateAccessor.getDate(data).toString();
                break;

            case Accessor.BLOB:
                paramType[idx] = "BLOB";
                // l1 == -4 && T4CPacketBuffer.versionNumber >= 9000 T4CLongRawAccessor
                // l1 == -3 && T4CPacketBuffer.versionNumber >= 9000 T4CRawAccessor
                // T4CBlobAccessor
                break;

            case Accessor.CLOB:
                paramType[idx] = "CLOB";
                // l1 == -1 && T4CPacketBuffer.versionNumber >= 9000 T4CLongAccessor
                // (l1 == 12 || l1 == 1) && T4CPacketBuffer.versionNumber >= 9000
                // T4CVarcharAccessor
                // T4CClobAccessor
                break;

            case Accessor.BFILE:
                paramType[idx] = "BFILE";
                // T4CBfileAccessor
                break;

            case Accessor.NAMED_TYPE:
                paramType[idx] = "NAMED_TYPE";
                // T4CNamedTypeAccessor
                break;

            case Accessor.REF_TYPE:
                paramType[idx] = "REF_TYPE";
                // T4CRefTypeAccessor
                break;

            case Accessor.TIMESTAMP:
                paramType[idx] = "TIMESTAMP";
                // T4CTimestampAccessor
                break;

            case Accessor.TIMESTAMPTZ:
                paramType[idx] = "TIMESTAMPTZ";
                // T4CTimestamptzAccessor
                break;

            case Accessor.TIMESTAMPLTZ:
                paramType[idx] = "TIMESTAMPLTZ";
                // T4CTimestampltzAccessor
                break;

            case Accessor.INTERVALYM:
                paramType[idx] = "INTERVALYM";
                // T4CIntervalymAccessor
                break;

            case Accessor.INTERVALDS:
                paramType[idx] = "INTERVALDS";
                // T4CIntervaldsAccessor
                break;
            default:
                throw new RuntimeException("unknown data type!");
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////

    public static boolean isParseable(byte[] message) {
        if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OALL8)) {
            return true;
        }
        if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OFETCH)) {
            return true;
        }
        if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OEXEC)) {
            return true;
        }
        if (T4CTTIfunPacket.isFunType(message, T4CTTIfunPacket.OLOBOPS)) {
            return true;
        }
        if (T4CTTIfunPacket.isFunType(message, T4CTTIMsgPacket.TTIPFN, T4CTTIfunPacket.OCCA)) {
            return true;
        }
        return false;
    }

}
