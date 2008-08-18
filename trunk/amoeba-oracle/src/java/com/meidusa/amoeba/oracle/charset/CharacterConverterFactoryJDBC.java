// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CharacterConverterFactoryJDBC.java

package com.meidusa.amoeba.oracle.charset;

import oracle.sql.converter.CharacterConverters;


// Referenced classes of package oracle.sql.converter:
//            CharacterConverterFactory, CharacterConverterJDBC, CharacterConverters

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
