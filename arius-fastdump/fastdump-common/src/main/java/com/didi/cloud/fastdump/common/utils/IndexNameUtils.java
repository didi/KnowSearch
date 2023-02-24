package com.didi.cloud.fastdump.common.utils;

/**
 * Created by linyunan on 2022/11/22
 */
public class IndexNameUtils {
    private IndexNameUtils() {
    }

    private static final String VERSION_TAG = "_v";

    private static final Long   ONE_DAY     = 24 * 60 * 60 * 1000L;

    public static String genCurrentDailyIndexName(String templateName) {
        return templateName + "_" + DateTimeUtil.getFormatDayByOffset(0);
    }
}
