package com.meidusa.amoeba.oracle.net.packet;

public interface T4CTTIoAuth {

    static final int    SERVER_VERSION_81           = 8100;
    static final int    KPZ_LOGON                   = 1;
    static final int    KPZ_CPW                     = 2;
    static final int    KPZ_SRVAUTH                 = 4;
    static final int    KPZ_ENCRYPTED_PASSWD        = 256;
    static final int    KPZ_LOGON_MIGRATE           = 16;
    static final int    KPZ_LOGON_SYSDBA            = 32;
    static final int    KPZ_LOGON_SYSOPER           = 64;
    static final int    KPZ_LOGON_PRELIMAUTH        = 128;
    static final int    KPZ_PASSWD_ENCRYPTED        = 256;
    static final int    KPZ_LOGON_DBCONC            = 512;
    static final int    KPZ_PROXY_AUTH              = 1024;
    static final int    KPZ_SESSION_CACHE           = 2048;
    static final int    KPZ_PASSWD_IS_VFR           = 4096;

    static final String AUTH_VERSION_STRING         = "AUTH_VERSION_STRING";
    static final String AUTH_CAPABILITY_TABLE       = "AUTH_CAPABILITY_TABLE";
    static final String AUTH_NLS_LXLAN              = "AUTH_NLS_LXLAN";
    static final String AUTH_NLS_LXCTERRITORY       = "AUTH_NLS_LXCTERRITORY";
    static final String AUTH_NLS_LXCCURRENCY        = "AUTH_NLS_LXCCURRENCY";
    static final String AUTH_NLS_LXCISOCURR         = "AUTH_NLS_LXCISOCURR";
    static final String AUTH_NLS_LXCNUMERICS        = "AUTH_NLS_LXCNUMERICS";
    static final String AUTH_NLS_LXCDATEFM          = "AUTH_NLS_LXCDATEFM";
    static final String AUTH_NLS_LXCDATELANG        = "AUTH_NLS_LXCDATELANG";
    static final String AUTH_NLS_LXCSORT            = "AUTH_NLS_LXCSORT";
    static final String AUTH_NLS_LXCCALENDAR        = "AUTH_NLS_LXCCALENDAR";
    static final String AUTH_NLS_LXCUNIONCUR        = "AUTH_NLS_LXCUNIONCUR";
    static final String AUTH_NLS_LXCTIMEFM          = "AUTH_NLS_LXCTIMEFM";
    static final String AUTH_NLS_LXCSTMPFM          = "AUTH_NLS_LXCSTMPFM";
    static final String AUTH_NLS_LXCTTZNFM          = "AUTH_NLS_LXCTTZNFM";
    static final String AUTH_NLS_LXCSTZNFM          = "AUTH_NLS_LXCSTZNFM";

    static final String AUTH_TERMINAL               = "AUTH_TERMINAL";
    static final String AUTH_PROGRAM_NM             = "AUTH_PROGRAM_NM";
    static final String AUTH_MACHINE                = "AUTH_MACHINE";
    static final String AUTH_PID                    = "AUTH_PID";
    static final String AUTH_SID                    = "AUTH_SID";
    static final String AUTH_SESSKEY                = "AUTH_SESSKEY";
    static final String AUTH_VFR_DATA               = "AUTH_VFR_DATA";
    static final String AUTH_PASSWORD               = "AUTH_PASSWORD";
    static final String AUTH_INTERNALNAME           = "AUTH_INTERNALNAME_";
    static final String AUTH_EXTERNALNAME           = "AUTH_EXTERNALNAME_";
    static final String AUTH_ACL                    = "AUTH_ACL";
    static final String AUTH_ALTER_SESSION          = "AUTH_ALTER_SESSION";
    static final String AUTH_INITIAL_CLIENT_ROLE    = "INITIAL_CLIENT_ROLE";
    static final String AUTH_VERSION_SQL            = "AUTH_VERSION_SQL";
    static final String AUTH_VERSION_NO             = "AUTH_VERSION_NO";
    static final String AUTH_XACTION_TRAITS         = "AUTH_XACTION_TRAITS";
    static final String AUTH_VERSION_STATUS         = "AUTH_VERSION_STATUS";
    static final String AUTH_SERIAL_NUM             = "AUTH_SERIAL_NUM";
    static final String AUTH_SESSION_ID             = "AUTH_SESSION_ID";
    static final String AUTH_CLIENT_CERTIFICATE     = "AUTH_CLIENT_CERTIFICATE";
    static final String AUTH_PROXY_CLIENT_NAME      = "PROXY_CLIENT_NAME";
    static final String AUTH_CLIENT_DN              = "AUTH_CLIENT_DISTINGUISHED_NAME";
    static final String AUTH_INSTANCENAME           = "AUTH_INSTANCENAME";
    static final String AUTH_DBNAME                 = "AUTH_DBNAME";
    static final String AUTH_INSTANCE_NO            = "AUTH_INSTANCE_NO";
    static final String AUTH_SC_SERVER_HOST         = "AUTH_SC_SERVER_HOST";
    static final String AUTH_SC_INSTANCE_NAME       = "AUTH_SC_INSTANCE_NAME";
    static final String AUTH_SC_INSTANCE_ID         = "AUTH_SC_INSTANCE_ID";
    static final String AUTH_SC_INSTANCE_START_TIME = "AUTH_SC_INSTANCE_START_TIME";
    static final String AUTH_SC_DBUNIQUE_NAME       = "AUTH_SC_DBUNIQUE_NAME";
    static final String AUTH_SC_SERVICE_NAME        = "AUTH_SC_SERVICE_NAME";
    static final String AUTH_SC_SVC_FLAGS           = "AUTH_SC_SVC_FLAGS";
    static final String AUTH_COPYRIGHT              = "AUTH_COPYRIGHT";
    static final String COPYRIGHT_STR               = "\"Oracle\nEverybody follows\nSpeedy bits exchange\nStars await to glow\"\nThe preceding key is copyrighted by Oracle Corporation.\nDuplication of this key is not allowed without permission\nfrom Oracle Corporation. Copyright 2003 Oracle Corporation.";

}
