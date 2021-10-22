package com.didi.arius.gateway.common.utils;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.exception.InvalidParameterException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DateUtil {
    protected static final List<String> timePatterns = Arrays.asList( "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS Z", "yyyy-MM-dd'T'HH:mm:ssZ");

    private static final long MILLIS_ZONE_OFFSET = LocalDateTime.of(1970, 1, 1, 0, 0, 0,
            0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    private static LoadingCache<Long, Map<String, String>> dayFormatCache = CacheBuilder.newBuilder().concurrencyLevel(20).expireAfterWrite(5,
            TimeUnit.MINUTES).initialCapacity(60).maximumSize(100).recordStats().build(new CacheLoader<Long, Map<String, String>>() {

        @Override
        public Map<String, String> load(Long key) {
            return new ConcurrentHashMap<>();
        }
    });

    public static long transformToMillis(String date) throws InvalidParameterException {
        long messageTime = 0;

        if (StringUtils.isNumeric(date)) {
            if (date.length() == 13) {
                messageTime = Long.parseLong(date);
            } else if (date.length() == 10) {
                messageTime = Long.parseLong(date);
                messageTime = messageTime * 1000;
            }
        } else if (!StringUtils.isEmpty(date)) {
            for (String timePattern : timePatterns) {
                try {
                    messageTime = DateTime.parse(date, DateTimeFormat.forPattern(timePattern)).getMillis();
                    break;
                } catch (Throwable e) {
                    // pass
                }
            }
        }

        if (messageTime == 0) {
            throw new InvalidParameterException("date format error, date=" + date);
        }

        return messageTime;
    }

    public static String transformToDateFormat(long time, String dateFormat) {
        // 需要校准时区之差对应的时间
        long key = (time - MILLIS_ZONE_OFFSET) / QueryConsts.DAY_MILLIS;
        String dateFormatTime = null;
        dateFormat = dateFormat.replace('Y', 'y');

        try {
            // 从缓存中获取
            Map<String, String> format2DayValueMap = dayFormatCache.get(key);

            // 如果该时间在缓存中找不到，则计算, 这里不会执行到，因为在构建dayFormatCache设置了load回调来放入新的key
            if (null == format2DayValueMap) {

                format2DayValueMap = new ConcurrentHashMap<>();
                // 不包含该日期转换格式则计算，然后放入map中

                Instant instant = Instant.ofEpochMilli(time);
                LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                dateFormatTime = DateTimeFormatter.ofPattern(dateFormat).format(dateTime);

                format2DayValueMap.put(dateFormat, dateFormatTime);
                // 放入缓存中
                dayFormatCache.put(key, format2DayValueMap);
            } else {
                // 如果包含了该日期转换格式
                if (format2DayValueMap.containsKey(dateFormat)) {
                    dateFormatTime = format2DayValueMap.get(dateFormat);
                } else {
                    // 不包含该日期转换格式则计算，然后放入map中
                    Instant instant = Instant.ofEpochMilli(time);
                    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

                    dateFormatTime = DateTimeFormatter.ofPattern(dateFormat).format(dateTime);

                    // 有效的才放入map中
                    format2DayValueMap.put(dateFormat, dateFormatTime);
                }
            }
        } catch (Throwable e) {

        }

        return dateFormatTime;
    }

    public static List<String> getDateFormatSuffix(long start, long end, String dateFormat) {
        List<String> suffixes = new ArrayList<>();

        if (start > end) {
            return suffixes;
        }

        DateTime startDate = new DateTime(start);
        DateTime endDate = new DateTime(end);

        String startSuffix = startDate.toString(dateFormat);
        String endSuffix = endDate.toString(dateFormat);

        suffixes.add(startSuffix);

        String lastSuffix = startSuffix;
        if (dateFormat.endsWith("dd")) {
            while (startDate.plusDays(1).getMillis() < endDate.getMillis()) {
                startDate = startDate.plusDays(1);

                String suffix = startDate.toString(dateFormat);
                suffixes.add(suffix);

                lastSuffix = suffix;
            }
        } else if (dateFormat.endsWith("MM")) {
            while (startDate.plusMonths(1).getMillis() < endDate.getMillis()) {
                startDate = startDate.plusMonths(1);

                String suffix = startDate.toString(dateFormat);
                suffixes.add(suffix);

                lastSuffix = suffix;
            }
        } else if (dateFormat.toLowerCase().endsWith("yy")) {
            while (startDate.plusYears(1).getMillis() < endDate.getMillis()) {
                startDate = startDate.plusYears(1);

                String suffix = startDate.toString(dateFormat);
                suffixes.add(suffix);

                lastSuffix = suffix;
            }
        }

        if (false == endSuffix.equals(lastSuffix)) {
            suffixes.add(endSuffix);
        }

        return suffixes;
    }
}
