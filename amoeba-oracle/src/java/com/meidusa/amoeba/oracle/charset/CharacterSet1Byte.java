// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name: CharacterSet1Byte.java

package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

import oracle.sql.converter.CharacterConverters;

// Referenced classes of package oracle.sql:
// CharacterSetWithConverter, CharacterWalker, CharacterBuffer

class CharacterSet1Byte extends CharacterSetWithConverter {

    CharacterSet1Byte(int i, CharacterConverters characterconverters){
        super(i, characterconverters);
    }

    static CharacterSet1Byte getInstance(int i, CharacterConverters characterconverters) {
        if (characterconverters.getGroupId() == 0) return new CharacterSet1Byte(i, characterconverters);
        else return null;
    }

    int decode(CharacterWalker characterwalker) throws SQLException {
        int i = characterwalker.bytes[characterwalker.next] & 0xff;
        characterwalker.next++;
        return i;
    }

    void encode(CharacterBuffer characterbuffer, int i) throws SQLException {
        need(characterbuffer, 1);
        if (i < 256) {
            characterbuffer.bytes[characterbuffer.next] = (byte) i;
            characterbuffer.next++;
        }
    }

    static final String CHAR_CONV_SUPERCLASS_NAME = "oracle.sql.converter.CharacterConverter1Byte";
    static Class        m_charConvSuperclass;
}
