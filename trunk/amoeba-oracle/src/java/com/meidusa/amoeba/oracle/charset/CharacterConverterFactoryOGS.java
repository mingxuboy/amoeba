// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CharacterConverterFactoryOGS.java

package com.meidusa.amoeba.oracle.charset;

import oracle.i18n.text.converter.CharacterConverterOGS;
import oracle.sql.converter.CharacterConverters;

// Referenced classes of package oracle.sql.converter:
//            CharacterConverterFactory, CharacterConverters

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
