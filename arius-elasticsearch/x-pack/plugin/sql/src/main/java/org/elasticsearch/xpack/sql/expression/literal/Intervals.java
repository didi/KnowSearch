/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.sql.expression.literal;

import org.elasticsearch.common.Strings;
import org.elasticsearch.xpack.sql.SqlIllegalArgumentException;
import org.elasticsearch.xpack.sql.expression.Foldables;
import org.elasticsearch.xpack.sql.expression.Literal;
import org.elasticsearch.xpack.sql.parser.ParsingException;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.type.DataType;
import org.elasticsearch.xpack.sql.util.Check;
import org.elasticsearch.xpack.sql.util.StringUtils;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.xpack.sql.type.DataType.INTERVAL_DAY_TO_HOUR;
import static org.elasticsearch.xpack.sql.type.DataType.INTERVAL_DAY_TO_MINUTE;
import static org.elasticsearch.xpack.sql.type.DataType.INTERVAL_DAY_TO_SECOND;
import static org.elasticsearch.xpack.sql.type.DataType.INTERVAL_HOUR_TO_MINUTE;
import static org.elasticsearch.xpack.sql.type.DataType.INTERVAL_HOUR_TO_SECOND;
import static org.elasticsearch.xpack.sql.type.DataType.INTERVAL_MINUTE_TO_SECOND;
import static org.elasticsearch.xpack.sql.type.DataType.INTERVAL_YEAR_TO_MONTH;

public final class Intervals {

    /**
     * Time unit definition - used to remember the initial declaration
     * for exposing it in ODBC.
     */
    public enum TimeUnit {
        YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILLISECOND;
    }

    private Intervals() {}

    public static long inMillis(Literal literal) {
        Object fold = Foldables.valueOf(literal);
        Check.isTrue(fold instanceof Interval, "Expected interval, received [{}]", fold);
        TemporalAmount interval = ((Interval<?>) fold).interval();
        long millis = 0;
        if (interval instanceof Period) {
            Period p = (Period) interval;
            millis = p.toTotalMonths() * 30 * 24 * 60 * 60 * 1000;
        } else {
            Duration d = (Duration) interval;
            millis = d.toMillis();
        }
        
        return millis;
    }

    public static TemporalAmount of(Source source, long duration, TimeUnit unit) {
        // Cannot use Period.of since it accepts int so use plus which accepts long
        // Further more Period and Duration have inconsistent addition methods but plus is there
        try {
            switch (unit) {
                case YEAR:
                    return Period.ZERO.plusYears(duration);
                case MONTH:
                    return Period.ZERO.plusMonths(duration);
                case DAY:
                    return Duration.ZERO.plusDays(duration);
                case HOUR:
                    return Duration.ZERO.plusHours(duration);
                case MINUTE:
                    return Duration.ZERO.plusMinutes(duration);
                case SECOND:
                    return Duration.ZERO.plusSeconds(duration);
                case MILLISECOND:
                    return Duration.ZERO.plusMillis(duration);
                default:
                    throw new ParsingException(source, "Cannot parse duration [{}]", unit);
            }
        } catch (ArithmeticException ae) {
            throw new ParsingException(source, "Value [{}] cannot be used as it is too large to convert into [{}]s", duration, unit);
        }
    }

    public static DataType intervalType(Source source, TimeUnit leading, TimeUnit trailing) {
        if (trailing == null) {
            switch (leading) {
                case YEAR:
                    return DataType.INTERVAL_YEAR;
                case MONTH:
                    return DataType.INTERVAL_MONTH;
                case DAY:
                    return DataType.INTERVAL_DAY;
                case HOUR:
                    return DataType.INTERVAL_HOUR;
                case MINUTE:
                    return DataType.INTERVAL_MINUTE;
                case SECOND:
                    return DataType.INTERVAL_SECOND;
                default:
                    throw new ParsingException(source, "Cannot determine datatype for [{}]", leading);
            }
        } else {
            if (leading == TimeUnit.YEAR && trailing == TimeUnit.MONTH) {
                return INTERVAL_YEAR_TO_MONTH;
            }
            if (leading == TimeUnit.DAY && trailing == TimeUnit.HOUR) {
                return INTERVAL_DAY_TO_HOUR;
            }
            if (leading == TimeUnit.DAY && trailing == TimeUnit.MINUTE) {
                return INTERVAL_DAY_TO_MINUTE;
            }
            if (leading == TimeUnit.DAY && trailing == TimeUnit.SECOND) {
                return INTERVAL_DAY_TO_SECOND;
            }
            if (leading == TimeUnit.HOUR && trailing == TimeUnit.MINUTE) {
                return INTERVAL_HOUR_TO_MINUTE;
            }
            if (leading == TimeUnit.HOUR && trailing == TimeUnit.SECOND) {
                return INTERVAL_HOUR_TO_SECOND;
            }
            if (leading == TimeUnit.MINUTE && trailing == TimeUnit.SECOND) {
                return INTERVAL_MINUTE_TO_SECOND;
            }
            throw new ParsingException(source, "Cannot determine datatype for combination [{}] [{}]", leading, trailing);
        }
    }

