package com.meidusa.amoeba.oracle.util;

import java.sql.SQLException;

import com.meidusa.amoeba.oracle.charset.CharacterSet;
import com.meidusa.amoeba.oracle.charset.CharacterSetMetaData;

public class DBConversion {

    public static final boolean DO_CONVERSION_WITH_REPLACEMENT = true;
    public static final short   ORACLE8_PROD_VERSION           = 8030;
    public static final short   DBCS_CHARSET                   = -1;
    public static final short   UCS2_CHARSET                   = -5;
    public static final short   ASCII_CHARSET                  = 1;
    public static final short   ISO_LATIN_1_CHARSET            = 31;
    public static final short   AL24UTFFSS_CHARSET             = 870;
    public static final short   UTF8_CHARSET                   = 871;
    public static final short   AL32UTF8_CHARSET               = 873;
    public static final short   AL16UTF16_CHARSET              = 2000;

    public boolean              isServerCSMultiByte;
    public int                  c2sNlsRatio;

    protected short             serverNCharSetId;
    protected short             serverCharSetId;
    protected short             clientCharSetId;
    protected CharacterSet      serverCharSet;
    protected CharacterSet      serverNCharSet;
    protected CharacterSet      clientCharSet;
    protected CharacterSet      asciiCharSet;
    protected boolean           isServerCharSetFixedWidth;
    protected boolean           isServerNCharSetFixedWidth;
    protected int               s2cNlsRatio;
    protected int               sMaxCharSize;
    protected int               cMaxCharSize;
    protected int               maxNCharSize;

    // class UnicodeStream extends OracleBufferedStream {
    //
    // public boolean needBytes() {
    // return !closed && pos < count;
    // }
    //
    // UnicodeStream(char ac[], int i, int j){
    // super(j);
    // int k = i;
    // for (int l = 0; l < j;) {
    // char c = ac[k++];
    // buf[l++] = (byte) (c >> 8 & 0xff);
    // buf[l++] = (byte) (c & 0xff);
    // }
    //
    // count = j;
    // }
    // }

    // class AsciiStream extends OracleBufferedStream {
    //
    // public boolean needBytes() {
    // return !closed && pos < count;
    // }
    //
    // AsciiStream(char ac[], int i, int j){
    // super(j);
    // int k = i;
    // for (int l = 0; l < j; l++)
    // buf[l] = (byte) ac[k++];
    //
    // count = j;
    // }
    // }

    public DBConversion(short word0, short word1, short word2) throws SQLException{
        switch (word1) {
            default:
                unexpectedCharset(word1);
                // fall through

            case -5:
            case -1:
            case 1: // '\001'
            case 2: // '\002'
            case 31: // '\037'
            case 178:
            case 870:
            case 871:
            case 873:
                serverCharSetId = word0;
                break;
        }
        clientCharSetId = word1;
        serverCharSet = CharacterSet.make(serverCharSetId);
        serverNCharSetId = word2;
        serverNCharSet = CharacterSet.make(serverNCharSetId);
        if (word1 == -1) {
            clientCharSet = serverCharSet;
        } else {
            clientCharSet = CharacterSet.make(clientCharSetId);
        }
        c2sNlsRatio = CharacterSetMetaData.getRatio(word0, word1);
        s2cNlsRatio = CharacterSetMetaData.getRatio(word1, word0);
        sMaxCharSize = CharacterSetMetaData.getRatio(word0, 1);
        cMaxCharSize = CharacterSetMetaData.getRatio(word1, 1);
        maxNCharSize = CharacterSetMetaData.getRatio(word2, 1);
        isServerCSMultiByte = CharacterSetMetaData.getRatio(word0, 1) != 1;
        isServerCharSetFixedWidth = CharacterSetMetaData.isFixedWidth(word0);
        isServerNCharSetFixedWidth = CharacterSetMetaData.isFixedWidth(word2);
    }

    public short getServerCharSetId() {
        return serverCharSetId;
    }

    public short getNCharSetId() {
        return serverNCharSetId;
    }

    public boolean IsNCharFixedWith() {
        return serverNCharSetId == 2000;
    }

    public short getClientCharSet() {
        if (clientCharSetId == -1) return serverCharSetId;
        else return clientCharSetId;
    }

