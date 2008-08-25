package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

public final class CharacterBuffer
{

    public CharacterBuffer(CharacterSet characterset)
    {
        charSet = characterset;
        next = 0;
        bytes = new byte[32];
    }

    public void append(int i)
        throws SQLException
    {
        charSet.encode(this, i);
    }

    public byte[] getBytes()
    {
        return CharacterSet.useOrCopy(bytes, 0, next);
    }

    CharacterSet charSet;
    byte bytes[];
    int next;
}
