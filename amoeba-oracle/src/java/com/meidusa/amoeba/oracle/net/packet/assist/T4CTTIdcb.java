package com.meidusa.amoeba.oracle.net.packet.assist;

import com.meidusa.amoeba.oracle.accessor.Accessor;
import com.meidusa.amoeba.oracle.net.packet.T4CPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-20 ÏÂÎç02:16:11
 */
public class T4CTTIdcb {

    T4C8TTIuds   uds[];
    int          numuds;
    String       colnames[];
    int          colOffset;
    byte         ignoreBuff[];
    StringBuffer colNameSB;

    public T4CTTIdcb(){
        // super(TTIDCB);
        ignoreBuff = new byte[40];
    }

    public void init(int colOffset) {
        this.colOffset = colOffset;
    }

    public Accessor[] receive(Accessor aaccessor[], T4CPacketBuffer meg) {
        short word0 = meg.unmarshalUB1();
        if (ignoreBuff.length < word0) {
            ignoreBuff = new byte[word0];
        }
        meg.unmarshalNBytes(ignoreBuff, 0, word0);
        meg.unmarshalUB4();// skip read
        aaccessor = receiveCommon(aaccessor, false, meg);
        return aaccessor;
    }

    public Accessor[] receiveCommon(Accessor aaccessor[], boolean flag, T4CPacketBuffer meg) {
        if (flag) {
            numuds = meg.unmarshalUB2();
        } else {
            numuds = (int) meg.unmarshalUB4();
            if (numuds > 0) {
                meg.unmarshalUB1();
            }
        }

        uds = new T4C8TTIuds[numuds];
        colnames = new String[numuds];
        for (int i = 0; i < numuds; i++) {
            uds[i] = new T4C8TTIuds(meg);
            uds[i].unmarshal();
            if (meg.versionNumber >= 10000) {
                meg.unmarshalUB2();
            }
            colnames[i] = meg.getConversion().CharBytesToString(uds[i].getColumName(), uds[i].getColumNameByteLength());
        }

        if (!flag) {
            meg.unmarshalDALC();
            if (meg.versionNumber >= 10000) {
                meg.unmarshalUB4();
                meg.unmarshalUB4();
            }
        }

        // if (statement.needToPrepareDefineBuffer) {
        // if (aaccessor == null || aaccessor.length != numuds + colOffset) {
        // Accessor aaccessor1[] = new Accessor[numuds + colOffset];
        // if (aaccessor != null && aaccessor.length == colOffset) {
        // System.arraycopy(aaccessor, 0, aaccessor1, 0, colOffset);
        // }
        // aaccessor = aaccessor1;
        // fillupAccessors(aaccessor, colOffset);
        // }
        // if (!flag) {
        // statement.describedWithNames = true;
        // statement.described = true;
        // statement.numberOfDefinePositions = numuds;
        // statement.accessors = aaccessor;
        // if (statement.connection.useFetchSizeWithLongColumn) {
        // statement.prepareAccessors();
        // statement.allocateTmpByteArray();
        // }
        // }
        // }
        return aaccessor;
    }

}
