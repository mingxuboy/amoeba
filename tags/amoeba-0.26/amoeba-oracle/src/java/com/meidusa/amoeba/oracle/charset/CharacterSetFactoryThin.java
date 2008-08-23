package com.meidusa.amoeba.oracle.charset;


class CharacterSetFactoryThin extends CharacterSetFactory
{

    CharacterSetFactoryThin()
    {
    }

    public CharacterSet make(int i)
    {
        if(i == -1)
            i = 31;
        if(i == 2000)
            return new CharacterSetAL16UTF16(i);
        if(i == 870 || i == 871)
            return new CharacterSetUTF(i);
        if(i == 873)
            return new CharacterSetAL32UTF8(i);
        if(i == 872)
            return new CharacterSetUTFE(i);
        if(i == 2002)
            return new CharacterSetAL16UTF16LE(i);
        CharacterSet characterset = CharacterSetWithConverter.getInstance(i);
        if(characterset != null)
            return characterset;
        else
            return new CharacterSetUnknown(i);
    }
}
