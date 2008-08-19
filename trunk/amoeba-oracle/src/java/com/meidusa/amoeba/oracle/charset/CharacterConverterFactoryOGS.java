package com.meidusa.amoeba.oracle.charset;

import oracle.i18n.text.converter.CharacterConverterOGS;
import oracle.sql.converter.CharacterConverters;

public class CharacterConverterFactoryOGS extends CharacterConverterFactory
{

    public CharacterConverterFactoryOGS()
    {
    }

    public CharacterConverters make(int i)
    {
        return CharacterConverterOGS.getInstance(i);
    }
}
