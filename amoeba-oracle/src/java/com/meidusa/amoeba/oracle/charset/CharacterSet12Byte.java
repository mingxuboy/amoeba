// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CharacterSet12Byte.java

package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

import oracle.sql.converter.CharacterConverters;

// Referenced classes of package oracle.sql:
//            CharacterSetWithConverter, CharacterWalker, CharacterBuffer

class CharacterSet12Byte extends CharacterSetWithConverter
{

    CharacterSet12Byte(int i, CharacterConverters characterconverters)
    {
        super(i, characterconverters);
    }

    static CharacterSet12Byte getInstance(int i, CharacterConverters characterconverters)
    {
        if(characterconverters.getGroupId() == 1)
            return new CharacterSet12Byte(i, characterconverters);
        else
            return null;
    }

    int decode(CharacterWalker characterwalker)
        throws SQLException
    {
        int i = characterwalker.bytes[characterwalker.next] & 0xff;
        characterwalker.next++;
        if(i > 127)
            if(characterwalker.bytes.length > characterwalker.next)
            {
                i = i << 8 | characterwalker.bytes[characterwalker.next];
                characterwalker.next++;
            } else
            {
                throw new SQLException("destination too small");
            }
        return i;
    }

    void encode(CharacterBuffer characterbuffer, int i)
        throws SQLException
    {
        short word0 = 0;
        short word1;
        for(word1 = 1; i >> word0 != 0; word1++)
            word0 += 8;

        need(characterbuffer, word1);
        for(; word0 >= 0; word0 -= 8)
            characterbuffer.bytes[characterbuffer.next++] = (byte)(i >> word0 & 0xff);

    }

    static final String CHAR_CONV_SUPERCLASS_NAME = "oracle.sql.converter.CharacterConverter12Byte";
    static final int MAX_7BIT = 127;
    static Class m_charConvSuperclass;
}
