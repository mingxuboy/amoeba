// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CharacterBuffer.java

package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

// Referenced classes of package oracle.sql:
//            CharacterSet

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
