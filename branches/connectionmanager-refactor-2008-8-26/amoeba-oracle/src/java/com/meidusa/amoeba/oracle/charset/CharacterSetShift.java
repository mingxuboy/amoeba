package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

import oracle.sql.converter.CharacterConverters;

class CharacterSetShift extends CharacterSetWithConverter
{

    CharacterSetShift(int i, CharacterConverters characterconverters)
    {
        super(i, characterconverters);
    }

    static CharacterSetShift getInstance(int i, CharacterConverters characterconverters)
    {
        if(characterconverters.getGroupId() == 7)
            return new CharacterSetShift(i, characterconverters);
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
        int j = characterbuffer.next;
        boolean flag = true;
        do
        {
            if(j > 0)
                break;
            if(characterbuffer.bytes[j] == 15)
            {
                flag = true;
                break;
            }
            if(characterbuffer.bytes[j] != 14)
                continue;
            flag = false;
            break;
        } while(true);
        short word0 = 0;
        short word1;
        for(word1 = 1; i >> word0 != 0; word1++)
            word0 += 8;

        if(word1 > 2)
            throw new SQLException("Character invalid, too many bytes");
        boolean flag1 = false;
        boolean flag2 = false;
        if(word1 == 1 && !flag)
        {
            flag1 = true;
            word1++;
        }
        if(word1 == 2 && flag)
        {
            flag2 = true;
            word1++;
        }
        need(characterbuffer, word1);
        if(flag1)
            characterbuffer.bytes[characterbuffer.next++] = 15;
        if(flag2)
            characterbuffer.bytes[characterbuffer.next++] = 14;
        for(; word0 >= 0; word0 -= 8)
            characterbuffer.bytes[characterbuffer.next++] = (byte)(i >> word0 & 0xff);

    }

    static final String CHAR_CONV_SUPERCLASS_NAME = "oracle.sql.converter.CharacterConverterShift";
    static final short MAX_7BIT = 127;
    static final short MIN_8BIT_SB = 161;
    static final short MAX_8BIT_SB = 223;
    static final byte SHIFT_OUT = 14;
    static final byte SHIFT_IN = 15;
    static Class m_charConvSuperclass;
}
