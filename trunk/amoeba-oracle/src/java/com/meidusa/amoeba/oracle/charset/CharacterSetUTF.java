// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CharacterSetUTF.java

package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

// Referenced classes of package oracle.sql:
//            CharacterSet, CharacterRepConstants, CharacterWalker, CharacterBuffer

class CharacterSetUTF extends CharacterSet
    implements CharacterRepConstants
{

    CharacterSetUTF(int i)
    {
        super(i);
        rep = 2;
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
        ac = new char[abyte0.length];
        int ai[] = new int[1];
        ai[0] = j;
        k = CharacterSet.convertUTFBytesToJavaChars(abyte0, i, ac, 0, ai, true);
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
        ac = new char[abyte0.length];
        int ai[] = new int[1];
        ai[0] = j;
        k = CharacterSet.convertUTFBytesToJavaChars(abyte0, i, ac, 0, ai, false);
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

    public byte[] convertWithReplacement(String s)
    {
        return stringToUTF(s);
    }

    public byte[] convert(String s)
        throws SQLException
    {
        return stringToUTF(s);
    }

    public byte[] convert(CharacterSet characterset, byte abyte0[], int i, int j)
        throws SQLException
    {
        byte abyte1[];
        if(characterset.rep == 2)
        {
            abyte1 = useOrCopy(abyte0, i, j);
        } else
        {
            String s = characterset.toString(abyte0, i, j);
            abyte1 = stringToUTF(s);
        }
        return abyte1;
    }

    int decode(CharacterWalker characterwalker)
        throws SQLException
    {
        byte abyte0[];
        int i;
        int l;
        abyte0 = characterwalker.bytes;
        i = characterwalker.next;
        int j = characterwalker.end;
        try{
        if(i >= j)
            failUTFConversion();
        int k = abyte0[i];
        l = getUTFByteLength((byte)k);
        if(l == 0 || i + (l - 1) >= j)
            failUTFConversion();
        if(l == 3 && isHiSurrogate((byte)k, abyte0[i + 1]) && i + 5 < j)
            l = 6;
        char ac[];
        int i1;
        ac = new char[2];
        int ai[] = new int[1];
        ai[0] = l;
        i1 = CharacterSet.convertUTFBytesToJavaChars(abyte0, i, ac, 0, ai, false);
        characterwalker.next += l;
        if(i1 == 1)
            return ac[0];
        return ac[0] << 16 | ac[1];
        }catch(SQLException sqlexception){
            failUTFConversion();
            return 0;
        }
//        SQLException sqlexception;
//        sqlexception;
//        failUTFConversion();
//        return 0;
    }

    void encode(CharacterBuffer characterbuffer, int i)
        throws SQLException
    {
        int j;
        if((i & 0xffff0000) != 0)
        {
            need(characterbuffer, 6);
            char ac[] = {
                (char)(i >>> 16), (char)i
            };
            j = CharacterSet.convertJavaCharsToUTFBytes(ac, 0, characterbuffer.bytes, characterbuffer.next, 2);
        } else
        {
            need(characterbuffer, 3);
            char ac1[] = {
                (char)i
            };
            j = CharacterSet.convertJavaCharsToUTFBytes(ac1, 0, characterbuffer.bytes, characterbuffer.next, 1);
        }
        characterbuffer.next += j;
    }

    private static int getUTFByteLength(byte byte0)
    {
        return m_byteLen[byte0 >>> 4 & 0xf];
    }

    private static boolean isHiSurrogate(byte byte0, byte byte1)
    {
        return byte0 == -19 && byte1 >= -96;
    }

    public int encodedByteLength(String s)
    {
        return CharacterSet.stringUTFLength(s);
    }

    public int encodedByteLength(char ac[])
    {
        return CharacterSet.charArrayUTF8Length(ac);
    }

    private static int m_byteLen[] = {
        1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 
        0, 0, 2, 2, 3, 0
    };

}
