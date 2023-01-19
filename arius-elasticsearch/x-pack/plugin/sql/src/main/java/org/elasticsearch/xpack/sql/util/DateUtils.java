/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.sql.util;

import org.elasticsearch.common.time.DateFormatter;
import org.elasticsearch.common.time.DateFormatters;
import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.expression.Foldables;
import org.elasticsearch.xpack.sql.parser.ParsingException;
import org.elasticsearch.xpack.sql.proto.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.time.format.DateTimeFormatter.ISO_TIME;

public final class DateUtils {

    public static final ZoneId UTC = ZoneId.of("Z");
    public static final String DATE_PARSE_FORMAT = "epoch_millis";
    // In Java 8 LocalDate.EPOCH is not available, introduced with later Java versions
    public static final LocalDate EPOCH = LocalDate.of(1970, 1, 1);
    public static final long DAY_IN_MILLIS = 60 * 60 * 24 * 1000L;

    private static final DateTimeFormatter DATE_TIME_ESCAPED_LITERAL_FORMATTER = new DateTimeFormatterBuilder()
        .append(ISO_LOCAL_DATE)
        .appendLiteral(" ")
        .append(ISO_LOCAL_TIME)
        .toFormatter().withZone(UTC);

    private static final DateFormatter UTC_DATE_TIME_FORMATTER = DateFormatter.forPattern("date_optional_time").withZone(UTC);
    private static final int DEFAULT_PRECISION_FOR_CURRENT_FUNCTIONS = 3;

    private DateUtils() {}

    /**
     * Creates an date for SQL DATE type from the millis since epoch.
     */
    public static ZonedDateTime asDateOnly(long millis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), UTC).toLocalDate().atStartOfDay(UTC);
    }

    /**
     * Creates an date for SQL TIME type from the millis since epoch.
     */
    public static OffsetTime asTimeOnly(long millis) {
        return OffsetTime.ofInstant(Instant.ofEpochMilli(millis % DAY_IN_MILLIS), UTC);
    }

    /**
     * Creates an date for SQL TIME type from the millis since epoch.
     */
    public static OffsetTime asTimeOnly(long millis, ZoneId zoneId) {
        return OffsetTime.ofInstant(Instant.ofEpochMilli(millis % DAY_IN_MILLIS), zoneId);
    }

    public static OffsetTime asTimeAtZone(OffsetTime time, ZoneId zonedId) {
        return time.atDate(DateUtils.EPOCH).atZoneSameInstant(zonedId).toOffsetDateTime().toOffsetTime();
    }

    /**
     * Creates a datetime from the millis since epoch (thus the time-zone is UTC).
     */
    public static ZonedDateTime asDateTime(long millis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), UTC);
    }

    /**
     * Creates a datetime from the millis since epoch then translates the date into the given timezone.
     */
    public static ZonedDateTime asDateTime(long millis, ZoneId id) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), id);
    }

    /**
     * Parses the given string into a Date (SQL DATE type) using UTC as a default timezone.
     */
    public static ZonedDateTime asDateOnly(String dateFormat) {
        return LocalDate.parse(dateFormat, ISO_LOCAL_DATE).atStartOfDay(UTC);
    }

    public static ZonedDateTime asDateOnly(ZonedDateTime zdt) {
        return zdt.toLocalDate().atStartOfDay(zdt.getZone());
    }

    public static OffsetTime asTimeOnly(String timeFormat) {
        return DateFormatters.from(ISO_TIME.parse(timeFormat)).toOffsetDateTime().toOffsetTime();
    }

    /**
     * Parses the given string into a DateTime using UTC as a default timezone.
     */
    public static ZonedDateTime asDateTime(String dateFormat) {
        return DateFormatters.from(UTC_DATE_TIME_FORMATTER.parse(dateFormat)).withZoneSameInstant(UTC);
    }

    public static ZonedDateTime ofEscapedLiteral(String dateFormat) {
        return ZonedDateTime.parse(dateFormat, DATE_TIME_ESCAPED_LITERAL_FORMATTER.withZone(UTC));
    }

    public static String toString(ZonedDateTime dateTime) {
        return StringUtils.toString(dateTime);
    }

    public static String toDateString(ZonedDateTime date) {
        return date.format(ISO_LOCAL_DATE);
    }

    public static String toTimeString(OffsetTime time) {
        return StringUtils.toString(time);
    }

    public static long minDayInterval(long l) {
        if (l < DAY_IN_MILLIS ) {
            return DAY_IN_MILLIS;
        }
        return l - (l % DAY_IN_MILLIS);
    }

    public static int getNanoPrecision(Expression precisionExpression, int nano) {
        int precision = DEFAULT_PRECISION_FOR_CURRENT_FUNCTIONS;

        if (precisionExpression != null) {
            try {
                precision = Foldables.intValueOf(precisionExpression);
            } catch (Exception e) {
                throw new ParsingException(precisionExpression.source(), "invalid precision; " + e.getMessage());
            }
        }

        if (precision < 0 || precision > 9) {
            throw new ParsingException(precisionExpression.source(), "precision needs to be between [0-9], received [{}]",
                precisionExpression.sourceText());
        }

        // remove the remainder
        nano = nano - nano % (int) Math.pow(10, (9 - precision));
        return nano;
    }
}
