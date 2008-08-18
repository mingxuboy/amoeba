// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CharacterSetAL16UTF16LE.java

package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

// Referenced classes of package oracle.sql:
//            CharacterSet, CharacterRepConstants, CharacterWalker, CharacterBuffer

class CharacterSetAL16UTF16LE extends CharacterSet
    implements CharacterRepConstants
{

    CharacterSetAL16UTF16LE(int i)
    {
        super(i);
        rep = 5;
    }

    public boolean isLossyFrom(CharacterSet characterset)
    {
        return !characterset.isUnicode();
    }

    public boolean isConvertibleFrom(CharacterSet characterset)
    {
        boolean flag = characterset.rep <= 1024;
        return flag;
    }

    public boolean isUnicode()
    {
        return true;
    }

    public String toStringWithReplacement(byte abyte0[], int i, int j)
    {
        char ac[];
        int k;
        try{
        ac = new char[Math.min(abyte0.length - i >>> 1, j >>> 1)];
        k = CharacterSet.convertAL16UTF16LEBytesToJavaChars(abyte0, i, ac, 0, j, true);
        return new String(ac, 0, k);
        }catch(SQLException sqlexception){
            return "";
        }
//        SQLException sqlexception;
//        sqlexception;
//        return "";
    }

    public String toString(byte abyte0[], int i, int j)
        throws SQLException
    {
        char ac[];
        int k;
        try{
        ac = new char[Math.min(abyte0.length - i >>> 1, j >>> 1)];
        k = CharacterSet.convertAL16UTF16LEBytesToJavaChars(abyte0, i, ac, 0, j, false);
        return new String(ac, 0, k);
        }catch(SQLException sqlexception){
            failUTFConversion();
            return "";
        }
//        SQLException sqlexception;
//        sqlexception;
//        failUTFConversion();
//        return "";
    }

    public byte[] convert(String s)
        throws SQLException
    {
        return stringToAL16UTF16LEBytes(s);
    }

    public byte[] convertWithReplacement(String s)
    {
        return stringToAL16UTF16LEBytes(s);
    }

    public byte[] convert(CharacterSet characterset, byte abyte0[], int i, int j)
        throws SQLException
    {
        byte abyte1[];
        if(characterset.rep == 5)
        {
            abyte1 = useOrCopy(abyte0, i, j);
        } else
        {
            String s = characterset.toString(abyte0, i, j);
            abyte1 = stringToAL16UTF16LEBytes(s);
        }
        return abyte1;
    }

    int decode(CharacterWalker characterwalker)
        throws SQLException
    {
        byte abyte0[] = characterwalker.bytes;
        int i = characterwalker.next;
        int j = characterwalker.end;
        if(i + 2 >= j)
            failUTFConversion();
        byte byte0 = abyte0[i++];
        byte byte1 = abyte0[i++];
        int k = byte0 << 8 & 0xff00 | byte1;
        characterwalker.next = i;
        return k;
    }

    void encode(CharacterBuffer characterbuffer, int i)
        throws SQLException
    {
        if(i > 65535)
        {
            failUTFConversion();
        } else
        {
            need(characterbuffer, 2);
            characterbuffer.bytes[characterbuffer.next++] = (byte)(i >> 8 & 0xff);
            characterbuffer.bytes[characterbuffer.next++] = (byte)(i & 0xff);
        }
    }
}
