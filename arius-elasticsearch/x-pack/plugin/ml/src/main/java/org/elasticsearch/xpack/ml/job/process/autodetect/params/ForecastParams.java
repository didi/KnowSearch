/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.job.process.autodetect.params;

import org.elasticsearch.common.UUIDs;
import org.elasticsearch.common.unit.TimeValue;

import java.util.Objects;

public class ForecastParams {

    private final String forecastId;
    private final long createTime;
    private final long duration;
    private final long expiresIn;
    private final String tmpStorage;

    private ForecastParams(String forecastId, long createTime, long duration, long expiresIn, String tmpStorage) {
        this.forecastId = forecastId;
        this.createTime = createTime;
        this.duration = duration;
        this.expiresIn = expiresIn;
        this.tmpStorage = tmpStorage;
    }

    public String getForecastId() {
        return forecastId;
    }

    /**
     * The forecast create time in seconds from the epoch
     * @return The create time in seconds from the epoch
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * The forecast duration in seconds
     * @return The duration in seconds
     */
    public long getDuration() {
        return duration;
    }

    /**
     * The forecast expiration in seconds (duration added to start time)
     * @return The expiration in seconds
     */
    public long getExpiresIn() {
        return expiresIn;
    }

    /**
     * Temporary storage forecast is allowed to use for persisting models.
     *
     * @return path to tmp storage
     */
    public String getTmpStorage() {
        return tmpStorage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(forecastId, createTime, duration, expiresIn, tmpStorage);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ForecastParams other = (ForecastParams) obj;
        return Objects.equals(forecastId, other.forecastId)
                && Objects.equals(createTime, other.createTime)
                && Objects.equals(duration, other.duration)
                && Objects.equals(expiresIn, other.expiresIn)
                && Objects.equals(tmpStorage, other.tmpStorage);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final String forecastId;
        private final long createTimeEpochSecs;
        private long durationSecs;
        private long expiresInSecs;
        private String tmpStorage;

        private Builder() {
            forecastId = UUIDs.base64UUID();
            createTimeEpochSecs = System.currentTimeMillis() / 1000;
            durationSecs = 0;

            // because 0 means never expire, the default is -1
            expiresInSecs = -1;
        }

        public Builder duration(TimeValue duration) {
            durationSecs = duration.seconds();
            return this;
        }

        public Builder expiresIn(TimeValue expiresIn) {
            expiresInSecs = expiresIn.seconds();
            return this;
        }

        public Builder tmpStorage(String tmpStorage) {
            this.tmpStorage = tmpStorage;
            return this;
        }

        public ForecastParams build() {
            return new ForecastParams(forecastId, createTimeEpochSecs, durationSecs, expiresInSecs, tmpStorage);
        }
    }
}

