package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

class CharacterSetUnknown extends CharacterSet
    implements CharacterRepConstants
{

    CharacterSetUnknown(int i)
    {
        super(i);
        rep = 1024 + i;
    }

    public boolean isLossyFrom(CharacterSet characterset)
    {
        return characterset.getOracleId() != getOracleId();
    }

    public boolean isConvertibleFrom(CharacterSet characterset)
    {
        return characterset.getOracleId() == getOracleId();
    }

    public String toStringWithReplacement(byte abyte0[], int i, int j)
    {
        return "???";
    }

    public String toString(byte abyte0[], int i, int j)
        throws SQLException
    {
        failCharsetUnknown(this);
        return null;
    }

    public byte[] convert(String s)
        throws SQLException
    {
        failCharsetUnknown(this);
        return null;
    }

    public byte[] convertWithReplacement(String s)
    {
        return new byte[0];
    }

    public byte[] convert(CharacterSet characterset, byte abyte0[], int i, int j)
        throws SQLException
    {
        if(characterset.getOracleId() == getOracleId())
        {
            return useOrCopy(abyte0, i, j);
        } else
        {
            failCharsetUnknown(this);
            return null;
        }
    }

    int decode(CharacterWalker characterwalker)
        throws SQLException
    {
        failCharsetUnknown(this);
        return 0;
    }

    void encode(CharacterBuffer characterbuffer, int i)
        throws SQLException
    {
        failCharsetUnknown(this);
    }

    static void failCharsetUnknown(CharacterSet characterset)
        throws SQLException
    {
//        DatabaseError.throwSqlException(56, characterset);
        throw new RuntimeException();
    }
}
