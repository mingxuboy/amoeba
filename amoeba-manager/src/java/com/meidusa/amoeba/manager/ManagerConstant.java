package com.meidusa.amoeba.manager;

public interface ManagerConstant {

    int    HEADER_SIZE               = 5;
    
    byte   FUN_TYPE_OBJECT           = 1;
    byte   FUN_TYPE_PING             = 2;
    byte   FUN_TYPE_PONG             = 3;
    byte   FUN_TYPE_OK               = 4;

    byte   FUN_TYPE_DBSERVER_ADD     = 11;
    byte   FUN_TYPE_DBSERVER_DELETE  = 12;
    byte   FUN_TYPE_DBSERVER_UPDATE  = 13;
    byte   FUN_TYPE_DBSERVER_DSIABLE = 14;
    byte   FUN_TYPE_DBSERVER_ENABLE  = 15;
    
    byte   FUN_TYPE_RULE_UPDATE      = 20;
    byte   FUN_TYPE_RULE_ADD         = 21;
    byte   FUN_TYPE_RULE_DELETE      = 22;
    byte   FUN_TYPE_AMOEBA_RELOAD    = 31;
    byte   FUN_TYPE_AMOEBA_SHUTDOWN  = -1;

    byte[] HEADER_PAD                = new byte[HEADER_SIZE];
}
