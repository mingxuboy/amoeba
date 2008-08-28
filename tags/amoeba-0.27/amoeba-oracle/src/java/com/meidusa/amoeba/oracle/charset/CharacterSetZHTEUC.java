package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

import oracle.sql.converter.CharacterConverters;

class CharacterSetZHTEUC extends CharacterSetWithConverter
{

    CharacterSetZHTEUC(int i, CharacterConverters characterconverters)
    {
        super(i, characterconverters);
        m_leadingCodes = characterconverters.getLeadingCodes();
    }

    static CharacterSetZHTEUC getInstance(int i, CharacterConverters characterconverters)
    {
        if(characterconverters.getGroupId() == 5)
            return new CharacterSetZHTEUC(i, characterconverters);
        else
            return null;
    }

    int decode(CharacterWalker characterwalker)
        throws SQLException
    {
        if(characterwalker.next + 1 < characterwalker.bytes.length)
        {
            int i = characterwalker.bytes[characterwalker.next] << 8 | characterwalker.bytes[characterwalker.next + 1];
            for(int k = 0; k < m_leadingCodes.length; k++)
                if(i == m_leadingCodes[k])
                {
                    if(characterwalker.bytes.length - characterwalker.next < 4)
                        throw new SQLException("destination too small");
                    int l = 0;
                    for(int i1 = 0; i1 < 4; i1++)
                        l = l << 8 | characterwalker.bytes[characterwalker.next++];

                    return l;
                }

        }
        int j = characterwalker.bytes[characterwalker.next] & 0xff;
        characterwalker.next++;
        if(j > 127)
            if(characterwalker.bytes.length > characterwalker.next)
            {
                j = j << 8 | characterwalker.bytes[characterwalker.next];
                characterwalker.next++;
            } else
            {
                throw new SQLException("destination too small");
            }
        return j;
    }

    void encode(CharacterBuffer characterbuffer, int i)
        throws SQLException
    {
        int j = i >> 16;
        for(int k = 0; k < m_leadingCodes.length; k++)
            if(j == m_leadingCodes[k])
            {
                need(characterbuffer, 4);
                for(int l = 0; l < 4; l++)
                {
                    characterbuffer.bytes[characterbuffer.next++] = (byte)i;
                    i >>= 8;
                }

                return;
            }

        throw new SQLException();
    }

    static final String CHAR_CONV_SUPERCLASS_NAME = "oracle.sql.converter.CharacterConverterZHTEUC";
    static final int MAX_7BIT = 127;
    static final int CHARLENGTH = 4;
    static Class m_charConvSuperclass;
    char m_leadingCodes[];
}
