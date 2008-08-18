// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name: CharacterConverterJDBC.java

package com.meidusa.amoeba.oracle.charset;

import java.util.HashMap;
import java.util.Hashtable;

import oracle.sql.converter.CharacterConverters;

// Referenced classes of package oracle.sql.converter:
// CharacterConverters

public abstract class CharacterConverterJDBC extends CharacterConverters {

    public CharacterConverterJDBC(){
    }

    public static CharacterConverters getInstance(int i) {
        Object obj = null;
        boolean flag = false;
        boolean flag1 = false;
        String s = Integer.toHexString(i);
        HashMap hashmap = m_converterStore;

        synchronized (hashmap) {
            CharacterConverterJDBC characterconverterjdbc = (CharacterConverterJDBC) m_converterStore.get(s);
            if (characterconverterjdbc != null)
                return characterconverterjdbc;
            String s1 = "converter_xcharset/lx2" + "0000".substring(0, 4 - s.length()) + s;
            ConverterArchive converterarchive = new ConverterArchive();
            characterconverterjdbc = (CharacterConverterJDBC) converterarchive.readObj(s1 + ".glb");

            if (characterconverterjdbc != null) {
                characterconverterjdbc.buildUnicodeToOracleMapping();
                m_converterStore.put(s, characterconverterjdbc);
                return characterconverterjdbc;
            } else {
                return null;
            }
        }

        // JVM INSTR monitorenter ;
        // CharacterConverterJDBC characterconverterjdbc = (CharacterConverterJDBC)m_converterStore.get(s);
        // if(characterconverterjdbc != null)
        // return characterconverterjdbc;
        // String s1 = "converter_xcharset/lx2" + "0000".substring(0, 4 - s.length()) + s;
        // ConverterArchive converterarchive = new ConverterArchive();
        // characterconverterjdbc = (CharacterConverterJDBC)converterarchive.readObj(s1 + ".glb");
        // if(characterconverterjdbc != null) goto _L2; else goto _L1
        // _L1:
        // null;
        // hashmap;
        // JVM INSTR monitorexit ;
        // return;
        // _L2:
        // characterconverterjdbc.buildUnicodeToOracleMapping();
        // m_converterStore.put(s, characterconverterjdbc);
        // characterconverterjdbc;
        // hashmap;
        // JVM INSTR monitorexit ;
        // return;
        // Exception exception;
        // exception;
        // throw exception;
    }

    protected void storeMappingRange(int i, Hashtable hashtable, Hashtable hashtable1) {
        int j = i >> 24 & 0xff;
        int k = i >> 16 & 0xff;
        int l = i >> 8 & 0xff;
        int i1 = i & 0xff;
        Integer integer = new Integer(j);
        Integer integer1 = new Integer(i >> 16 & 0xffff);
        Integer integer2 = new Integer(i >> 8 & 0xffffff);
        if (i >>> 26 == 54) {
            char ac[] = (char[]) hashtable.get(integer);
            if (ac == null)
                ac = (new char[] { '\377', '\0' });
            if (ac[0] == '\377' && ac[1] == 0) {
                ac[0] = (char) k;
                ac[1] = (char) k;
            } else {
                if (k < (ac[0] & 0xffff))
                    ac[0] = (char) k;
                if (k > (ac[0] & 0xffff))
                    ac[1] = (char) k;
            }
            hashtable.put(integer, ac);
            ac = (char[]) hashtable.get(integer1);
            if (ac == null)
                ac = (new char[] { '\377', '\0' });
            if (ac[0] == '\377' && ac[1] == 0) {
                ac[0] = (char) l;
                ac[1] = (char) l;
            } else {
                if (l < (ac[0] & 0xffff))
                    ac[0] = (char) l;
                if (l > (ac[0] & 0xffff))
                    ac[1] = (char) l;
            }
            hashtable.put(integer1, ac);
        }
        char ac1[] = (char[]) hashtable1.get(integer2);
        if (ac1 == null)
            ac1 = (new char[] { '\377', '\0' });
        if (ac1[0] == '\377' && ac1[1] == 0) {
            ac1[0] = (char) i1;
            ac1[1] = (char) i1;
        } else {
            if (i1 < (ac1[0] & 0xffff))
                ac1[0] = (char) i1;
            if (i1 > (ac1[0] & 0xffff))
                ac1[1] = (char) i1;
        }
        hashtable1.put(integer2, ac1);
    }

    static final String  CONVERTERNAMEPREFIX = "converter_xcharset/lx2";
    static final String  CONVERTERIDPREFIX   = "0000";
    static final int     HIBYTEMASK          = 65280;
    static final int     LOWBYTEMASK         = 255;
    static final int     STORE_INCREMENT     = 10;
    static final int     INVALID_ORA_CHAR    = -1;
    static final int     FIRSTBSHIFT         = 24;
    static final int     SECONDBSHIFT        = 16;
    static final int     THIRDBSHIFT         = 8;
    static final int     UB2MASK             = 65535;
    static final int     UB4MASK             = 65535;
    static final HashMap m_converterStore    = new HashMap();

}
