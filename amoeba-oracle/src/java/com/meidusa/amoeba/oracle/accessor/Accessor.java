package com.meidusa.amoeba.oracle.accessor;

public abstract class Accessor {

    static final int FIXED_CHAR         = 999;
    static final int CHAR               = 96;
    static final int VARCHAR            = 1;
    static final int VCS                = 9;
    static final int LONG               = 8;
    static final int NUMBER             = 2;
    static final int VARNUM             = 6;
    static final int BINARY_FLOAT       = 100;
    static final int BINARY_DOUBLE      = 101;
    static final int RAW                = 23;
    static final int VBI                = 15;
    static final int LONG_RAW           = 24;
    static final int ROWID              = 104;
    static final int ROWID_THIN         = 11;
    static final int RESULT_SET         = 102;
    static final int RSET               = 116;
    static final int DATE               = 12;
    static final int BLOB               = 113;
    static final int CLOB               = 112;
    static final int BFILE              = 114;
    static final int NAMED_TYPE         = 109;
    static final int REF_TYPE           = 111;
    static final int TIMESTAMP          = 180;
    static final int TIMESTAMPTZ        = 181;
    static final int TIMESTAMPLTZ       = 231;
    static final int INTERVALYM         = 182;
    static final int INTERVALDS         = 183;
    static final int UROWID             = 208;
    static final int PLSQL_INDEX_TABLE  = 998;
    static final int T2S_OVERLONG_RAW   = 997;
    static final int SET_CHAR_BYTES     = 996;
    static final int NULL_TYPE          = 995;
    static final int DML_RETURN_PARAM   = 994;

    static final int ONLY_FORM_USABLE   = 0;
    static final int NOT_USABLE         = 1;
    static final int NO_NEED_TO_PREPARE = 2;
    static final int NEED_TO_PREPARE    = 3;

    boolean          outBind;
    int              internalType;
    int              internalTypeMaxLength;
    boolean          isStream;
    boolean          isColumnNumberAware;
    short            formOfUse;

    int              externalType;
    String           internalTypeName;
    String           columnName;
    int              describeType;
    int              describeMaxLength;
    boolean          nullable;
    int              precision;
    int              scale;
    int              flags;
    int              contflag;
    int              total_elems;

    String           describeTypeName;
    int              definedColumnType;
    int              definedColumnSize;
    int              oacmxl;
    byte[]           rowSpaceByte;
    char[]           rowSpaceChar;
    short[]          rowSpaceIndicator;
    int              columnIndex;
    int              lengthIndex;
    int              indicatorIndex;
    int              columnIndexLastRow;
    int              lengthIndexLastRow;
    int              indicatorIndexLastRow;
    int              byteLength;
    int              charLength;
    int              defineType;
    boolean          isDMLReturnedParam;
    int              lastRowProcessed;
    public boolean   isUseLess;
    public int       physicalColumnIndex;
    boolean          isNullByDescribe;

    Accessor(){
        isStream = false;
        isColumnNumberAware = false;
        formOfUse = 2;
        definedColumnType = 0;
        definedColumnSize = 0;
        oacmxl = 0;
        rowSpaceByte = null;
        rowSpaceChar = null;
        rowSpaceIndicator = null;
        columnIndex = 0;
        lengthIndex = 0;
        indicatorIndex = 0;
        columnIndexLastRow = 0;
        lengthIndexLastRow = 0;
        indicatorIndexLastRow = 0;
        byteLength = 0;
        charLength = 0;
        isDMLReturnedParam = false;
        lastRowProcessed = 0;
        isUseLess = false;
        physicalColumnIndex = -2;
        isNullByDescribe = false;
    }

    public boolean unmarshalOneRow() {
        return false;
    }

    public void copyRow() {
    }
}
