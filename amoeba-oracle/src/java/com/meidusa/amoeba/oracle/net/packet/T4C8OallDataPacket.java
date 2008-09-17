package com.meidusa.amoeba.oracle.net.packet;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.accessor.Accessor;
import com.meidusa.amoeba.oracle.accessor.T4CCharAccessor;
import com.meidusa.amoeba.oracle.accessor.T4CDateAccessor;
import com.meidusa.amoeba.oracle.accessor.T4CNumberAccessor;
import com.meidusa.amoeba.oracle.accessor.T4CVarcharAccessor;
import com.meidusa.amoeba.oracle.accessor.T4CVarnumAccessor;
import com.meidusa.amoeba.oracle.handler.OracleQueryMessageHandler;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIoac;

/**
 * @author hexianmao
 * @version 2008-8-20 下午03:02:04
 */
public class T4C8OallDataPacket extends T4CTTIfunPacket {

    private static Logger             logger = Logger.getLogger(T4C8OallDataPacket.class);

    long                              options;
    int                               cursor;
    int                               sqlStmtLength;
    int                               numberOfParams;
    int                               al8i4Length;
    int                               defCols;

    public String                     sqlStmt;                                            // sql
    long[]                            al8i4;
    T4CTTIoac[]                       oacBind;
    public Accessor[]                 accessors;                                          // parameter Accessors
    T4CTTIoac[]                       oacdefDefines;
    public Accessor[]                 definesAccessors;
    public byte[][]                   paramBytes;                                         // parameter bytes

    private OracleQueryMessageHandler handler;

    public T4C8OallDataPacket(){
        super(OALL8);
        this.defCols = 0;
        this.al8i4 = new long[13];
    }

    public T4C8OallDataPacket(OracleQueryMessageHandler handler){
        this();
        this.handler = handler;
    }

    public void setHandlerProps() {
    }

    public String getSqlStmt() {
        return sqlStmt;
    }

    public Accessor[] getAccessors() {
        return accessors;
    }

