package com.didi.arius.gateway.metrics.util;

import java.text.SimpleDateFormat;

public class TimeUtils {
    public static final String TIME_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static String formatTimestamp(long millseconds, String formateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(formateStr);
        return sdf.format(new java.util.Date(millseconds));
    }
}