    //
    // String parsers
    //
    // For consistency and validation, each pattern has its own parser

    private static class ParserBuilder {

        private final List<TimeUnit> units;
        private final List<Token> tokens;
        private final String name;
        private boolean optional = false;

        ParserBuilder(DataType dataType) {
            units = new ArrayList<>(10);
            tokens = new ArrayList<>(6);
            name = dataType.name().replace('_', ' ');
        }

        ParserBuilder unit(TimeUnit unit) {
            unit(unit, 0);
            return this;
        }

        ParserBuilder unit(TimeUnit unit, int maxValue) {
            units.add(unit);
            tokens.add(new Token((char) 0, maxValue, optional));
            return this;
        }

        ParserBuilder separator(char ch) {
            tokens.add(new Token(ch, 0, optional));
            return this;
        }

        ParserBuilder optional() {
            optional = true;
            return this;
        }

        Parser build() {
            return new Parser(units, tokens, name);
        }
    }

    private static class Token {
        private final char ch;
        private final int maxValue;
        private final boolean optional;

        Token(char ch, int maxValue, boolean optional) {
            this.ch = ch;
            this.maxValue = maxValue;
            this.optional = optional;
        }

        @Override
        public String toString() {
            return ch > 0 ? String.valueOf(ch) : "[numeric]";
        }
    }

    private static class Parser {
        private static final char PLUS = '+', MINUS = '-';

        private final List<TimeUnit> units;
        private final List<Token> tokens;
        private final String name;

        Parser(List<TimeUnit> units, List<Token> tokens, String name) {
            this.units = units;
            this.tokens = tokens;
            this.name = name;
        }

        TemporalAmount parse(Source source, String string) {
            int unitIndex = 0;
            int startToken = 0;
            int endToken = 0;
            
            long[] values = new long[units.size()];

            boolean negate = false;

            // first check if there's a sign
            char maybeSign = string.charAt(0);
            if (PLUS == maybeSign) {
                startToken = 1;
            } else if (MINUS == maybeSign) {
                startToken = 1;
                negate = true;
            }

            // take each token and use it to consume a part of the string
            // validate each token and that the whole string is consumed
            for (Token token : tokens) {
                endToken = startToken;

                if (startToken >= string.length()) {
                    // consumed the string, bail out
                    if (token.optional) {
                        break;
                    }
                    throw new ParsingException(source, invalidIntervalMessage(string) + ": incorrect format, expecting {}",
                            Strings.collectionToDelimitedString(tokens, ""));
                }
                
                // char token
                if (token.ch != 0) {
                    char found = string.charAt(startToken);
                    if (found != token.ch) {
                        throw new ParsingException(source, invalidIntervalMessage(string) + ": expected [{}] (at [{}]) but found [{}]",
                                token.ch, startToken, found);
                    }
                    startToken++;
                }
                // number char
                else {
                    // go through the group the digits
                    for (; endToken < string.length() && Character.isDigit(string.charAt(endToken)); endToken++) {
                    }
                    
                    if (endToken == startToken) {
                        throw new ParsingException(source,
                                invalidIntervalMessage(string) + ": expected digit (at [{}]) but found [{}]",
                                endToken, string.charAt(endToken));
                    }

                    String number = string.substring(startToken, endToken);
                    try {
                        long v = StringUtils.parseLong(number);
                        if (token.maxValue > 0 && v > token.maxValue) {
                            throw new ParsingException(source,
                                    invalidIntervalMessage(string)
                                            + ": [{}] unit has illegal value [{}], expected a positive number up to [{}]",
                                    units.get(unitIndex).name(), v, token.maxValue);
                        }
                        if (v < 0) {
                            throw new ParsingException(source,
                                    invalidIntervalMessage(string)
                                            + ": negative value [{}] not allowed (negate the entire interval instead)",
                                    v);
                        }
                        values[unitIndex++] = v;
                    } catch (SqlIllegalArgumentException siae) {
                        throw new ParsingException(source, invalidIntervalMessage(string), siae.getMessage());
                    }
                    startToken = endToken;
                }
            }

            if (endToken <= string.length() - 1) {
                throw new ParsingException(source, invalidIntervalMessage(string) + ": unexpected trailing characters found [{}]",
                        string.substring(endToken));
            }

            TemporalAmount interval = units.get(0) == TimeUnit.YEAR || units.get(0) == TimeUnit.MONTH ? Period.ZERO : Duration.ZERO;

            for (int i = 0; i < values.length; i++) {
                TemporalAmount ta = of(source, values[i], units.get(i));
                interval = ta instanceof Period ? ((Period) ta).plus(interval) : ((Duration) ta).plus((Duration) interval);
            }

            if (negate) {
                interval = negate(interval);
            }

            return interval;
        }