    public byte[][] getParamBytes() {
        return paramBytes;
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

    // ///////////////////////////////////////////////////////////////////////////////////////
    private void parseFunPacket(T4CPacketBuffer meg) {
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

    private void parseOALL8(T4CPacketBuffer meg) {
        unmarshalPisdef(meg);

        byte[] sqlStmtBytes = meg.unmarshalCHR(sqlStmtLength);

        sqlStmt = meg.getConversion().CharBytesToString(sqlStmtBytes, sqlStmtLength);

        meg.unmarshalUB4Array(al8i4);

        oacBind = new T4CTTIoac[numberOfParams];
        accessors = new Accessor[numberOfParams];
        unmarshalBindsTypes(meg);// 解析参数描述，并初始化相应的accessor。

        if (meg.versionNumber >= 9000 && defCols > 0) {
            oacdefDefines = new T4CTTIoac[defCols];
            for (int i = 0; i < defCols; i++) {
                oacdefDefines[i] = new T4CTTIoac(meg);
                oacdefDefines[i].unmarshal();
            }
        }

        paramBytes = new byte[numberOfParams][];
        if (numberOfParams > 0) {
            unmarshalBinds(meg);// 解析参数，并读取相应的参数值。
        }

        if (logger.isDebugEnabled()) {
            System.out.println("type:T4CTTIfunPacket.OALL8");
            System.out.println("sqlStmt:" + sqlStmt);
            System.out.println("numberOfParams:" + numberOfParams);
            for (int i = 0; i < numberOfParams; i++) {
                System.out.println("param_des_" + i + ":" + oacBind[i]);
                Object object = accessors[i].getObject(paramBytes[i]);
                if (object instanceof String) {
                    String s = (String) object;
                    if (s.length() > 4000) {
                        System.out.println("param_val_" + i + ":" + s.substring(0, 4000) + "... #dataLength:" + s.length());
                    } else {
                        System.out.println("param_val_" + i + ":" + s);
                    }
                } else {
                    System.out.println("param_val_" + i + ":" + object);
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
        numberOfParams = meg.unmarshalSWORD();

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
        for (int i = 0; i < numberOfParams; i++) {
            oacBind[i] = new T4CTTIoac(meg);
            oacBind[i].unmarshal();
            fillAccessor(i, oacBind[i], meg);
        }
    }

    private void fillAccessor(int i, T4CTTIoac oac, T4CPacketBuffer meg) {
        switch (oac.oacdty) {
            case Accessor.CHAR:
                accessors[i] = new T4CCharAccessor();
                break;
            case Accessor.NUMBER:
                accessors[i] = new T4CNumberAccessor();
                break;
            case Accessor.VARCHAR:
                accessors[i] = new T4CVarcharAccessor();
                break;
            case Accessor.LONG:
                // accessors[idx] = new T4CLongAccessor();
                break;
            case Accessor.VARNUM:
                accessors[i] = new T4CVarnumAccessor();
                break;
            case Accessor.BINARY_FLOAT:
                // accessors[idx] = new T4CBinaryFloatAccessor();
                break;
            case Accessor.BINARY_DOUBLE:
                // accessors[idx] = new T4CBinaryDoubleAccessor();
                break;
            case Accessor.RAW:
                // accessors[idx] = new T4CRawAccessor();
                break;
            case Accessor.LONG_RAW:
                // if (meg.versionNumber >= 9000) {
                // accessors[idx] = new T4CRawAccessor();
                // } else {
                // accessors[idx] = new T4CLongRawAccessor();
                // }
                break;
            case Accessor.ROWID:
            case Accessor.UROWID:
                // accessors[idx] = new T4CRowidAccessor();
                break;
            case Accessor.RESULT_SET:
                // accessors[idx] = new T4CResultSetAccessor();
                break;
            case Accessor.DATE:
                accessors[i] = new T4CDateAccessor();
                break;
            case Accessor.BLOB:
                // if (meg.versionNumber >= 9000 && l1 == -4) {
                // accessors[idx] = new T4CLongRawAccessor();
                // } else if (meg.versionNumber >= 9000 && l1 == -3) {
                // accessors[idx] = new T4CRawAccessor();
                // } else {
                // accessors[idx] = new T4CBlobAccessor();
                // }
                break;
            case Accessor.CLOB:
                // if (meg.versionNumber >= 9000 && l1 == -1) {
                // accessors[idx] = new T4CLongAccessor();
                // } else if (meg.versionNumber >= 9000 && (l1 == 12 || l1 == 1)) {
                // accessors[idx] = new T4CVarcharAccessor();
                // } else {
                // accessors[idx] = new T4CClobAccessor();
                // }
                break;
            case Accessor.BFILE:
                // accessors[idx] = new T4CBfileAccessor();
                break;
            case Accessor.NAMED_TYPE:
                // accessors[idx] = new T4CNamedTypeAccessor();
                break;
            case Accessor.REF_TYPE:
                // accessors[idx] = new T4CRefTypeAccessor();
                break;
            case Accessor.TIMESTAMP:
                // accessors[idx] = new T4CTimestampAccessor();
                break;
            case Accessor.TIMESTAMPTZ:
                // accessors[idx] = new T4CTimestamptzAccessor();
                break;
            case Accessor.TIMESTAMPLTZ:
                // accessors[idx] = new T4CTimestampltzAccessor();
                break;
            case Accessor.INTERVALYM:
                // accessors[idx] = new T4CIntervalymAccessor();
                break;
            case Accessor.INTERVALDS:
                // accessors[idx] = new T4CIntervaldsAccessor();
                break;
            default:
                throw new RuntimeException("unknown data type!");
        }

        if (accessors[i] != null) {
            accessors[i].init(oac);
        }
    }

    private void unmarshalBinds(T4CPacketBuffer meg) {
        short msgCode = meg.unmarshalUB1();
        if (msgCode == TTIRXD) {
            byte[][] tmp = new byte[numberOfParams][];
            byte[][] bigTmp = new byte[numberOfParams][];

            int m = 0, l = 0;
            for (int k = 0; k < numberOfParams; k++) {
                byte[] tmpBytes = meg.unmarshalCLRforREFS();
                if (tmpBytes != null && tmpBytes.length > 4000) {
                    bigTmp[m++] = tmpBytes;
                } else {
                    tmp[l++] = tmpBytes;
                }
            }

            int x = 0, y = 0;
            for (int i = 0; i < numberOfParams; i++) {
                if (oacBind[i].oacmxl > 4000) {
                    paramBytes[i] = bigTmp[x++];
                } else {
                    paramBytes[i] = tmp[y++];
                }
            }
            tmp = null;
            bigTmp = null;
        } else {
            throw new RuntimeException();
        }
    }

    private void parseOFETCH(T4CPacketBuffer meg) {
        if (logger.isDebugEnabled()) {
            System.out.println("type:T4CTTIfunPacket.OFETCH");
        }
    }

    private void parseOEXEC(T4CPacketBuffer meg) {
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
