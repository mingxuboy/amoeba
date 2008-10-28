package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

class CharacterSetAL32UTF8 extends CharacterSet
    implements CharacterRepConstants
{

    CharacterSetAL32UTF8(int i)
    {
        super(i);
        rep = 6;
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
        k = CharacterSet.convertAL32UTF8BytesToJavaChars(abyte0, i, ac, 0, ai, true);
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
        k = CharacterSet.convertAL32UTF8BytesToJavaChars(abyte0, i, ac, 0, ai, false);
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
        return stringToAL32UTF8(s);
    }

    public byte[] convert(String s)
        throws SQLException
    {
        return stringToAL32UTF8(s);
    }

    public byte[] convert(CharacterSet characterset, byte abyte0[], int i, int j)
        throws SQLException
    {
        byte abyte1[];
        if(characterset.rep == 6)
        {
            abyte1 = useOrCopy(abyte0, i, j);
        } else
        {
            String s = characterset.toString(abyte0, i, j);
            abyte1 = stringToAL32UTF8(s);
        }
        return abyte1;
    }

    int decode(CharacterWalker characterwalker)
        throws SQLException
    {
        byte abyte0[];
        int i;
        int l;
        try{
        abyte0 = characterwalker.bytes;
        i = characterwalker.next;
        int j = characterwalker.end;
        if(i >= j)
            failUTFConversion();
        int k = abyte0[i];
        l = getUTFByteLength((byte)k);
        if(l == 0 || i + (l - 1) >= j)
            failUTFConversion();
        char ac[];
        int i1;
        ac = new char[2];
        int ai[] = new int[1];
        ai[0] = l;
        i1 = CharacterSet.convertAL32UTF8BytesToJavaChars(abyte0, i, ac, 0, ai, false);
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
            need(characterbuffer, 4);
            char ac[] = {
                (char)(i >>> 16), (char)i
            };
            j = CharacterSet.convertJavaCharsToAL32UTF8Bytes(ac, 0, characterbuffer.bytes, characterbuffer.next, 2);
        } else
        {
            need(characterbuffer, 3);
            char ac1[] = {
                (char)i
            };
            j = CharacterSet.convertJavaCharsToAL32UTF8Bytes(ac1, 0, characterbuffer.bytes, characterbuffer.next, 1);
        }
        characterbuffer.next += j;
    }

    private static int getUTFByteLength(byte byte0)
    {
        return m_byteLen[byte0 >>> 4 & 0xf];
    }

    public int encodedByteLength(String s)
    {
        return CharacterSet.string32UTF8Length(s);
    }

    public int encodedByteLength(char ac[])
    {
        return CharacterSet.charArray32UTF8Length(ac);
    }

    private static int m_byteLen[] = {
        1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 
        0, 0, 2, 2, 3, 4
    };

}
