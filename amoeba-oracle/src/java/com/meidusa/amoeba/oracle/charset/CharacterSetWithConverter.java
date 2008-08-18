// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CharacterSetWithConverter.java

package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

import oracle.sql.converter.CharacterConverters;

// Referenced classes of package oracle.sql:
//            CharacterSet, CharacterSet1Byte, CharacterSetSJIS, CharacterSetShift, 
//            CharacterSet2ByteFixed, CharacterSetGB18030, CharacterSet12Byte, CharacterSetJAEUC, 
//            CharacterSetZHTEUC, CharacterSetLCFixed

public abstract class CharacterSetWithConverter extends CharacterSet
{

    CharacterSetWithConverter(int i, CharacterConverters characterconverters)
    {
        super(i);
        m_converter = characterconverters;
    }

    static CharacterSet getInstance(int i)
    {
        CharacterConverters characterconverters = ccFactory.make(i);
        if(characterconverters == null)
            return null;
        Object obj = null;
        if((obj = CharacterSet1Byte.getInstance(i, characterconverters)) != null)
            return ((CharacterSet) (obj));
        if((obj = CharacterSetSJIS.getInstance(i, characterconverters)) != null)
            return ((CharacterSet) (obj));
        if((obj = CharacterSetShift.getInstance(i, characterconverters)) != null)
            return ((CharacterSet) (obj));
        if((obj = CharacterSet2ByteFixed.getInstance(i, characterconverters)) != null)
            return ((CharacterSet) (obj));
        if((obj = CharacterSetGB18030.getInstance(i, characterconverters)) != null)
            return ((CharacterSet) (obj));
        if((obj = CharacterSet12Byte.getInstance(i, characterconverters)) != null)
            return ((CharacterSet) (obj));
        if((obj = CharacterSetJAEUC.getInstance(i, characterconverters)) != null)
            return ((CharacterSet) (obj));
        if((obj = CharacterSetZHTEUC.getInstance(i, characterconverters)) != null)
            return ((CharacterSet) (obj));
        else
            return CharacterSetLCFixed.getInstance(i, characterconverters);
    }

    public boolean isLossyFrom(CharacterSet characterset)
    {
        return characterset.getOracleId() != getOracleId();
    }

    public boolean isConvertibleFrom(CharacterSet characterset)
    {
        return characterset.getOracleId() == getOracleId();
    }

    public String toStringWithReplacement(byte abyte0[], int i, int j)
    {
        return m_converter.toUnicodeStringWithReplacement(abyte0, i, j);
    }

    public String toString(byte abyte0[], int i, int j)
        throws SQLException
    {
        return m_converter.toUnicodeString(abyte0, i, j);
    }

    public byte[] convert(String s)
        throws SQLException
    {
        return m_converter.toOracleString(s);
    }

    public byte[] convertWithReplacement(String s)
    {
        return m_converter.toOracleStringWithReplacement(s);
    }

    public byte[] convert(CharacterSet characterset, byte abyte0[], int i, int j)
        throws SQLException
    {
        if(characterset.getOracleId() == getOracleId())
            return useOrCopy(abyte0, i, j);
        else
            return convert(characterset.toString(abyte0, i, j));
    }

    public static CharacterConverterFactory ccFactory = new CharacterConverterFactoryJDBC();
    CharacterConverters m_converter;

}
