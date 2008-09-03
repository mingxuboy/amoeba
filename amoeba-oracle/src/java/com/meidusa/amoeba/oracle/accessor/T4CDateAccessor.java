package com.meidusa.amoeba.oracle.accessor;

import java.sql.Date;
import java.util.Calendar;
import java.util.TimeZone;

public class T4CDateAccessor extends DateAccessor {

    public static Date getDate(byte[] data) {
        TimeZone defaultTZ = TimeZone.getDefault();
        Calendar calendar = Calendar.getInstance(defaultTZ);

        Date date = null;

        int y = oracleYear(data);
        int m = oracleMonth(data);
        int d = oracleDay(data);
        calendar.set(y, m, d, 0, 0, 0);
        calendar.set(14, 0);
        if (y > 0 && calendar.isSet(0)) {
            calendar.set(0, 1);
        }

        date = new Date(calendar.getTimeInMillis());

        return date;
    }

}
