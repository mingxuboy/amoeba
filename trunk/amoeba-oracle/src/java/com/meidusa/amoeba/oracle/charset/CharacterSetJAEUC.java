// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CharacterSetJAEUC.java

package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

import oracle.sql.converter.CharacterConverters;

// Referenced classes of package oracle.sql:
//            CharacterSetWithConverter, CharacterWalker, CharacterBuffer

class CharacterSetJAEUC extends CharacterSetWithConverter
{

    CharacterSetJAEUC(int i, CharacterConverters characterconverters)
    {
        super(i, characterconverters);
    }

    static CharacterSetJAEUC getInstance(int i, CharacterConverters characterconverters)
    {
        if(characterconverters.getGroupId() == 2)
            return new CharacterSetJAEUC(i, characterconverters);
        else
            return null;
    }

    int decode(CharacterWalker characterwalker)
        throws SQLException
    {
        int i = characterwalker.bytes[characterwalker.next] & 0xff;
        characterwalker.next++;
        if(i > 127)
            if(i != 143)
            {
                if(characterwalker.bytes.length > characterwalker.next)
                {
                    i = i << 8 | characterwalker.bytes[characterwalker.next];
                    characterwalker.next++;
                }
            } else
            {
                for(int j = 0; j < 2; j++)
                    if(characterwalker.bytes.length > characterwalker.next)
                    {
                        i = i << 8 | characterwalker.bytes[characterwalker.next];
                        characterwalker.next++;
                    }

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

    static final String CHAR_CONV_SUPERCLASS_NAME = "oracle.sql.converter.CharacterConverterJAEUC";
    static final transient short MAX_7BIT = 127;
    static final transient short LEADINGCODE = 143;
    static Class m_charConvSuperclass;
}