        private String invalidIntervalMessage(String interval) {
            return "Invalid [" + name + "] value [" + interval + "]";
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static TemporalAmount negate(TemporalAmount interval) {
        // negated is not present on TemporalAmount though present in both Period and Duration so handle each class individually
        return interval instanceof Period ? ((Period) interval).negated() : ((Duration) interval).negated();
    }

    static final Map<DataType, Parser> PARSERS = new LinkedHashMap<>();

    //
    // Used the syntax described at the links below
    //
    // https://docs.microsoft.com/en-us/sql/odbc/reference/appendixes/interval-literal-syntax?view=sql-server-2017
    // https://docs.microsoft.com/en-us/sql/odbc/reference/appendixes/interval-literals?view=sql-server-2017
    // https://docs.oracle.com/cd/B19306_01/server.102/b14200/sql_elements003.htm#i38598

    static {
        int MAX_MONTH = 11;
        int MAX_HOUR = 23;
        int MAX_MINUTE = 59;
        int MAX_SECOND = 59;
        int MAX_MILLI = 999;
        
        char DOT = '.';
        char SPACE = ' ';
        char MINUS = '-';
        char COLON = ':';
        
        PARSERS.put(DataType.INTERVAL_YEAR, new ParserBuilder(DataType.INTERVAL_YEAR).unit(TimeUnit.YEAR).build());
        PARSERS.put(DataType.INTERVAL_MONTH, new ParserBuilder(DataType.INTERVAL_MONTH).unit(TimeUnit.MONTH).build());
        PARSERS.put(DataType.INTERVAL_DAY, new ParserBuilder(DataType.INTERVAL_DAY).unit(TimeUnit.DAY).build());
        PARSERS.put(DataType.INTERVAL_HOUR, new ParserBuilder(DataType.INTERVAL_HOUR).unit(TimeUnit.HOUR).build());
        PARSERS.put(DataType.INTERVAL_MINUTE, new ParserBuilder(DataType.INTERVAL_MINUTE).unit(TimeUnit.MINUTE).build());
        PARSERS.put(DataType.INTERVAL_SECOND, new ParserBuilder(DataType.INTERVAL_SECOND)
                 .unit(TimeUnit.SECOND)
                 .optional()
                 .separator(DOT).unit(TimeUnit.MILLISECOND, MAX_MILLI)
                 .build());

        // patterns
        PARSERS.put(DataType.INTERVAL_YEAR_TO_MONTH, new ParserBuilder(DataType.INTERVAL_YEAR_TO_MONTH)
                .unit(TimeUnit.YEAR)
                .separator(MINUS)
                .unit(TimeUnit.MONTH, MAX_MONTH)
                .build());

        PARSERS.put(DataType.INTERVAL_DAY_TO_HOUR, new ParserBuilder(DataType.INTERVAL_DAY_TO_HOUR)
                .unit(TimeUnit.DAY)
                .separator(SPACE)
                .unit(TimeUnit.HOUR, MAX_HOUR)
                .build());

        PARSERS.put(DataType.INTERVAL_DAY_TO_MINUTE, new ParserBuilder(DataType.INTERVAL_DAY_TO_MINUTE)
                .unit(TimeUnit.DAY)
                .separator(SPACE)
                .unit(TimeUnit.HOUR, MAX_HOUR)
                .separator(COLON)
                .unit(TimeUnit.MINUTE, MAX_MINUTE)
                .build());

        PARSERS.put(DataType.INTERVAL_DAY_TO_SECOND, new ParserBuilder(DataType.INTERVAL_DAY_TO_SECOND)
                .unit(TimeUnit.DAY)
                .separator(SPACE)
                .unit(TimeUnit.HOUR, MAX_HOUR)
                .separator(COLON)
                .unit(TimeUnit.MINUTE, MAX_MINUTE)
                .separator(COLON)
                .unit(TimeUnit.SECOND, MAX_SECOND)
                .optional()
                .separator(DOT).unit(TimeUnit.MILLISECOND, MAX_MILLI)
                .build());

        PARSERS.put(DataType.INTERVAL_HOUR_TO_MINUTE, new ParserBuilder(DataType.INTERVAL_HOUR_TO_MINUTE)
                .unit(TimeUnit.HOUR)
                .separator(COLON)
                .unit(TimeUnit.MINUTE, MAX_MINUTE)
                .build());

        PARSERS.put(DataType.INTERVAL_HOUR_TO_SECOND, new ParserBuilder(DataType.INTERVAL_HOUR_TO_SECOND)
                .unit(TimeUnit.HOUR)
                .separator(COLON)
                .unit(TimeUnit.MINUTE, MAX_MINUTE)
                .separator(COLON)
                .unit(TimeUnit.SECOND, MAX_SECOND)
                .optional()
                .separator(DOT).unit(TimeUnit.MILLISECOND, MAX_MILLI)
                .build());
        
        PARSERS.put(DataType.INTERVAL_MINUTE_TO_SECOND, new ParserBuilder(DataType.INTERVAL_MINUTE_TO_SECOND)
                .unit(TimeUnit.MINUTE)
                .separator(COLON)
                .unit(TimeUnit.SECOND, MAX_SECOND)
                .optional()
                .separator(DOT).unit(TimeUnit.MILLISECOND, MAX_MILLI)
                .build());
    }

    public static TemporalAmount parseInterval(Source source, String value, DataType intervalType) {
        return PARSERS.get(intervalType).parse(source, value);
    }
}
