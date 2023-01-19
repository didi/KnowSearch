/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.sql;

import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.xpack.sql.proto.Mode;
import org.elasticsearch.xpack.sql.proto.Protocol;
import org.elasticsearch.xpack.sql.session.Configuration;
import org.elasticsearch.xpack.sql.util.DateUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.StringJoiner;

import static org.elasticsearch.test.ESTestCase.randomAlphaOfLength;
import static org.elasticsearch.test.ESTestCase.randomBoolean;
import static org.elasticsearch.test.ESTestCase.randomFrom;
import static org.elasticsearch.test.ESTestCase.randomInt;
import static org.elasticsearch.test.ESTestCase.randomIntBetween;
import static org.elasticsearch.test.ESTestCase.randomNonNegativeLong;
import static org.elasticsearch.test.ESTestCase.randomZone;


public final class TestUtils {

    private TestUtils() {}

    public static final Configuration TEST_CFG = new Configuration(DateUtils.UTC, Protocol.FETCH_SIZE,
            Protocol.REQUEST_TIMEOUT, Protocol.PAGE_TIMEOUT, null, Mode.PLAIN,
            null, null, null, false, false);

    /**
     * Returns the current UTC date-time with milliseconds precision.
     * In Java 9+ (as opposed to Java 8) the {@code Clock} implementation uses system's best clock implementation (which could mean
     * that the precision of the clock can be milliseconds, microseconds or nanoseconds), whereas in Java 8
     * {@code System.currentTimeMillis()} is always used. To account for these differences, this method defines a new {@code Clock}
     * which will offer a value for {@code ZonedDateTime.now()} set to always have milliseconds precision.
     * 
     * @return {@link ZonedDateTime} instance for the current date-time with milliseconds precision in UTC
     */
    public static ZonedDateTime now() {
        return ZonedDateTime.now(Clock.tick(Clock.system(DateUtils.UTC), Duration.ofMillis(1)));
    }

    public static Configuration randomConfiguration() {
        return new Configuration(randomZone(),
                randomIntBetween(0,  1000),
                new TimeValue(randomNonNegativeLong()),
                new TimeValue(randomNonNegativeLong()),
                null,
                randomFrom(Mode.values()),
                randomAlphaOfLength(10),
                randomAlphaOfLength(10),
                randomAlphaOfLength(10),
                false,
                randomBoolean());
    }

    public static Configuration randomConfiguration(ZoneId providedZoneId) {
        return new Configuration(providedZoneId,
                randomIntBetween(0,  1000),
                new TimeValue(randomNonNegativeLong()),
                new TimeValue(randomNonNegativeLong()),
                null,
                randomFrom(Mode.values()),
                randomAlphaOfLength(10),
                randomAlphaOfLength(10),
                randomAlphaOfLength(10),
                false,
                randomBoolean());
    }

    public static String randomWhitespaces() {
        StringJoiner sj = new StringJoiner("");
        for (int i = 0; i < randomInt(10); i++) {
            sj.add(randomFrom(" ", "\t", "\r", "\n"));
        }
        return sj.toString();
    }
}

