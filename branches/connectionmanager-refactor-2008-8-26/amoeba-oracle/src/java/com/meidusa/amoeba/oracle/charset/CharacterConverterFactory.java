package com.meidusa.amoeba.oracle.charset;

import oracle.sql.converter.CharacterConverters;

public abstract class CharacterConverterFactory
{

    public CharacterConverterFactory()
    {
    }

    public abstract CharacterConverters make(int i);
}
