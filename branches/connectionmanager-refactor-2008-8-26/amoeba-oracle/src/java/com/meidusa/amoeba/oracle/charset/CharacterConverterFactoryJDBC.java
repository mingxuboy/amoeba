package com.meidusa.amoeba.oracle.charset;

import oracle.sql.converter.CharacterConverters;

public class CharacterConverterFactoryJDBC extends CharacterConverterFactory
{

    public CharacterConverterFactoryJDBC()
    {
    }

    public CharacterConverters make(int i)
    {
        return CharacterConverterJDBC.getInstance(i);
    }
}