    public CharacterSet getDbCharSetObj() {
        return serverCharSet;
    }

    public CharacterSet getDriverCharSetObj() {
        return clientCharSet;
    }

    public CharacterSet getDriverNCharSetObj() {
        return serverNCharSet;
    }

    public static final short findDriverCharSet(short word0, short word1) {
        short word2 = 0;
        switch (word0) {
            case 1: // '\001'
            case 2: // '\002'
            case 31: // '\037'
            case 178:
            case 873:
                word2 = word0;
                break;

            default:
                word2 = word1 < 8030 ? (short) 870 : (short) 871;
                break;
        }
        return word2;
    }

    public static final byte[] stringToDriverCharBytes(String s, short word0) {
        if (s == null) return null;
        byte abyte0[] = null;
        switch (word0) {
            case -5:
            case 2000:
                abyte0 = CharacterSet.stringToAL16UTF16Bytes(s);
                break;

            case 1: // '\001'
            case 2: // '\002'
            case 31: // '\037'
            case 178:
                abyte0 = CharacterSet.stringToASCII(s);
                break;

            case 870:
            case 871:
                abyte0 = CharacterSet.stringToUTF(s);
                break;

            case 873:
                abyte0 = CharacterSet.stringToAL32UTF8(s);
                break;

            case -1:
            default:
                unexpectedCharset(word0);
                break;
        }
        return abyte0;
    }

    public byte[] StringToCharBytes(String s) {
        if (s.length() == 0) return null;
        if (clientCharSetId == -1) return serverCharSet.convertWithReplacement(s);
        else return stringToDriverCharBytes(s, clientCharSetId);
    }

    public String CharBytesToString(byte abyte0[], int i) {
        return CharBytesToString(abyte0, i, false);
    }

    @SuppressWarnings("deprecation")
    public String CharBytesToString(byte abyte0[], int i, boolean flag) {
        String s = null;
        if (abyte0.length == 0) return s;
        switch (clientCharSetId) {
            case -5:
                s = CharacterSet.AL16UTF16BytesToString(abyte0, i);
                break;

            case 1: // '\001'
            case 2: // '\002'
            case 31: // '\037'
            case 178:
                s = new String(abyte0, 0, 0, i);
                break;

            case 870:
            case 871:
                s = CharacterSet.UTFToString(abyte0, 0, i, flag);
                break;

            case 873:
                s = CharacterSet.AL32UTF8ToString(abyte0, 0, i, flag);
                break;

            case -1:
                s = serverCharSet.toStringWithReplacement(abyte0, 0, i);
                break;

            default:
                unexpectedCharset(clientCharSetId);
                break;
        }
        return s;
    }

    @SuppressWarnings("deprecation")
    public String NCharBytesToString(byte abyte0[], int i) {
        String s = null;
        if (clientCharSetId == -1) s = serverNCharSet.toStringWithReplacement(abyte0, 0, i);
        else switch (serverNCharSetId) {
            case -5:
            case 2000:
                s = CharacterSet.AL16UTF16BytesToString(abyte0, i);
                break;

            case 1: // '\001'
            case 2: // '\002'
            case 31: // '\037'
            case 178:
                s = new String(abyte0, 0, 0, i);
                break;

            case 870:
            case 871:
                s = CharacterSet.UTFToString(abyte0, 0, i);
                break;

            case 873:
                s = CharacterSet.AL32UTF8ToString(abyte0, 0, i);
                break;

            case -1:
                s = serverCharSet.toStringWithReplacement(abyte0, 0, i);
                break;

            default:
                unexpectedCharset(clientCharSetId);
                break;
        }
        return s;
    }

    public int javaCharsToCHARBytes(char ac[], int i, byte abyte0[]) throws SQLException {
        return javaCharsToCHARBytes(ac, i, abyte0, clientCharSetId);
    }

    public int javaCharsToCHARBytes(char ac[], int i, byte abyte0[], int j, int k) throws SQLException {
        return javaCharsToCHARBytes(ac, i, abyte0, j, clientCharSetId, k);
    }

    public int javaCharsToNCHARBytes(char ac[], int i, byte abyte0[]) throws SQLException {
        return javaCharsToCHARBytes(ac, i, abyte0, serverNCharSetId);
    }

