package com.meidusa.amoeba.manager.landscape;

/**
 * User: Sun Ning <Classicning@gmail.com>
 * Date: 1/11/11
 * Time: 8:37 PM
 */
public interface AmoebaInstanceType {

    public static final int AMOEBA_MYSQL = 1 << 0;

    public static final int AMOEBA_MEMCACHED = 1 << 2;

    public static final int AMOEBA_ALADDIN = 1 << 3;

    public static final int AMOEBA_MONGODB = 1<<4;
}
