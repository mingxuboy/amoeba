package com.meidusa.amoeba.oracle.charset;

import java.sql.SQLException;

import oracle.sql.converter.CharacterConverters;

class CharacterSet2ByteFixed extends CharacterSetWithConverter {

    CharacterSet2ByteFixed(int i, CharacterConverters characterconverters){
        super(i, characterconverters);
    }

    static CharacterSet2ByteFixed getInstance(int i, CharacterConverters characterconverters) {
        if (characterconverters.getGroupId() == 6) return new CharacterSet2ByteFixed(i, characterconverters);
        else return null;
    }

    int decode(CharacterWalker characterwalker) throws SQLException {
        int i = characterwalker.bytes[characterwalker.next] & 0xff;
        characterwalker.next++;
        if (characterwalker.bytes.length > characterwalker.next) {
            i = i << 8 | characterwalker.bytes[characterwalker.next];
            characterwalker.next++;
        } else {
            throw new SQLException("destination too small");
        }
        return i;
    }

    void encode(CharacterBuffer characterbuffer, int i) throws SQLException {
        need(characterbuffer, 2);
        characterbuffer.bytes[characterbuffer.next++] = (byte) (i >> 8 & 0xff);
        characterbuffer.bytes[characterbuffer.next++] = (byte) (i & 0xff);
    }

    static final String CHAR_CONV_SUPERCLASS_NAME = "oracle.sql.converter.CharacterConverter2ByteFixed";
    static final short  MAX_7BIT                  = 127;
    static final short  MIN_8BIT_SB               = 161;
    static final short  MAX_8BIT_SB               = 223;
    static final short  CHARLENGTH                = 2;
    static Class        m_charConvSuperclass;
}
