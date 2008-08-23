package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

import oracle.sql.converter.CharacterConverters;

class CharacterSetLCFixed extends CharacterSetWithConverter
{

    CharacterSetLCFixed(int i, CharacterConverters characterconverters)
    {
        super(i, characterconverters);
        m_leadingCodes = characterconverters.getLeadingCodes();
    }

    static CharacterSetLCFixed getInstance(int i, CharacterConverters characterconverters)
    {
        if(characterconverters.getGroupId() == 3)
            return new CharacterSetLCFixed(i, characterconverters);
        else
            return null;
    }

    int decode(CharacterWalker characterwalker)
        throws SQLException
    {
        if(characterwalker.bytes.length - characterwalker.next < 4)
            throw new SQLException("destination too small");
        int i = characterwalker.bytes[characterwalker.next] << 8 | characterwalker.bytes[characterwalker.next + 1];
        for(int j = 0; j < m_leadingCodes.length; j++)
            if(i == m_leadingCodes[j])
            {
                int k = 0;
                for(int l = 0; l < 4; l++)
                    k = k << 8 | characterwalker.bytes[characterwalker.next++];

                return k;
            }

        throw new SQLException("Leading code invalid");
    }

    void encode(CharacterBuffer characterbuffer, int i)
        throws SQLException
    {
        int j = i >> 16;
        for(int k = 0; k < m_leadingCodes.length; k++)
            if(j == m_leadingCodes[k])
            {
                need(characterbuffer, 4);
                for(int l = 3; l >= 0; l--)
                    characterbuffer.bytes[characterbuffer.next++] = (byte)(i >> 8 * l & 0xff);

                return;
            }

        throw new SQLException("Leading code invalid");
    }

    static final String CHAR_CONV_SUPERCLASS_NAME = "oracle.sql.converter.CharacterConverterLCFixed";
    static final int CHARLENGTH = 4;
    static Class m_charConvSuperclass;
    char m_leadingCodes[];
}
