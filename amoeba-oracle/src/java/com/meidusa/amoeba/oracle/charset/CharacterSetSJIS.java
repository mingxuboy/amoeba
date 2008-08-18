// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CharacterSetSJIS.java

package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

import oracle.sql.converter.CharacterConverters;

// Referenced classes of package oracle.sql:
//            CharacterSetWithConverter, CharacterWalker, CharacterBuffer

class CharacterSetSJIS extends CharacterSetWithConverter
{

    CharacterSetSJIS(int i, CharacterConverters characterconverters)
    {
        super(i, characterconverters);
    }

    static CharacterSetSJIS getInstance(int i, CharacterConverters characterconverters)
    {
        if(characterconverters.getGroupId() == 4)
            return new CharacterSetSJIS(i, characterconverters);
        else
            return null;
    }

    int decode(CharacterWalker characterwalker)
        throws SQLException
    {
        int i = characterwalker.bytes[characterwalker.next] & 0xff;
        characterwalker.next++;
        if(i > 223 || i > 127 && i < 161)
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

    static final String CHAR_CONV_SUPERCLASS_NAME = "oracle.sql.converter.CharacterConverterSJIS";
    static final short MAX_7BIT = 127;
    static final short MIN_8BIT_SB = 161;
    static final short MAX_8BIT_SB = 223;
    static Class m_charConvSuperclass;
}
