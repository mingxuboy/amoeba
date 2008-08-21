package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.accessor.Accessor;

/**
 * @author hexianmao
 * @version 2008-8-20 下午03:02:04
 */
public class T4C8OallDataPacket extends T4CTTIfunPacket {

    long                   options;
    int                    cursor;
    int                    sqlStmtLength;
    int                    numberOfBindPositions;
    int                    defCols;

    byte[]                 sqlStmt;
    final long[]           al8i4 = new long[13];
    T4CTTIoac[]            oacdefBindsSent;
    T4CTTIoac[]            oacdefDefines;

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
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        if (funCode == OFETCH) {
            ofetch = new T4CTTIofetchDataPacket();
            //ofetch.init(buffer);
            this.cursor = ofetch.cursor;
            this.al8i4[1] = ofetch.al8i4_1;
        } else if (funCode == OEXEC) {
            oexec = new T4CTTIoexecDataPacket();
            this.cursor = oexec.cursor;
            this.al8i4[1] = oexec.al8i4_1;
            // int[] binds = null;
            // TODO ...
            throw new RuntimeException("is not yet support");
        } else if (funCode == OALL8) {
            T4CPacketBuffer meg = (T4CPacketBuffer) buffer;

            unmarshalPisdef(meg);

            sqlStmt = meg.unmarshalCHR(sqlStmtLength);

            meg.unmarshalUB4Array(al8i4);

            unmarshalBindsTypes(meg);

            if (T4CPacketBuffer.versionNumber >= 9000 && defCols > 0) {
                oacdefDefines = new T4CTTIoac[defCols];
                for (int i = 0; i < defCols; i++) {
                    oacdefDefines[i] = new T4CTTIoac(meg);
                    oacdefDefines[i].unmarshal();
                }
            }

            unmarshalBinds(meg);
        } else {
            throw new RuntimeException("违反协议");
        }

    }

    @SuppressWarnings("unused")
    void unmarshalPisdef(T4CPacketBuffer meg) {
        options = meg.unmarshalUB4();
        cursor = meg.unmarshalSWORD();
        meg.unmarshalPTR();
        sqlStmtLength = meg.unmarshalSWORD();
        meg.unmarshalPTR();
        int al8i4Length = meg.unmarshalSWORD();
        meg.unmarshalPTR();
        meg.unmarshalPTR();
        meg.unmarshalUB4();
        meg.unmarshalUB4();
        long l = meg.unmarshalUB4();

        meg.unmarshalPTR();
        numberOfBindPositions = meg.unmarshalSWORD();

        meg.unmarshalPTR();
        meg.unmarshalPTR();
        meg.unmarshalPTR();
        meg.unmarshalPTR();
        meg.unmarshalPTR();

        if (T4CPacketBuffer.versionNumber >= 9000) {
            meg.unmarshalPTR();
            defCols = meg.unmarshalSWORD();
        }
    }

    void unmarshalBindsTypes(T4CPacketBuffer meg) {
        if (numberOfBindPositions <= 0) {
            return;
        }
        oacdefBindsSent = new T4CTTIoac[numberOfBindPositions];
        for (int i = 0; i < numberOfBindPositions; i++) {
            oacdefBindsSent[i] = new T4CTTIoac(meg);
            oacdefBindsSent[i].unmarshal();
        }
    }

    void unmarshalBinds(T4CPacketBuffer meg) {
        if (numberOfBindPositions <= 0) {
            return;
        }
        byte[][] ab = new byte[numberOfBindPositions][];
        for (int i = 0; i < numberOfBindPositions; i++) {
            ab[i] = meg.unmarshalDALC();
        }

    }

    void fillupAccessors(Accessor[] aaccessor, int offset) {
        int ai[] = null;// statement.definedColumnType;
        int ai1[] = null;// statement.definedColumnSize;
        int ai2[] = null;// statement.definedColumnFormOfUse;
        for (int i = 0; i < numberOfBindPositions; i++) {
            int l1 = 0;
            int i2 = 0;
            int j2 = 0;
            if (ai != null && ai.length > offset + i && ai[offset + i] != 0) {
                l1 = ai[offset + i];
            }
            if (ai1 != null && ai1.length > offset + i && ai1[offset + i] > 0) {
                i2 = ai1[offset + i];
            }
            if (ai2 != null && ai2.length > offset + i && ai2[offset + i] > 0) {
                j2 = ai2[offset + i];
            }
            T4CTTIoac t4c8ttiuds = oacdefBindsSent[i];
            String s = null;// meg.conv.CharBytesToString(oacdefBindsSent[i].getTypeName(),
                            // oacdefBindsSent[i].getTypeCharLength());
            String s1 = null;// meg.conv.CharBytesToString(oacdefBindsSent[i].getSchemaName(),
                             // oacdefBindsSent[i].getSchemaCharLength());
            String s2 = s1 + "." + s;
            int j = t4c8ttiuds.oacmxl;
            // switch (t4c8ttiuds.oacdty) {
            // case 96:
            // if (t4c8ttiuds.oacmxlc != 0 && t4c8ttiuds.oacmxlc < j) {
            // j = 2 * t4c8ttiuds.oacmxlc;
            // }
            // int k = j;
            // if ((l1 == 1 || l1 == 12) && i2 > 0 && i2 < j) {
            // k = i2;
            // }
            // aaccessor[offset + i] = new T4CCharAccessor(statement, k, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal, t4c8ttiuds.formOfUse,
            // j, l1, i2, meg);
            // if ((t4c8ttiuds.oacfl2 & 0x1000) == 4096 || t4c8ttiuds.oacmxlc != 0) {
            // aaccessor[colOffset + i].setDisplaySize(t4c8ttiuds.oacmxlc);
            // }
            // break;
            //
            // case 2:
            // aaccessor[offset + i] = new T4CNumberAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // break;
            //
            // case 1:
            // if (t4c8ttiuds.oacmxlc != 0 && t4c8ttiuds.oacmxlc < j) {
            // j = 2 * t4c8ttiuds.oacmxlc;
            // }
            // int l = j;
            // if ((l1 == 1 || l1 == 12) && i2 > 0 && i2 < j) {
            // l = i2;
            // }
            // aaccessor[offset + i] = new T4CVarcharAccessor(statement, l, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, j, l1, i2, meg);
            // if ((t4c8ttiuds.oacfl2 & 0x1000) == 4096 || t4c8ttiuds.oacmxlc != 0) {
            // aaccessor[colOffset + i].setDisplaySize(t4c8ttiuds.oacmxlc);
            // }
            // break;
            //
            // case 8:
            // if ((l1 == 1 || l1 == 12) && meg.versionNumber >= 9000 && i2 < 4001) {
            // int i1;
            // if (i2 > 0) i1 = i2;
            // else i1 = 4000;
            // j = -1;
            // aaccessor[offset + i] = new T4CVarcharAccessor(statement, i1, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, j, l1, i2, meg);
            // aaccessor[offset + i].describeType = 8;
            // } else {
            // j = 0;
            // aaccessor[offset + i] = new T4CLongAccessor(statement, offset + i + 1, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // }
            // break;
            //
            // case 6:
            // aaccessor[offset + i] = new T4CVarnumAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // break;
            //
            // case 100:
            // aaccessor[offset + i] = new T4CBinaryFloatAccessor(statement, 4, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // break;
            //
            // case 101:
            // aaccessor[offset + i] = new T4CBinaryDoubleAccessor(statement, 8, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // break;
            //
            // case 23:
            // aaccessor[offset + i] = new T4CRawAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal, t4c8ttiuds.formOfUse,
            // l1, i2, meg);
            // break;
            //
            // case 24:
            // if (l1 == -2 && i2 < 2001 && meg.versionNumber >= 9000) {
            // j = -1;
            // aaccessor[offset + i] = new T4CRawAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // aaccessor[offset + i].describeType = 24;
            // } else {
            // aaccessor[offset + i] = new T4CLongRawAccessor(statement, offset + i + 1, j,
            // t4c8ttiuds.udsnull, t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // }
            // break;
            //
            // case 104:
            // case 208:
            // aaccessor[offset + i] = new T4CRowidAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal, t4c8ttiuds.formOfUse,
            // l1, i2, meg);
            // if (t4c8ttiuds.oacdty == 208) {
            // aaccessor[i].describeType = t4c8ttiuds.oacdty;
            // }
            // break;
            //
            // case 102:
            // aaccessor[offset + i] = new T4CResultSetAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // break;
            //
            // case 12:
            // aaccessor[offset + i] = new T4CDateAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal, t4c8ttiuds.formOfUse,
            // l1, i2, meg);
            // break;
            //
            // case 113:
            // if (l1 == -4 && meg.versionNumber >= 9000) {
            // aaccessor[offset + i] = new T4CLongRawAccessor(statement, offset + i + 1, 0x7fffffff,
            // t4c8ttiuds.udsnull, t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // aaccessor[offset + i].describeType = 113;
            // } else if (l1 == -3 && meg.versionNumber >= 9000) {
            // aaccessor[offset + i] = new T4CRawAccessor(statement, 4000, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // aaccessor[offset + i].describeType = 113;
            // } else {
            // aaccessor[offset + i] = new T4CBlobAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // }
            // break;
            //
            // case 112:
            // short word0 = 1;
            // if (j2 != 0) {
            // word0 = (short) j2;
            // }
            // if (l1 == -1 && meg.versionNumber >= 9000) {
            // j = 0;
            // aaccessor[offset + i] = new T4CLongAccessor(statement, offset + i + 1, 0x7fffffff,
            // t4c8ttiuds.udsnull, t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre, t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2, t4c8ttiuds.oacmal,
            // word0, l1, i2, meg);
            // aaccessor[offset + i].describeType = 112;
            // } else if ((l1 == 12 || l1 == 1) && meg.versionNumber >= 9000) {
            // int j1 = 4000;
            // if (i2 > 0 && i2 < j1) {
            // j1 = i2;
            // }
            // aaccessor[offset + i] = new T4CVarcharAccessor(statement, j1, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal, word0, 4000, l1, i2,
            // meg);
            // aaccessor[offset + i].describeType = 112;
            // } else {
            // aaccessor[offset + i] = new T4CClobAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // }
            // break;
            //
            // case 114:
            // aaccessor[offset + i] = new T4CBfileAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal, t4c8ttiuds.formOfUse,
            // l1, i2, meg);
            // break;
            //
            // case 109:
            // aaccessor[offset + i] = new T4CNamedTypeAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, s2, l1, i2, meg);
            // break;
            //
            // case 111:
            // aaccessor[offset + i] = new T4CRefTypeAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg, t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl, t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, s2, l1, i2, meg);
            // break;
            //
            // case 180:
            // aaccessor[offset + i] = new T4CTimestampAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // break;
            //
            // case 181:
            // aaccessor[offset + i] = new T4CTimestamptzAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // break;
            //
            // case 231:
            // aaccessor[offset + i] = new T4CTimestampltzAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // break;
            //
            // case 182:
            // aaccessor[offset + i] = new T4CIntervalymAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // break;
            //
            // case 183:
            // aaccessor[offset + i] = new T4CIntervaldsAccessor(statement, j, t4c8ttiuds.udsnull,
            // t4c8ttiuds.oacflg,
            // t4c8ttiuds.oacpre,
            // t4c8ttiuds.oacscl,
            // t4c8ttiuds.oacfl2,
            // t4c8ttiuds.oacmal,
            // t4c8ttiuds.formOfUse, l1, i2, meg);
            // break;
            //
            // default:
            // aaccessor[offset + i] = null;
            // break;
            // }
            // if (t4c8ttiuds.oactoid.length > 0) {
            // aaccessor[offset + i].internalOtype = new OracleTypeADT(t4c8ttiuds.oactoid,
            // t4c8ttiuds.oacvsn,
            // t4c8ttiuds.ncs,
            // t4c8ttiuds.formOfUse, s1 + "." + s);
            // } else {
            // aaccessor[offset + i].internalOtype = null;
            // }
            // aaccessor[offset + i].columnName = colnames[i];
            // if (uds[i].udsoac.oacmxl == 0) {
            // aaccessor[i].isNullByDescribe = true;
            // }
        }

        // colNameSB = null;
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
    }

}