    public int javaCharsToNCHARBytes(char ac[], int i, byte abyte0[], int j, int k) throws SQLException {
        return javaCharsToCHARBytes(ac, i, abyte0, j, serverNCharSetId, k);
    }

    protected int javaCharsToCHARBytes(char ac[], int i, byte abyte0[], short word0) throws SQLException {
        return javaCharsToCHARBytes(ac, 0, abyte0, 0, word0, i);
    }

    protected int javaCharsToCHARBytes(char ac[], int i, byte abyte0[], int j, short word0, int k) throws SQLException {
        int l = 0;
        switch (word0) {
            case -5:
            case 2000:
                l = CharacterSet.convertJavaCharsToAL16UTF16Bytes(ac, i, abyte0, j, k);
                break;

            case 2: // '\002'
            case 178:
                byte abyte1[] = new byte[k];
                abyte1 = clientCharSet.convertWithReplacement(new String(ac, i, k));
                System.arraycopy(abyte1, 0, abyte0, 0, abyte1.length);
                l = abyte1.length;
                break;

            case 1: // '\001'
                l = CharacterSet.convertJavaCharsToASCIIBytes(ac, i, abyte0, j, k);
                break;

            case 31: // '\037'
                l = CharacterSet.convertJavaCharsToISOLATIN1Bytes(ac, i, abyte0, j, k);
                break;

            case 870:
            case 871:
                l = CharacterSet.convertJavaCharsToUTFBytes(ac, i, abyte0, j, k);
                break;

            case 873:
                l = CharacterSet.convertJavaCharsToAL32UTF8Bytes(ac, i, abyte0, j, k);
                break;

            case -1:
                l = javaCharsToDbCsBytes(ac, i, abyte0, j, k);
                break;

            default:
                unexpectedCharset(clientCharSetId);
                break;
        }
        return l;
    }

    public int CHARBytesToJavaChars(byte abyte0[], int i, char ac[], int j, int ai[], int k) throws SQLException {
        return _CHARBytesToJavaChars(abyte0, i, ac, j, clientCharSetId, ai, k, serverCharSet, serverNCharSet, clientCharSet, false);
    }

    public int NCHARBytesToJavaChars(byte abyte0[], int i, char ac[], int j, int ai[], int k) throws SQLException {
        return _CHARBytesToJavaChars(abyte0, i, ac, j, serverNCharSetId, ai, k, serverCharSet, serverNCharSet, clientCharSet, true);
    }

    static final int _CHARBytesToJavaChars(byte abyte0[], int i, char ac[], int j, short word0, int ai[], int k,
                                           CharacterSet characterset, CharacterSet characterset1,
                                           CharacterSet characterset2, boolean flag) throws SQLException {
        int l = 0;
        switch (word0) {
            case -5:
            case 2000:
                int i1 = ai[0] - ai[0] % 2;
                if (k > ac.length - j) k = ac.length - j;
                if (k * 2 < i1) i1 = k * 2;
                l = CharacterSet.convertAL16UTF16BytesToJavaChars(abyte0, i, ac, j, i1, true);
                ai[0] = ai[0] - i1;
                break;

            case 1: // '\001'
            case 2: // '\002'
            case 31: // '\037'
            case 178:
                int j1 = ai[0];
                if (k > ac.length - j) k = ac.length - j;
                if (k < j1) j1 = k;
                l = CharacterSet.convertASCIIBytesToJavaChars(abyte0, i, ac, j, j1);
                ai[0] = ai[0] - j1;
                break;

            case 870:
            case 871:
                if (k > ac.length - j) k = ac.length - j;
                l = CharacterSet.convertUTFBytesToJavaChars(abyte0, i, ac, j, ai, true, k);
                break;

            case 873:
                if (k > ac.length - j) k = ac.length - j;
                l = CharacterSet.convertAL32UTF8BytesToJavaChars(abyte0, i, ac, j, ai, true, k);
                break;

            case -1:
                l = dbCsBytesToJavaChars(abyte0, i, ac, j, ai[0], characterset);
                ai[0] = 0;
                break;

            default:
                CharacterSet characterset3 = characterset2;
                if (flag) characterset3 = characterset1;
                String s = characterset3.toString(abyte0, i, ai[0]);
                char ac1[] = s.toCharArray();
                int k1 = ac1.length;
                if (k1 > k) k1 = k;
                System.arraycopy(ac1, 0, ac, j, k1);
                break;
        }
        return l;
    }

