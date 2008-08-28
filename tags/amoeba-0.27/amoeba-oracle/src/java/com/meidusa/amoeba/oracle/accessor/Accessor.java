package com.meidusa.amoeba.oracle.accessor;

public abstract class Accessor {

    public static final int VARCHAR            = 1;
    public static final int NUMBER             = 2;
    public static final int VARNUM             = 6;
    public static final int LONG               = 8;
    public static final int VCS                = 9;
    public static final int ROWID_THIN         = 11;
    public static final int DATE               = 12;
    public static final int VBI                = 15;
    public static final int RAW                = 23;
    public static final int LONG_RAW           = 24;
    public static final int CHAR               = 96;

    public static final int BINARY_FLOAT       = 100;
    public static final int BINARY_DOUBLE      = 101;
    public static final int RESULT_SET         = 102;
    public static final int ROWID              = 104;
    public static final int NAMED_TYPE         = 109;
    public static final int REF_TYPE           = 111;
    public static final int CLOB               = 112;
    public static final int BLOB               = 113;
    public static final int BFILE              = 114;
    public static final int RSET               = 116;

    public static final int TIMESTAMP          = 180;
    public static final int TIMESTAMPTZ        = 181;
    public static final int INTERVALYM         = 182;
    public static final int INTERVALDS         = 183;
    public static final int UROWID             = 208;
    public static final int TIMESTAMPLTZ       = 231;

    public static final int DML_RETURN_PARAM   = 994;
    public static final int NULL_TYPE          = 995;
    public static final int SET_CHAR_BYTES     = 996;
    public static final int T2S_OVERLONG_RAW   = 997;
    public static final int PLSQL_INDEX_TABLE  = 998;
    public static final int FIXED_CHAR         = 999;

    public static final int ONLY_FORM_USABLE   = 0;
    public static final int NOT_USABLE         = 1;
    public static final int NO_NEED_TO_PREPARE = 2;
    public static final int NEED_TO_PREPARE    = 3;

    protected byte[]        rowSpaceByte;
    protected int           internalTypeMaxLength;
    protected int           definedColumnSize;

    public byte[] getRowSpaceByte() {
        return rowSpaceByte;
    }

    public void setRowSpaceByte(byte[] rowSpaceByte) {
        this.rowSpaceByte = rowSpaceByte;
    }

    public int getInternalTypeMaxLength() {
        return internalTypeMaxLength;
    }

    public void setInternalTypeMaxLength(int internalTypeMaxLength) {
        this.internalTypeMaxLength = internalTypeMaxLength;
    }

    public int getDefinedColumnSize() {
        return definedColumnSize;
    }

    public void setDefinedColumnSize(int definedColumnSize) {
        this.definedColumnSize = definedColumnSize;
    }

}
