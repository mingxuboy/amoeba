// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

package com.meidusa.amoeba.oracle.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

// Referenced classes of package oracle.net.ns:
// Message

public class Message11 implements Message {

    public Message11(){
    }

    public String getMessage(int i, String s) {
        try {
            rBundle = ResourceBundle.getBundle("oracle.net.mesg.Message");
        } catch (Exception exception) {
            return "Message file 'oracle.net.mesg.Message' is missing.";
        }
        try {
            msg = number2string(i, s);
        } catch (MissingResourceException missingresourceexception) {
            msg = "Undefined Error";
        }
        return msg;
    }

    private String number2string(int i, String s) throws MissingResourceException {
        String s1 = null;
        String s2 = s != null ? s : "";
        if (i > 12000) {
            if (i >= 12500 && i <= 12530)
                s1 = rBundle.getString("LISTENER_REFUSES_CONNECTION") + ":\n" + "ORA-" + i + ", "
                     + rBundle.getString(String.valueOf(i)) + "\n" + rBundle.getString("CONNECT_DESCRIPTOR_USED")
                     + ":\n" + s2;
            else
                s1 = rBundle.getString("ORACLE_ERROR") + " ORA-" + i;
            return s1;
        }
        switch (i) {
            case 0: // '\0'
                s1 = rBundle.getString("GOT_MINUS_ONE") + s2;
                break;

            case 1: // '\001'
                s1 = rBundle.getString("ASSERTION_FAILED") + s2;
                break;

            case 20: // '\024'
                s1 = rBundle.getString("NT_CONNECTION_FAILED") + s2;
                break;

            case 21: // '\025'
                s1 = rBundle.getString("INVALID_NT_ADAPTER") + s2;
                break;

            case 100: // 'd'
                s1 = rBundle.getString("PROTOCOL_NOT_SPECIFIED") + s2;
                break;

            case 101: // 'e'
                s1 = rBundle.getString("CSTRING_PARSING") + s2;
                break;

            case 102: // 'f'
                s1 = rBundle.getString("INVALID_CONNECT_DATA") + s2;
                break;

            case 103: // 'g'
                s1 = rBundle.getString("HOSTNAME_NOT_SPECIFIED") + s2;
                break;

            case 104: // 'h'
                s1 = rBundle.getString("PORT_NOT_SPECIFIED") + s2;
                break;

            case 105: // 'i'
                s1 = rBundle.getString("CONNECT_DATA_MISSING") + s2;
                break;

            case 106: // 'j'
                s1 = rBundle.getString("SID_INFORMATION_MISSING") + s2;
                break;

            case 107: // 'k'
                s1 = rBundle.getString("ADDRESS_NOT_DEFINED") + s2;
                break;

            case 108: // 'l'
                s1 = rBundle.getString("JNDI_THREW_EXCEPTION") + s2;
                break;

            case 109: // 'm'
                s1 = rBundle.getString("JNDI_NOT_INITIALIZED") + s2;
                break;

            case 110: // 'n'
                s1 = rBundle.getString("JNDI_CLASSES_NOT_FOUND") + s2;
                break;

            case 111: // 'o'
                s1 = rBundle.getString("USER_PROPERTIES_NOT_DEFINED") + s2;
                break;

            case 112: // 'p'
                s1 = rBundle.getString("NAMING_FACTORY_NOT_DEFINED") + s2;
                break;

            case 113: // 'q'
                s1 = rBundle.getString("NAMING_PROVIDER_NOT_DEFINED") + s2;
                break;

            case 114: // 'r'
                s1 = rBundle.getString("PROFILE_NAME_NOT_DEFINED") + s2;
                break;

            case 115: // 's'
                s1 = rBundle.getString("HOST_PORT_SID_EXPECTED") + s2;
                break;

            case 116: // 't'
                s1 = rBundle.getString("PORT_NUMBER_ERROR") + s2;
                break;

            case 117: // 'u'
                s1 = rBundle.getString("EZ_CONNECT_FORMAT_EXPECTED") + s2;
                break;

            case 118: // 'v'
                s1 = rBundle.getString("EZ_CONNECT_UNKNOWN_HOST") + s2;
                break;

            case 121: // 'y'
                s1 = rBundle.getString("INVALID_READ_PATH") + s2;
                break;

            case 119: // 'w'
                s1 = rBundle.getString("TNS_ADMIN_EMPTY") + s2;
                break;

            case 120: // 'x'
                s1 = rBundle.getString("CONNECT_STRING_EMPTY") + s2;
                break;

            case 122: // 'z'
                s1 = rBundle.getString("NAMELOOKUP_FAILED") + s2;
                break;

            case 123: // '{'
                s1 = rBundle.getString("NAMELOOKUP_FILE_ERROR") + s2;
                break;

            case 124: // '|'
                s1 = rBundle.getString("INVALID_LDAP_URL") + s2;
                break;

            case 200:
                s1 = rBundle.getString("NOT_CONNECTED") + s2;
                break;

            case 201:
                s1 = rBundle.getString("CONNECTED_ALREADY") + s2;
                break;

            case 202:
                s1 = rBundle.getString("DATA_EOF") + s2;
                break;

            case 203:
                s1 = rBundle.getString("SDU_MISMATCH") + s2;
                break;

            case 204:
                s1 = rBundle.getString("BAD_PKT_TYPE") + s2;
                break;

            case 205:
                s1 = rBundle.getString("UNEXPECTED_PKT") + s2;
                break;

            case 206:
                s1 = rBundle.getString("REFUSED_CONNECT") + s2;
                break;

            case 207:
                s1 = rBundle.getString("INVALID_PKT_LENGTH") + s2;
                break;

            case 208:
                s1 = rBundle.getString("CONNECTION_STRING_NULL") + s2;
                break;

            case 300:
                s1 = rBundle.getString("FAILED_TO_TURN_ENCRYPTION_ON") + s2;
                break;

            case 301:
                s1 = rBundle.getString("WRONG_BYTES_IN_NAPACKET") + s2;
                break;

            case 302:
                s1 = rBundle.getString("WRONG_MAGIC_NUMBER") + s2;
                break;

            case 303:
                s1 = rBundle.getString("UNKNOWN_ALGORITHM_12649") + s2;
                break;

            case 304:
                s1 = rBundle.getString("INVALID_ENCRYPTION_PARAMETER") + s2;
                break;

            case 305:
                s1 = rBundle.getString("WRONG_SERVICE_SUBPACKETS") + s2;
                break;

            case 306:
                s1 = rBundle.getString("SUPERVISOR_STATUS_FAILURE") + s2;
                break;

            case 307:
                s1 = rBundle.getString("AUTHENTICATION_STATUS_FAILURE") + s2;
                break;

            case 308:
                s1 = rBundle.getString("SERVICE_CLASSES_NOT_INSTALLED") + s2;
                break;

            case 309:
                s1 = rBundle.getString("INVALID_DRIVER") + s2;
                break;

            case 310:
                s1 = rBundle.getString("ARRAY_HEADER_ERROR") + s2;
                break;

            case 311:
                s1 = rBundle.getString("RECEIVED_UNEXPECTED_LENGTH_FOR_TYPE") + s2;
                break;

            case 312:
                s1 = rBundle.getString("INVALID_NA_PACKET_TYPE_LENGTH") + s2;
                break;

            case 313:
                s1 = rBundle.getString("INVALID_NA_PACKET_TYPE") + s2;
                break;

            case 314:
                s1 = rBundle.getString("UNEXPECTED_NA_PACKET_TYPE_RECEIVED") + s2;
                break;

            case 315:
                s1 = rBundle.getString("UNKNOWN_ENC_OR_DATAINT_ALGORITHM") + s2;
                break;

            case 316:
                s1 = rBundle.getString("INVALID_ENCRYPTION_ALGORITHM_FROM_SERVER") + s2;
                break;

            case 317:
                s1 = rBundle.getString("ENCRYPTION_CLASS_NOT_INSTALLED") + s2;
                break;

            case 318:
                s1 = rBundle.getString("DATAINTEGRITY_CLASS_NOT_INSTALLED") + s2;
                break;

            case 319:
                s1 = rBundle.getString("INVALID_DATAINTEGRITY_ALGORITHM_FROM_SERVER") + s2;
                break;

            case 320:
                s1 = rBundle.getString("INVALID_SERVICES_FROM_SERVER") + s2;
                break;

            case 321:
                s1 = rBundle.getString("INCOMPLETE_SERVICES_FROM_SERVER") + s2;
                break;

            case 322:
                s1 = rBundle.getString("INVALID_LEVEL") + s2;
                break;

            case 323:
                s1 = rBundle.getString("INVALID_SERVICE") + s2;
                break;

            case 400:
                s1 = rBundle.getString("INVALID_SSL_VERSION") + s2;
                break;

            case 401:
                s1 = rBundle.getString("UNSUPPORTED_SSL_PROTOCOL") + s2;
                break;

            case 403:
                s1 = rBundle.getString("INVALID_SSL_CIPHER_SUITES") + s2;
                break;

            case 404:
                s1 = rBundle.getString("UNSUPPORTED_CIPHER_SUITE") + s2;
                break;

            case 405:
                s1 = rBundle.getString("MISMATCH_SERVER_CERT_DN") + s2;
                break;

            case 406:
                s1 = rBundle.getString("DOUBLE_ENCRYPTION_NOT_ALLOWED") + s2;
                break;

            case 407:
                s1 = rBundle.getString("UNABLE_TO_PARSE_WALLET_LOCATION") + s2;
                break;

            case 408:
                s1 = rBundle.getString("UNABLE_TO_INIT_KEY_STORE") + s2;
                break;

            case 409:
                s1 = rBundle.getString("UNABLE_TO_INIT_TRUST_STORE") + s2;
                break;

            case 410:
                s1 = rBundle.getString("UNABLE_TO_INIT_SSL_CONTEXT") + s2;
                break;

            case 411:
                s1 = rBundle.getString("SSL_UNVERIFIED_PEER") + s2;
                break;

            case 412:
                s1 = rBundle.getString("UNSUPPORTED_METHOD_IN_WALLET_LOCATION") + s2;
                break;

            case 500:
                s1 = rBundle.getString("NS_BREAK") + s2;
                break;

            case 501:
                s1 = rBundle.getString("NL_EXCEPTION") + s2;
                break;

            case 502:
                s1 = rBundle.getString("SO_EXCEPTION") + s2;
                break;

            case 503:
                s1 = rBundle.getString("SO_CONNECTTIMEDOUT") + s2;
                break;

            case 504:
                s1 = rBundle.getString("SO_READTIMEDOUT") + s2;
                break;

            case 505:
                s1 = rBundle.getString("INVALID_CONNECTTIMEOUT") + s2;
                break;

            case 506:
                s1 = rBundle.getString("INVALID_READTIMEOUT") + s2;
                break;

            default:
                s1 = rBundle.getString("UNDEFINED_ERROR") + s2;
                break;
        }
        return s1;
    }

    private static final boolean DEBUG       = false;
    private String               msg;
    private ResourceBundle       rBundle;
    private static final String  messageFile = "oracle.net.mesg.Message";
}
