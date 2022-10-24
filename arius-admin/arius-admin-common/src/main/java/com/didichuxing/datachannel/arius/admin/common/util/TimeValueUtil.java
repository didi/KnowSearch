package com.didichuxing.datachannel.arius.admin.common.util;

import org.elasticsearch.common.unit.TimeValue;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author didi
 * @date 2022-01-13 3:00 下午
 */
public class TimeValueUtil {

    private static final long     C0        = 1L;
    private static final long     C1        = C0 * 1000L;
    private static final long     C2        = C1 * 1000L;
    private static final long     C3        = C2 * 1000L;
    private static final long     C4        = C3 * 60L;
    private static final long     C5        = C4 * 60L;
    private static final long     C6        = C5 * 24L;

    public static final TimeValue MINUS_ONE = timeValueMillis(-1);
    public static final TimeValue ZERO      = timeValueMillis(0);

    public static TimeValue parseTimeValue(String sValue, String settingName) {
        settingName = Objects.requireNonNull(settingName);
        if (sValue == null) {
            return ZERO;
        }
        final String normalized = sValue.toLowerCase(Locale.ROOT).trim();
        if (normalized.endsWith("nanos")) {
            return new org.elasticsearch.common.unit.TimeValue((long) parse(sValue, normalized, "nanos"),
                TimeUnit.NANOSECONDS);
        } else if (normalized.endsWith("micros")) {
            return new org.elasticsearch.common.unit.TimeValue((long) (parse(sValue, normalized, "micros") * C1),
                TimeUnit.NANOSECONDS);
        } else if (normalized.endsWith("ms")) {
            return new org.elasticsearch.common.unit.TimeValue((long) (parse(sValue, normalized, "ms") * C2),
                TimeUnit.NANOSECONDS);
        } else if (normalized.endsWith("s")) {
            return new org.elasticsearch.common.unit.TimeValue((long) (parse(sValue, normalized, "s") * C3),
                TimeUnit.NANOSECONDS);
        } else if (sValue.endsWith("m")) {
            // parsing minutes should be case-sensitive as 'M' means "months", not "minutes"; this is the only special case.
            return new org.elasticsearch.common.unit.TimeValue((long) (parse(sValue, normalized, "m") * C4),
                TimeUnit.NANOSECONDS);
        } else if (normalized.endsWith("h")) {
            return new org.elasticsearch.common.unit.TimeValue((long) (parse(sValue, normalized, "h") * C5),
                TimeUnit.NANOSECONDS);
        } else if (normalized.endsWith("d")) {
            return new org.elasticsearch.common.unit.TimeValue((long) (parse(sValue, normalized, "d") * C6),
                TimeUnit.NANOSECONDS);
        } else if (normalized.matches("-0*1")) {
            return MINUS_ONE;
        } else if (normalized.matches("0+")) {
            return ZERO;
        } else {
            // Missing units:
            throw new IllegalArgumentException("failed to parse setting [" + settingName + "] with value [" + sValue
                                               + "] as a time value: unit is missing or unrecognized");
        }
    }

    private static double parse(final String initialInput, final String normalized, final String suffix) {
        final String s = normalized.substring(0, normalized.length() - suffix.length()).trim();
        try {
            return Double.parseDouble(s);
        } catch (final NumberFormatException e) {
            try {
                @SuppressWarnings("unused")
                final double ignored = Double.parseDouble(s);
                throw new IllegalArgumentException(
                    "failed to parse [" + initialInput + "], fractional time values are not supported", e);
            } catch (final NumberFormatException ignored) {
                throw new IllegalArgumentException("failed to parse [" + initialInput + "]", e);
            }
        }
    }

    public static TimeValue timeValueMillis(long millis) {
        return new TimeValue(millis, TimeUnit.MILLISECONDS);
    }
}