    public byte[] asciiBytesToCHARBytes(byte abyte0[]) {
        byte abyte1[] = null;
        switch (clientCharSetId) {
            case -5:
                abyte1 = new byte[abyte0.length * 2];
                int i = 0;
                int j = 0;
                for (; i < abyte0.length; i++) {
                    abyte1[j++] = 0;
                    abyte1[j++] = abyte0[i];
                }

                break;

            case -1:
                if (asciiCharSet == null) asciiCharSet = CharacterSet.make(1);
                try {
                    abyte1 = serverCharSet.convert(asciiCharSet, abyte0, 0, abyte0.length);
                } catch (SQLException sqlexception) {
                }
                break;

            default:
                abyte1 = abyte0;
                break;
        }
        return abyte1;
    }

    public int javaCharsToDbCsBytes(char ac[], int i, byte abyte0[]) throws SQLException {
        int j = javaCharsToDbCsBytes(ac, 0, abyte0, 0, i);
        return j;
    }

    public int javaCharsToDbCsBytes(char ac[], int i, byte abyte0[], int j, int k) throws SQLException {
        int l = 0;
        catchCharsLen(ac, i, k);
        String s = new String(ac, i, k);
        byte abyte1[] = serverCharSet.convertWithReplacement(s);
        s = null;
        if (abyte1 != null) {
            l = abyte1.length;
            catchBytesLen(abyte0, j, l);
            System.arraycopy(abyte1, 0, abyte0, j, l);
            abyte1 = null;
        }
        return l;
    }

    static final int dbCsBytesToJavaChars(byte abyte0[], int i, char ac[], int j, int k, CharacterSet characterset)
                                                                                                                   throws SQLException {
        int l = 0;
        catchBytesLen(abyte0, i, k);
        String s = characterset.toStringWithReplacement(abyte0, i, k);
        if (s != null) {
            l = s.length();
            catchCharsLen(ac, j, l);
            s.getChars(0, l, ac, j);
            s = null;
        }
        return l;
    }

    public static final int javaCharsToUcs2Bytes(char ac[], int i, byte abyte0[]) throws SQLException {
        int j = javaCharsToUcs2Bytes(ac, 0, abyte0, 0, i);
        return j;
    }

    public static final int javaCharsToUcs2Bytes(char ac[], int i, byte abyte0[], int j, int k) throws SQLException {
        catchCharsLen(ac, i, k);
        catchBytesLen(abyte0, j, k * 2);
        int j1 = k + i;
        int l = i;
        int i1 = j;
        for (; l < j1; l++) {
            abyte0[i1++] = (byte) (ac[l] >> 8 & 0xff);
            abyte0[i1++] = (byte) (ac[l] & 0xff);
        }

        return i1 - j;
    }

    public static final int ucs2BytesToJavaChars(byte abyte0[], int i, char ac[]) throws SQLException {
        return CharacterSet.AL16UTF16BytesToJavaChars(abyte0, i, ac);
    }

    public static final byte[] stringToAsciiBytes(String s) {
        return CharacterSet.stringToASCII(s);
    }

    public static final int asciiBytesToJavaChars(byte abyte0[], int i, char ac[]) throws SQLException {
        return CharacterSet.convertASCIIBytesToJavaChars(abyte0, 0, ac, 0, i);
    }

    public static final int javaCharsToAsciiBytes(char ac[], int i, byte abyte0[]) throws SQLException {
        return CharacterSet.convertJavaCharsToASCIIBytes(ac, 0, abyte0, 0, i);
    }

    public static final boolean isCharSetMultibyte(short word0) {
        switch (word0) {
            case 1: // '\001'
            case 31: // '\037'
                return false;

            case -5:
            case -1:
            case 870:
            case 871:
            case 873:
                return true;
        }
        return false;
    }

    public int getMaxCharbyteSize() {
        return _getMaxCharbyteSize(clientCharSetId);
    }

