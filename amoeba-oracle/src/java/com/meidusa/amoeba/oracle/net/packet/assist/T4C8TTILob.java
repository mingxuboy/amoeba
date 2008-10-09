package com.meidusa.amoeba.oracle.net.packet.assist;

import com.meidusa.amoeba.oracle.net.packet.T4C8OallResponseDataPacket;
import com.meidusa.amoeba.oracle.net.packet.T4CPacketBuffer;
import com.meidusa.amoeba.oracle.net.packet.T4CPacketBufferExchanger;

/**
 * lob数据解析包
 * 
 * @author hexianmao
 * @version 2008-9-27 下午09:45:04
 */
public class T4C8TTILob {

    public static final int LOB_OPS_BYTES      = 8;

    static final int        KPLOB_READ         = 2;
    static final int        KPLOB_WRITE        = 64;
    static final int        KPLOB_WRITE_APPEND = 8192;
    static final int        KPLOB_PAGE_SIZE    = 16384;
    static final int        KPLOB_FILE_OPEN    = 256;
    static final int        KPLOB_FILE_ISOPEN  = 1024;
    static final int        KPLOB_FILE_EXISTS  = 2048;
    static final int        KPLOB_FILE_CLOSE   = 512;
    static final int        KPLOB_OPEN         = 32768;
    static final int        KPLOB_CLOSE        = 0x10000;
    static final int        KPLOB_ISOPEN       = 0x11000;
    static final int        KPLOB_TMP_CREATE   = 272;
    static final int        KPLOB_TMP_FREE     = 273;
    static final int        KPLOB_GET_LEN      = 1;
    static final int        KPLOB_TRIM         = 32;
    static final int        KOKL_ORDONLY       = 1;
    static final int        KOKL_ORDWR         = 2;
    static final int        KOLF_ORDONLY       = 11;
    static final byte       KOLBLOPEN          = 8;
    static final byte       KOLBLTMP           = 1;
    static final byte       KOLBLRDWR          = 16;
    static final byte       KOLBLABS           = 64;
    static final byte       ALLFLAGS           = -1;
    static final byte       KOLBLFLGB          = 4;
    static final byte       KOLLFLG            = 4;
    static final byte       KOLL3FLG           = 7;
    static final byte       KOLBLVLE           = 64;
    static final int        DTYCLOB            = 112;
    static final int        DTYBLOB            = 113;

    byte[]                  sourceLobLocator;
    byte[]                  destinationLobLocator;
    int                     destinationLength;
    short                   characterSet;
    boolean                 hasCharacterSet;
    boolean                 sendLobamt;
    long                    lobamt;
    boolean                 nullO2U;
    int[]                   lobscn;
    int                     lobscnl;
    boolean                 hasLobscnl;
    long                    sourceOffset;
    long                    destinationOffset;
    long                    lobops;

    int                     rowsProcessed;
    long                    lobBytesRead;
    boolean                 varWidthChar;
    boolean                 littleEndianClob;
    byte[]                  outBuffer;
    boolean                 lobnull;
    T4C8TTILobd             lobd               = new T4C8TTILobd();
    T4CTTIoer               oer                = new T4CTTIoer();

    private int             sourceLobLocatorOffset;
    private boolean         isComplete;

