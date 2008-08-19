// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CharacterSetFactory.java

package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

// Referenced classes of package oracle.sql:
//            CharacterSet

abstract class CharacterSetFactory
{

    CharacterSetFactory()
    {
    }

    public abstract CharacterSet make(int i);

    public static void main(String args[])
    {
        CharacterSet characterset = CharacterSet.make(871);
        int ai[] = {
            1, 31, 870, 871
        };
        for(int i = 0; i < ai.length; i++)
        {
            CharacterSet characterset1 = CharacterSet.make(ai[i]);
            String s = "longlonglonglong";
            s = s + s + s + s;
            s = s + s + s + s;
            s = s + s + s + s;
            s = s + s + s + s;
            String args1[] = {
                "abc", "ab?c", "XYZ", s
            };
            for(int j = 0; j < args1.length; j++)
            {
                String s1 = args1[j];
                String s2 = s1;
                if(s1.length() > 16)
                    s2 = s2.substring(0, 16) + "...";
                System.out.println("testing " + characterset1 + " against <" + s2 + ">");
                boolean flag = true;
                try
                {
                    byte abyte0[] = characterset1.convertWithReplacement(s1);
                    String s3 = characterset1.toStringWithReplacement(abyte0, 0, abyte0.length);
                    abyte0 = characterset1.convert(s3);
                    String s4 = characterset1.toString(abyte0, 0, abyte0.length);
                    if(!s3.equals(s4))
                    {
                        System.out.println("    FAILED roundTrip " + s4);
                        flag = false;
                    }
                    if(characterset1.isLossyFrom(characterset))
                    {
                        try
                        {
                            byte abyte1[] = characterset1.convert(s1);
                            String s5 = characterset1.toString(abyte1, 0, abyte1.length);
                            if(!s5.equals(s4))
                                System.out.println("    FAILED roundtrip, no throw");
                        }
                        catch(SQLException sqlexception) { }
                    } else
                    {
                        if(!s4.equals(s1))
                        {
                            System.out.println("    FAILED roundTrip " + s4);
                            flag = false;
                        }
                        byte abyte2[] = characterset.convert(s1);
                        byte abyte3[] = characterset1.convert(characterset, abyte2, 0, abyte2.length);
                        String s6 = characterset1.toString(abyte3, 0, abyte3.length);
                        if(!s6.equals(s1))
                        {
                            System.out.println("    FAILED withoutReplacement " + s6);
                            flag = false;
                        }
                    }
                }
                catch(Exception exception)
                {
                    System.out.println("    FAILED with Exception " + exception);
                }
                if(flag)
                    System.out.println("    PASSED " + (characterset1.isLossyFrom(characterset) ? "LOSSY" : ""));
            }

        }

    }

    public static final short DEFAULT_CHARSET = -1;
    public static final short ASCII_CHARSET = 1;
    public static final short ISO_LATIN_1_CHARSET = 31;
    public static final short UNICODE_1_CHARSET = 870;
    public static final short UNICODE_2_CHARSET = 871;
}
