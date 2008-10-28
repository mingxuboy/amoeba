package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

import oracle.sql.converter.CharacterConverters;

class CharacterSetGB18030 extends CharacterSetWithConverter
{

    CharacterSetGB18030(int i, CharacterConverters characterconverters)
    {
        super(i, characterconverters);
    }

    static CharacterSetGB18030 getInstance(int i, CharacterConverters characterconverters)
    {
        if(characterconverters.getGroupId() == 9)
            return new CharacterSetGB18030(i, characterconverters);
        else
            return null;
    }

    int decode(CharacterWalker characterwalker)
        throws SQLException
    {
        int i = characterwalker.bytes[characterwalker.next] & 0xff;
        if(i > 127)
            if(characterwalker.bytes.length > characterwalker.next + 1)
            {
                if((characterwalker.bytes[characterwalker.next] & 0xff) >= 129 && (characterwalker.bytes[characterwalker.next] & 0xff) <= 254 && (characterwalker.bytes[characterwalker.next + 1] & 0xff) >= 48 && (characterwalker.bytes[characterwalker.next + 1] & 0xff) <= 57)
                {
                    if(characterwalker.bytes.length > characterwalker.next + 3)
                    {
                        if((characterwalker.bytes[characterwalker.next + 2] & 0xff) >= 129 && (characterwalker.bytes[characterwalker.next + 2] & 0xff) <= 254 && (characterwalker.bytes[characterwalker.next + 3] & 0xff) >= 48 && (characterwalker.bytes[characterwalker.next + 3] & 0xff) <= 57)
                        {
                            i = (characterwalker.bytes[characterwalker.next] & 0xff) << 24 | (characterwalker.bytes[characterwalker.next + 1] & 0xff) << 16 | (characterwalker.bytes[characterwalker.next + 2] & 0xff) << 8 | characterwalker.bytes[characterwalker.next + 3] & 0xff;
                            characterwalker.next += 4;
                        } else
                        {
                            i = characterwalker.bytes[characterwalker.next] & 0xff;
                            characterwalker.next++;
                        }
                    } else
                    {
                        throw new SQLException("destination too small");
                    }
                } else
                {
                    i = (characterwalker.bytes[characterwalker.next] & 0xff) << 8 | characterwalker.bytes[characterwalker.next + 1] & 0xff;
                    characterwalker.next += 2;
                }
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
        for(short word1 = 0; i >> word0 != 0; word1++)
            word0 += 8;

        byte byte0;
        if(i >> 16 != 0)
        {
            word0 = 3;
            byte0 = 4;
        } else
        if(i >> 8 != 0)
        {
            word0 = 1;
            byte0 = 2;
        } else
        {
            word0 = 0;
            byte0 = 1;
        }
        need(characterbuffer, byte0);
        for(; word0 >= 0; word0 -= 8)
            characterbuffer.bytes[characterbuffer.next++] = (byte)(i >> word0 & 0xff);

    }

    static final int MAX_7BIT = 127;
    static Class m_charConvSuperclass;
}