    public int getSourceLobLocatorOffset() {
        return sourceLobLocatorOffset;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void marshal(T4CPacketBuffer meg) {
        int i = 0;
        if (sourceLobLocator != null) {
            i = sourceLobLocator.length;
            meg.marshalPTR();
        } else {
            meg.marshalNULLPTR();
        }
        meg.marshalSB4(i);

        if (destinationLobLocator != null) {
            destinationLength = destinationLobLocator.length;
            meg.marshalPTR();
        } else {
            meg.marshalNULLPTR();
        }
        meg.marshalSB4(destinationLength);

        if (meg.versionNumber >= 10000) {
            meg.marshalUB4(0L);
        } else {
            meg.marshalUB4(sourceOffset);
        }

        if (meg.versionNumber >= 10000) {
            meg.marshalUB4(0L);
        } else {
            meg.marshalUB4(destinationOffset);
        }

        if (characterSet != 0) {
            meg.marshalPTR();
        } else {
            meg.marshalNULLPTR();
        }

        if (sendLobamt && meg.versionNumber < 10000) {
            meg.marshalPTR();
        } else {
            meg.marshalNULLPTR();
        }

        if (nullO2U) {
            meg.marshalPTR();
        } else {
            meg.marshalNULLPTR();
        }

        meg.marshalUB4(lobops);

        if (lobscnl != 0) {
            meg.marshalPTR();
        } else {
            meg.marshalNULLPTR();
        }
        meg.marshalSB4(lobscnl);

        if (meg.versionNumber >= 10000) {
            meg.marshalSB8(sourceOffset);
            meg.marshalSB8(destinationOffset);
            if (sendLobamt) {
                meg.marshalPTR();
            } else {
                meg.marshalNULLPTR();
            }
        }
        if (sourceLobLocator != null) {
            meg.marshalB1Array(sourceLobLocator);
        }

        if (destinationLobLocator != null) {
            meg.marshalB1Array(destinationLobLocator);
        }

        if (characterSet != 0) {
            meg.marshalUB2(characterSet);
        }

        if (sendLobamt && meg.versionNumber < 10000) {
            meg.marshalUB4(lobamt);
        }

        if (lobscnl != 0) {
            for (int j = 0; j < lobscnl; j++) {
                meg.marshalUB4(lobscn[j]);
            }
        }

        if (sendLobamt && meg.versionNumber >= 10000) {
            meg.marshalSB8(lobamt);
        }
    }

    public void unmarshal(T4CPacketBuffer meg) {
        meg.unmarshalPTR();
        int i = meg.unmarshalSB4();
        if (i == 0) {
            sourceLobLocator = null;
        } else {
            sourceLobLocator = new byte[i];
        }

        meg.unmarshalPTR();
        destinationLength = meg.unmarshalSB4();
        if (destinationLength == 0) {
            destinationLobLocator = null;
        } else {
            destinationLobLocator = new byte[destinationLength];
        }

        if (meg.versionNumber >= 10000) {
            meg.unmarshalUB4();
        } else {
            sourceOffset = meg.unmarshalUB4();
        }

        if (meg.versionNumber >= 10000) {
            meg.unmarshalUB4();
        } else {
            destinationOffset = meg.unmarshalUB4();
        }

        if (meg.unmarshalSB1() != 0) {
            hasCharacterSet = true;
        }

        if (meg.unmarshalSB1() != 0) {
            sendLobamt = true;
        }

        if (meg.unmarshalSB1() != 0) {
            nullO2U = true;
        }

        lobops = meg.unmarshalUB4();

        meg.unmarshalPTR();// lobscnl
        lobscnl = meg.unmarshalSB4();

        if (meg.versionNumber >= 10000) {
            sourceOffset = meg.unmarshalSB8();
            destinationOffset = meg.unmarshalSB8();
            meg.unmarshalPTR();// sendLobamt
        }

        if (sourceLobLocator != null) {
            sourceLobLocatorOffset = meg.getPosition();
            sourceLobLocator = meg.unmarshalNBytes(sourceLobLocator.length);
        }

        if (destinationLobLocator != null) {
            destinationLobLocator = meg.unmarshalNBytes(destinationLength);
        }

        if (hasCharacterSet) {
            characterSet = (short) meg.unmarshalUB2();
        }

        if (sendLobamt && meg.versionNumber < 10000) {
            lobamt = meg.unmarshalUB4();
        }

        if (lobscnl != 0) {
            lobscn = new int[lobscnl];
            for (int j = 0; j < lobscnl; j++) {
                lobscn[j] = (int) meg.unmarshalUB4();
            }
        }

        if (sendLobamt && meg.versionNumber >= 10000) {
            lobamt = meg.unmarshalSB8();
        }

    }

    public void marshalResponse(T4CPacketBuffer meg) {
        if (isComplete) {
            meg.marshalSB1(T4C8OallResponseDataPacket.LOB_DATA);// 14
            meg.marshalCLR(outBuffer, 0, (int) lobamt, T4CPacketBuffer.TTCC_MXOUT);
        }
        meg.marshalSB1(T4C8OallResponseDataPacket.LOB_OPS);// 8
        marshalTTIRPA(meg);// RPA
        meg.marshalSB1(T4C8OallResponseDataPacket.QUERY_END);// 4
        oer.marshal(meg);
    }

    public void unmarshalResponse(T4CPacketBufferExchanger meg) {
        boolean flag = false;
        label0: do {
            byte byte0 = meg.unmarshalSB1();
            switch (byte0) {
                case 8:
                    unmarshalTTIRPA(meg);
                    continue;
                case 9:
                    break label0;
                case 14:// lob data
                    if (outBuffer == null) {
                        if (varWidthChar) {
                            outBuffer = new byte[(int) lobamt * 2];
                        } else {
                            outBuffer = new byte[(int) lobamt * 3];
                        }
                    }
                    if (!varWidthChar) {
                        lobBytesRead = lobd.unmarshalLobData(outBuffer, meg);
                    } else if (meg.versionNumber < 10101) {
                        lobBytesRead = lobd.unmarshalClobUB2(outBuffer, meg);
                    } else {
                        lobBytesRead = lobd.unmarshalLobData(outBuffer, meg);
                    }
                    if (!flag) {
                        flag = true;
                    }
                    continue;
                case 4:
                    oer.init();
                    oer.unmarshal(meg);
                    rowsProcessed = (int) oer.curRowNumber;
                    if (oer.retCode != 0) {
                        oer.processError();
                    }
                    if (flag) {
                        isComplete = true;
                    }
                    break label0;
                default:
                    throw new RuntimeException("protocol error!");
            }
        } while (true);
        return;
    }

    private void unmarshalTTIRPA(T4CPacketBufferExchanger meg) {
        if (sourceLobLocator != null) {
            meg.getNBytes(sourceLobLocator, 0, sourceLobLocator.length);
        }

        if (destinationLobLocator != null) {
            short word0 = meg.unmarshalSB2();
            destinationLobLocator = meg.unmarshalNBytes(word0);
        }

        if (characterSet != 0) {
            characterSet = meg.unmarshalSB2();
        }

        if (sendLobamt) {
            if (meg.versionNumber >= 10000) {
                lobamt = meg.unmarshalSB8();
            } else {
                lobamt = meg.unmarshalUB4();
            }
        }

        if (nullO2U) {
            short word1 = meg.unmarshalSB2();
            if (word1 != 0) {
                lobnull = true;
            }
        }

        if (lobscnl != 0) {
            for (int j = 0; j < lobscnl; j++) {
                lobscn[j] = meg.unmarshalSB4();
            }
        }
    }

    private void marshalTTIRPA(T4CPacketBuffer meg) {
        if (sourceLobLocator != null) {
            meg.marshalB1Array(sourceLobLocator);
        }

        if (destinationLobLocator != null) {
            meg.marshalSB2((short) destinationLobLocator.length);
            meg.marshalB1Array(destinationLobLocator);
        }

        if (characterSet != 0) {
            meg.marshalSB2(characterSet);
        }

        if (sendLobamt) {
            if (meg.versionNumber >= 10000) {
                meg.marshalSB8(lobamt);
            } else {
                meg.marshalUB4(lobamt);
            }
        }

        if (nullO2U) {
            if (lobnull) {
                meg.marshalSB2((short) 1);
            } else {
                meg.marshalSB2((short) 0);
            }
        }

        if (lobscnl != 0) {
            for (int j = 0; j < lobscnl; j++) {
                meg.marshalSB4(lobscn[j]);
            }
        }

    }

}
