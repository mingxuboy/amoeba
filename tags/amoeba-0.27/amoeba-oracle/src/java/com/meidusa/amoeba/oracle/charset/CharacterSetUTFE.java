package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

class CharacterSetUTFE extends CharacterSet
    implements CharacterRepConstants
{

    CharacterSetUTFE(int i)
    {
        super(i);
        rep = 3;
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

    public String toString(byte abyte0[], int i, int j)
        throws SQLException
    {
        char ac[];
        int k;
        try{
        ac = new char[abyte0.length];
        k = UTFEToJavaChar(abyte0, i, j, ac, CharacterSet.CharacterConverterBehavior.REPORT_ERROR);
        return new String(ac, 0, k);
        }catch(SQLException sqlexception){
            throw new SQLException(sqlexception.getMessage());
        }
//        SQLException sqlexception;
//        sqlexception;
//        throw new SQLException(sqlexception.getMessage());
    }

    public String toStringWithReplacement(byte abyte0[], int i, int j)
    {
        char ac[];
        int k;
        try{
        ac = new char[abyte0.length];
        k = UTFEToJavaChar(abyte0, i, j, ac, CharacterSet.CharacterConverterBehavior.REPLACEMENT);
        return new String(ac, 0, k);
        }catch(SQLException sqlexception){
            throw new IllegalStateException(sqlexception.getMessage());
        }
//        SQLException sqlexception;
//        sqlexception;
//        throw new IllegalStateException(sqlexception.getMessage());
    }

    int UTFEToJavaChar(byte abyte0[], int i, int j, char ac[], CharacterSet.CharacterConverterBehavior characterconverterbehavior)
        throws SQLException
    {
        int k = i;
        int l = i + j;
        int i1 = 0;
        do
        {
            if(k >= l)
                break;
            byte byte0 = utfe2utf8m[high(abyte0[k])][low(abyte0[k++])];
            switch(byte0 >>> 4 & 0xf)
            {
            case 0: // '\0'
            case 1: // '\001'
            case 2: // '\002'
            case 3: // '\003'
            case 4: // '\004'
            case 5: // '\005'
            case 6: // '\006'
            case 7: // '\007'
                ac[i1++] = (char)(byte0 & 0x7f);
                break;

            case 8: // '\b'
            case 9: // '\t'
                ac[i1++] = (char)(byte0 & 0x1f);
                break;

            case 12: // '\f'
            case 13: // '\r'
                if(k >= l)
                {
                    characterconverterbehavior.onFailConversion();
                    k = l;
                } else
                {
                    byte0 &= 0x1f;
                    byte byte1 = utfe2utf8m[high(abyte0[k])][low(abyte0[k++])];
                    if(!is101xxxxx(byte1))
                    {
                        characterconverterbehavior.onFailConversion();
                        ac[i1++] = '\uFFFD';
                    } else
                    {
                        ac[i1++] = (char)(byte0 << 5 | byte1 & 0x1f);
                    }
                }
                break;

            case 14: // '\016'
                if(k + 1 >= l)
                {
                    characterconverterbehavior.onFailConversion();
                    k = l;
                } else
                {
                    byte0 &= 0xf;
                    byte byte2 = utfe2utf8m[high(abyte0[k])][low(abyte0[k++])];
                    byte byte4 = utfe2utf8m[high(abyte0[k])][low(abyte0[k++])];
                    if(!is101xxxxx(byte2) || !is101xxxxx(byte4))
                    {
                        characterconverterbehavior.onFailConversion();
                        ac[i1++] = '\uFFFD';
                    } else
                    {
                        ac[i1++] = (char)(byte0 << 10 | (byte2 & 0x1f) << 5 | byte4 & 0x1f);
                    }
                }
                break;

            case 15: // '\017'
                if(k + 2 >= l)
                {
                    characterconverterbehavior.onFailConversion();
                    k = l;
                } else
                {
                    byte0 &= 1;
                    byte byte3 = utfe2utf8m[high(abyte0[k])][low(abyte0[k++])];
                    byte byte5 = utfe2utf8m[high(abyte0[k])][low(abyte0[k++])];
                    byte byte6 = utfe2utf8m[high(abyte0[k])][low(abyte0[k++])];
                    if(!is101xxxxx(byte3) || !is101xxxxx(byte5) || !is101xxxxx(byte6))
                    {
                        characterconverterbehavior.onFailConversion();
                        ac[i1++] = '\uFFFD';
                    } else
                    {
                        ac[i1++] = (char)(byte0 << 15 | (byte3 & 0x1f) << 10 | (byte5 & 0x1f) << 5 | byte6 & 0x1f);
                    }
                }
                break;

            case 10: // '\n'
            case 11: // '\013'
            default:
                characterconverterbehavior.onFailConversion();
                ac[i1++] = '\uFFFD';
                break;
            }
        } while(true);
        return i1;
    }

    public byte[] convertWithReplacement(String s)
    {
        char ac[] = s.toCharArray();
        byte abyte0[] = new byte[ac.length * 4];
        int i = javaCharsToUTFE(ac, 0, ac.length, abyte0, 0);
        byte abyte1[] = new byte[i];
        System.arraycopy(abyte0, 0, abyte1, 0, i);
        return abyte1;
    }

    public byte[] convert(String s)
        throws SQLException
    {
        return convertWithReplacement(s);
    }

    public byte[] convert(CharacterSet characterset, byte abyte0[], int i, int j)
        throws SQLException
    {
        byte abyte1[];
        if(characterset.rep == 3)
        {
            abyte1 = useOrCopy(abyte0, i, j);
        } else
        {
            String s = characterset.toString(abyte0, i, j);
            abyte1 = convert(s);
        }
        return abyte1;
    }

    int javaCharsToUTFE(char ac[], int i, int j, byte abyte0[], int k)
    {
        int l = i + j;
        int i2 = 0;
        for(int j2 = i; j2 < l; j2++)
        {
            char c = ac[j2];
            if(c <= '\037')
            {
                int i1 = c | 0x80;
                abyte0[i2++] = utf8m2utfe[high(i1)][low(i1)];
                continue;
            }
            if(c <= '\177')
            {
                abyte0[i2++] = utf8m2utfe[high(c)][low(c)];
                continue;
            }
            if(c <= '\u03FF')
            {
                int j1 = (c & 0x3e0) >> 5 | 0xc0;
                abyte0[i2++] = utf8m2utfe[high(j1)][low(j1)];
                j1 = c & 0x1f | 0xa0;
                abyte0[i2++] = utf8m2utfe[high(j1)][low(j1)];
                continue;
            }
            if(c <= '\u3FFF')
            {
                int k1 = (c & 0x3c00) >> 10 | 0xe0;
                abyte0[i2++] = utf8m2utfe[high(k1)][low(k1)];
                k1 = (c & 0x3e0) >> 5 | 0xa0;
                abyte0[i2++] = utf8m2utfe[high(k1)][low(k1)];
                k1 = c & 0x1f | 0xa0;
                abyte0[i2++] = utf8m2utfe[high(k1)][low(k1)];
            } else
            {
                int l1 = (c & 0x8000) >> 15 | 0xf0;
                abyte0[i2++] = utf8m2utfe[high(l1)][low(l1)];
                l1 = (c & 0x7c00) >> 10 | 0xa0;
                abyte0[i2++] = utf8m2utfe[high(l1)][low(l1)];
                l1 = (c & 0x3e0) >> 5 | 0xa0;
                abyte0[i2++] = utf8m2utfe[high(l1)][low(l1)];
                l1 = c & 0x1f | 0xa0;
                abyte0[i2++] = utf8m2utfe[high(l1)][low(l1)];
            }
        }

        return i2;
    }

    int decode(CharacterWalker characterwalker)
        throws SQLException
    {
        byte abyte0[];
        int i;
        int k;
        int i1;        
        abyte0 = characterwalker.bytes;
        i = characterwalker.next;
        int j = characterwalker.end;
        k = 0;
        try{
        if(i >= j)
            failUTFConversion();
        int l = abyte0[i];
        i1 = getUTFByteLength((byte)l);
        if(i1 == 0 || i + (i1 - 1) >= j)
            failUTFConversion();
        char ac[];
        int j1;
        ac = new char[2];
        j1 = UTFEToJavaChar(abyte0, i, i1, ac, CharacterSet.CharacterConverterBehavior.REPORT_ERROR);
        characterwalker.next += i1;
        if(j1 == 1)
            return ac[0];
        return ac[0] << 16 | ac[1];
        }catch(SQLException sqlexception){
            failUTFConversion();
            characterwalker.next = i;
            return k;
        }
//        SQLException sqlexception;
//        sqlexception;
//        failUTFConversion();
//        characterwalker.next = i;
//        return k;
    }

    void encode(CharacterBuffer characterbuffer, int i)
        throws SQLException
    {
        if((i & 0xffff0000) != 0)
        {
            failUTFConversion();
        } else
        {
            char ac[] = {
                (char)i
            };
            if(i <= 31)
            {
                need(characterbuffer, 1);
                int j = i | 0x80;
                characterbuffer.bytes[characterbuffer.next++] = utf8m2utfe[high(j)][low(j)];
            } else
            if(i <= 127)
            {
                need(characterbuffer, 1);
                characterbuffer.bytes[characterbuffer.next++] = utf8m2utfe[high(i)][low(i)];
            } else
            if(i <= 1023)
            {
                need(characterbuffer, 2);
                int k = (i & 0x3e0) >> 5 | 0xc0;
                characterbuffer.bytes[characterbuffer.next++] = utf8m2utfe[high(k)][low(k)];
                k = i & 0x1f | 0xa0;
                characterbuffer.bytes[characterbuffer.next++] = utf8m2utfe[high(k)][low(k)];
            } else
            if(i <= 16383)
            {
                need(characterbuffer, 3);
                int l = (i & 0x3c00) >> 10 | 0xe0;
                characterbuffer.bytes[characterbuffer.next++] = utf8m2utfe[high(l)][low(l)];
                l = (i & 0x3e0) >> 5 | 0xa0;
                characterbuffer.bytes[characterbuffer.next++] = utf8m2utfe[high(l)][low(l)];
                l = i & 0x1f | 0xa0;
                characterbuffer.bytes[characterbuffer.next++] = utf8m2utfe[high(l)][low(l)];
            } else
            {
                need(characterbuffer, 4);
                int i1 = (i & 0x8000) >> 15 | 0xf0;
                characterbuffer.bytes[characterbuffer.next++] = utf8m2utfe[high(i1)][low(i1)];
                i1 = (i & 0x7c00) >> 10 | 0xa0;
                characterbuffer.bytes[characterbuffer.next++] = utf8m2utfe[high(i1)][low(i1)];
                i1 = (i & 0x3e0) >> 5 | 0xa0;
                characterbuffer.bytes[characterbuffer.next++] = utf8m2utfe[high(i1)][low(i1)];
                i1 = i & 0x1f | 0xa0;
                characterbuffer.bytes[characterbuffer.next++] = utf8m2utfe[high(i1)][low(i1)];
            }
        }
    }

    private static int high(int i)
    {
        return i >> 4 & 0xf;
    }

    private static int low(int i)
    {
        return i & 0xf;
    }

    private static boolean is101xxxxx(byte byte0)
    {
        return (byte0 & 0xffffffe0) == -96;
    }

    private static int getUTFByteLength(byte byte0)
    {
        return m_byteLen[utfe2utf8m[high(byte0)][low(byte0)] >>> 4 & 0xf];
    }

    static final int MAXBYTEPERCHAR = 4;
    static byte utf8m2utfe[][] = {
        {
            0, 1, 2, 3, 55, 45, 46, 47, 22, 5, 
            21, 11, 12, 13, 14, 15
        }, {
            16, 17, 18, 19, 60, 61, 50, 38, 24, 25, 
            63, 39, 28, 29, 30, 31
        }, {
            64, 90, 127, 123, 91, 108, 80, 125, 77, 93, 
            92, 78, 107, 96, 75, 97
        }, {
            -16, -15, -14, -13, -12, -11, -10, -9, -8, -7, 
            122, 94, 76, 126, 110, 111
        }, {
            124, -63, -62, -61, -60, -59, -58, -57, -56, -55, 
            -47, -46, -45, -44, -43, -42
        }, {
            -41, -40, -39, -30, -29, -28, -27, -26, -25, -24, 
            -23, -83, -32, -67, 95, 109
        }, {
            121, -127, -126, -125, -124, -123, -122, -121, -120, -119, 
            -111, -110, -109, -108, -107, -106
        }, {
            -105, -104, -103, -94, -93, -92, -91, -90, -89, -88, 
            -87, -64, 79, -48, -95, 7
        }, {
            32, 33, 34, 35, 36, 37, 6, 23, 40, 41, 
            42, 43, 44, 9, 10, 27
        }, {
            48, 49, 26, 51, 52, 53, 54, 8, 56, 57, 
            58, 59, 4, 20, 62, -1
        }, {
            65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 
            81, 82, 83, 84, 85, 86
        }, {
            87, 88, 89, 98, 99, 100, 101, 102, 103, 104, 
            105, 106, 112, 113, 114, 115
        }, {
            116, 117, 118, 119, 120, -128, -118, -117, -116, -115, 
            -114, -113, -112, -102, -101, -100
        }, {
            -99, -98, -97, -96, -86, -85, -84, -82, -81, -80, 
            -79, -78, -77, -76, -75, -74
        }, {
            -73, -72, -71, -70, -69, -68, -66, -65, -54, -53, 
            -52, -51, -50, -49, -38, -37
        }, {
            -36, -35, -34, -33, -31, -22, -21, -20, -19, -18, 
            -17, -6, -5, -4, -3, -2
        }
    };
    static byte utfe2utf8m[][] = {
        {
            0, 1, 2, 3, -100, 9, -122, 127, -105, -115, 
            -114, 11, 12, 13, 14, 15
        }, {
            16, 17, 18, 19, -99, 10, 8, -121, 24, 25, 
            -110, -113, 28, 29, 30, 31
        }, {
            -128, -127, -126, -125, -124, -123, 23, 27, -120, -119, 
            -118, -117, -116, 5, 6, 7
        }, {
            -112, -111, 22, -109, -108, -107, -106, 4, -104, -103, 
            -102, -101, 20, 21, -98, 26
        }, {
            32, -96, -95, -94, -93, -92, -91, -90, -89, -88, 
            -87, 46, 60, 40, 43, 124
        }, {
            38, -86, -85, -84, -83, -82, -81, -80, -79, -78, 
            33, 36, 42, 41, 59, 94
        }, {
            45, 47, -77, -76, -75, -74, -73, -72, -71, -70, 
            -69, 44, 37, 95, 62, 63
        }, {
            -68, -67, -66, -65, -64, -63, -62, -61, -60, 96, 
            58, 35, 64, 39, 61, 34
        }, {
            -59, 97, 98, 99, 100, 101, 102, 103, 104, 105, 
            -58, -57, -56, -55, -54, -53
        }, {
            -52, 106, 107, 108, 109, 110, 111, 112, 113, 114, 
            -51, -50, -49, -48, -47, -46
        }, {
            -45, 126, 115, 116, 117, 118, 119, 120, 121, 122, 
            -44, -43, -42, 88, -41, -40
        }, {
            -39, -38, -37, -36, -35, -34, -33, -32, -31, -30, 
            -29, -28, -27, 93, -26, -25
        }, {
            123, 65, 66, 67, 68, 69, 70, 71, 72, 73, 
            -24, -23, -22, -21, -20, -19
        }, {
            13, 74, 75, 76, 77, 78, 79, 80, 81, 82, 
            -18, -17, -16, -15, -14, -13
        }, {
            92, -12, 83, 84, 85, 86, 87, 88, 89, 90, 
            -11, -10, -9, -8, -7, -6
        }, {
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 
            -5, -4, -3, -2, -1, -97
        }
    };
    private static int m_byteLen[] = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
        0, 0, 2, 2, 3, 4
    };

}