    public int getMaxNCharbyteSize() {
        return _getMaxCharbyteSize(serverNCharSetId);
    }

    public int _getMaxCharbyteSize(short word0) {
        switch (word0) {
            case 1: // '\001'
                return 1;

            case 31: // '\037'
                return 1;

            case 870:
            case 871:
                return 3;

            case -5:
            case 2000:
                return 2;

            case -1:
                return 4;

            case 873:
                return 4;
        }
        return 1;
    }

    public boolean isUcs2CharSet() {
        return clientCharSetId == -5;
    }

    public static final int RAWBytesToHexChars(byte abyte0[], int i, char ac[]) {
        int j = 0;
        int k = 0;
        for (; j < i; j++) {
            ac[k++] = (char) RepConversion.nibbleToHex((byte) (abyte0[j] >> 4 & 0xf));
            ac[k++] = (char) RepConversion.nibbleToHex((byte) (abyte0[j] & 0xf));
        }

        return k;
    }

    // public InputStream ConvertStream(InputStream inputstream, int i) {
    // return new OracleConversionInputStream(this, inputstream, i);
    // }
    //
    // public InputStream ConvertStream(InputStream inputstream, int i, int j) {
    // return new OracleConversionInputStream(this, inputstream, i, j);
    // }
    //
    // public InputStream ConvertStream(Reader reader, int i, int j, short word0) {
    // OracleConversionInputStream oracleconversioninputstream = new OracleConversionInputStream(this, reader, i, j,
    // word0);
    // return oracleconversioninputstream;
    // }
    //
    // public Reader ConvertCharacterStream(InputStream inputstream, int i) throws SQLException {
    // return new OracleConversionReader(this, inputstream, i);
    // }
    //
    // public Reader ConvertCharacterStream(InputStream inputstream, int i, short word0) throws SQLException {
    // OracleConversionReader oracleconversionreader = new OracleConversionReader(this, inputstream, i);
    // oracleconversionreader.setFormOfUse(word0);
    // return oracleconversionreader;
    // }
    //
    // public InputStream CharsToStream(char ac[], int i, int j, int k) throws SQLException {
    // if (k == 10)
    // return new AsciiStream(ac, i, j);
    // if (k == 11) {
    // return new UnicodeStream(ac, i, j);
    // } else {
    // DatabaseError.throwSqlException(39, "unknownConversion");
    // return null;
    // }
    // }

    static final void unexpectedCharset(short word0) {
        throw new RuntimeException("code:35 DBConversion");
        // DatabaseError.throwSqlException(35, "DBConversion");
    }

    protected static final void catchBytesLen(byte abyte0[], int i, int j) throws SQLException {
        if (i + j > abyte0.length) {
            // DatabaseError.throwSqlException(39, "catchBytesLen");
            throw new RuntimeException("code:39 catchBytesLen");
        }
    }

    protected static final void catchCharsLen(char ac[], int i, int j) throws SQLException {
        if (i + j > ac.length) {
            // DatabaseError.throwSqlException(39, "catchCharsLen");
            throw new RuntimeException("code:39 catchBytesLen");
        }
    }

    public static final int getUtfLen(char c) {
        byte byte0 = 0;
        if ((c & 0xff80) == 0) byte0 = 1;
        else if ((c & 0xf800) == 0) byte0 = 2;
        else byte0 = 3;
        return byte0;
    }

    int encodedByteLength(String s, boolean flag) {
        int i = 0;
        if (s != null) {
            i = s.length();
            if (i != 0) if (flag) i = isServerNCharSetFixedWidth ? i * maxNCharSize : serverNCharSet.encodedByteLength(s);
            else i = isServerCharSetFixedWidth ? i * sMaxCharSize : serverCharSet.encodedByteLength(s);
        }
        return i;
    }

    int encodedByteLength(char ac[], boolean flag) {
        int i = 0;
        if (ac != null) {
            i = ac.length;
            if (i != 0) if (flag) i = isServerNCharSetFixedWidth ? i * maxNCharSize : serverNCharSet.encodedByteLength(ac);
            else i = isServerCharSetFixedWidth ? i * sMaxCharSize : serverCharSet.encodedByteLength(ac);
        }
        return i;
    }

}
