package com.meidusa.amoeba.oracle.net.packet.assist;


/**
 * @author hexianmao
 * @version 2008-8-20 ÏÂÎç02:16:11
 */
public class T4CTTIdcb {

    int  colOffset;
    byte ignoreBuff[];
    int  numuds;

    public T4CTTIdcb(){
        // super(TTIDCB);
    }

    public void init(int colOffset) {
        this.colOffset = colOffset;
    }

//    Accessor[] receive(Accessor aaccessor[], T4CPacketBuffer meg) {
//        short word0 = meg.unmarshalUB1();
//        if (ignoreBuff.length < word0) {
//            ignoreBuff = new byte[word0];
//        }
//        meg.unmarshalNBytes(ignoreBuff, 0, word0);
//        int i = (int) meg.unmarshalUB4();
//        aaccessor = receiveCommon(aaccessor, false, meg);
//        return aaccessor;
//    }

//    Accessor[] receiveCommon(Accessor aaccessor[], boolean flag, T4CPacketBuffer meg) {
//        // TODO ÐèÒª×ÐÏ¸ÔÄ¶Á
//        if (flag) {
//            numuds = meg.unmarshalUB2();
//        } else {
//            numuds = (int) meg.unmarshalUB4();
//            if (numuds > 0) {
//                meg.unmarshalUB1();
//            }
//        }
//        uds = new T4C8TTIuds[numuds];
//        colnames = new String[numuds];
//        for (int i = 0; i < numuds; i++) {
//            uds[i] = new T4C8TTIuds(meg);
//            uds[i].unmarshal();
//            int k;
//            if (meg.versionNumber >= 10000) {
//                k = meg.unmarshalUB2();
//            }
//            colnames[i] = meg.conv.CharBytesToString(uds[i].getColumName(), uds[i].getColumNameByteLength());
//        }
//
//        if (!flag) {
//            meg.unmarshalDALC();
//            if (meg.versionNumber >= 10000) {
//                int j = (int) meg.unmarshalUB4();
//                int l = (int) meg.unmarshalUB4();
//            }
//        }
//        if (statement.needToPrepareDefineBuffer) {
//            if (aaccessor == null || aaccessor.length != numuds + colOffset) {
//                Accessor aaccessor1[] = new Accessor[numuds + colOffset];
//                if (aaccessor != null && aaccessor.length == colOffset) {
//                    System.arraycopy(aaccessor, 0, aaccessor1, 0, colOffset);
//                }
//                aaccessor = aaccessor1;
//                fillupAccessors(aaccessor, colOffset);
//            }
//            if (!flag) {
//                statement.describedWithNames = true;
//                statement.described = true;
//                statement.numberOfDefinePositions = numuds;
//                statement.accessors = aaccessor;
//                if (statement.connection.useFetchSizeWithLongColumn) {
//                    statement.prepareAccessors();
//                    statement.allocateTmpByteArray();
//                }
//            }
//        }
//        return aaccessor;
//    }

}
