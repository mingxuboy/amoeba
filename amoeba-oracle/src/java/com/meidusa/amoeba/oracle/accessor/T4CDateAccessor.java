package com.meidusa.amoeba.oracle.accessor;

import java.sql.Date;
import java.util.Calendar;
import java.util.TimeZone;

public class T4CDateAccessor extends DateAccessor {

    public static Date getDate(byte[] dataBytes) {
        TimeZone defaultTZ = TimeZone.getDefault();
        Calendar calendar = Calendar.getInstance(defaultTZ);

        Date date = null;

        int j = 1;
        int y = oracleYear(dataBytes, j);
        int m = oracleMonth(dataBytes, j);
        int d = oracleDay(dataBytes, j);
        calendar.set(y, m, d, 0, 0, 0);
        calendar.set(14, 0);
        if (y > 0 && calendar.isSet(0)) {
            calendar.set(0, 1);
        }

        date = new Date(calendar.getTimeInMillis());

        return date;
    }

}
