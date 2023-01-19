/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.client.ml.job.process;

import org.elasticsearch.client.common.TimeUtil;
import org.elasticsearch.client.ml.job.config.Job;
import org.elasticsearch.client.ml.job.results.Result;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.ObjectParser.ValueType;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Provide access to the C++ model memory usage numbers for the Java process.
 */
public class ModelSizeStats implements ToXContentObject {

    /**
     * Result type
     */
    public static final String RESULT_TYPE_VALUE = "model_size_stats";
    public static final ParseField RESULT_TYPE_FIELD = new ParseField(RESULT_TYPE_VALUE);

    /**
     * Field Names
     */
    public static final ParseField MODEL_BYTES_FIELD = new ParseField("model_bytes");
    public static final ParseField MODEL_BYTES_EXCEEDED_FIELD = new ParseField("model_bytes_exceeded");
    public static final ParseField MODEL_BYTES_MEMORY_LIMIT_FIELD = new ParseField("model_bytes_memory_limit");
    public static final ParseField TOTAL_BY_FIELD_COUNT_FIELD = new ParseField("total_by_field_count");
    public static final ParseField TOTAL_OVER_FIELD_COUNT_FIELD = new ParseField("total_over_field_count");
    public static final ParseField TOTAL_PARTITION_FIELD_COUNT_FIELD = new ParseField("total_partition_field_count");
    public static final ParseField BUCKET_ALLOCATION_FAILURES_COUNT_FIELD = new ParseField("bucket_allocation_failures_count");
    public static final ParseField MEMORY_STATUS_FIELD = new ParseField("memory_status");
    public static final ParseField LOG_TIME_FIELD = new ParseField("log_time");
    public static final ParseField TIMESTAMP_FIELD = new ParseField("timestamp");

    public static final ConstructingObjectParser<Builder, Void> PARSER =
        new ConstructingObjectParser<>(RESULT_TYPE_VALUE, true, a -> new Builder((String) a[0]));

    static {
        PARSER.declareString(ConstructingObjectParser.constructorArg(), Job.ID);
        PARSER.declareLong(Builder::setModelBytes, MODEL_BYTES_FIELD);
        PARSER.declareLong(Builder::setModelBytesExceeded, MODEL_BYTES_EXCEEDED_FIELD);
        PARSER.declareLong(Builder::setModelBytesMemoryLimit, MODEL_BYTES_MEMORY_LIMIT_FIELD);
        PARSER.declareLong(Builder::setBucketAllocationFailuresCount, BUCKET_ALLOCATION_FAILURES_COUNT_FIELD);
        PARSER.declareLong(Builder::setTotalByFieldCount, TOTAL_BY_FIELD_COUNT_FIELD);
        PARSER.declareLong(Builder::setTotalOverFieldCount, TOTAL_OVER_FIELD_COUNT_FIELD);
        PARSER.declareLong(Builder::setTotalPartitionFieldCount, TOTAL_PARTITION_FIELD_COUNT_FIELD);
        PARSER.declareField(Builder::setLogTime,
            (p) -> TimeUtil.parseTimeField(p, LOG_TIME_FIELD.getPreferredName()),
            LOG_TIME_FIELD,
            ValueType.VALUE);
        PARSER.declareField(Builder::setTimestamp,
            (p) -> TimeUtil.parseTimeField(p, TIMESTAMP_FIELD.getPreferredName()),
            TIMESTAMP_FIELD,
            ValueType.VALUE);
        PARSER.declareField(Builder::setMemoryStatus, p -> MemoryStatus.fromString(p.text()), MEMORY_STATUS_FIELD, ValueType.STRING);
    }

    /**
     * The status of the memory monitored by the ResourceMonitor. OK is default,
     * SOFT_LIMIT means that the models have done some aggressive pruning to
     * keep the memory below the limit, and HARD_LIMIT means that samples have
     * been dropped
     */
    public enum MemoryStatus {
        OK, SOFT_LIMIT, HARD_LIMIT;

        public static MemoryStatus fromString(String statusName) {
            return valueOf(statusName.trim().toUpperCase(Locale.ROOT));
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private final String jobId;
    private final long modelBytes;
    private final Long modelBytesExceeded;
    private final Long modelBytesMemoryLimit;
    private final long totalByFieldCount;
    private final long totalOverFieldCount;
    private final long totalPartitionFieldCount;
    private final long bucketAllocationFailuresCount;
    private final MemoryStatus memoryStatus;
    private final Date timestamp;
    private final Date logTime;

    private ModelSizeStats(String jobId, long modelBytes, Long modelBytesExceeded, Long modelBytesMemoryLimit, long totalByFieldCount,
                           long totalOverFieldCount, long totalPartitionFieldCount, long bucketAllocationFailuresCount,
                           MemoryStatus memoryStatus, Date timestamp, Date logTime) {
        this.jobId = jobId;
        this.modelBytes = modelBytes;
        this.modelBytesExceeded = modelBytesExceeded;
        this.modelBytesMemoryLimit = modelBytesMemoryLimit;
        this.totalByFieldCount = totalByFieldCount;
        this.totalOverFieldCount = totalOverFieldCount;
        this.totalPartitionFieldCount = totalPartitionFieldCount;
        this.bucketAllocationFailuresCount = bucketAllocationFailuresCount;
        this.memoryStatus = memoryStatus;
        this.timestamp = timestamp;
        this.logTime = logTime;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();

        builder.field(Job.ID.getPreferredName(), jobId);
        builder.field(Result.RESULT_TYPE.getPreferredName(), RESULT_TYPE_VALUE);
        builder.field(MODEL_BYTES_FIELD.getPreferredName(), modelBytes);
        if (modelBytesExceeded != null) {
            builder.field(MODEL_BYTES_EXCEEDED_FIELD.getPreferredName(), modelBytesExceeded);
        }
        if (modelBytesMemoryLimit != null) {
            builder.field(MODEL_BYTES_MEMORY_LIMIT_FIELD.getPreferredName(), modelBytesMemoryLimit);
        }
        builder.field(TOTAL_BY_FIELD_COUNT_FIELD.getPreferredName(), totalByFieldCount);
        builder.field(TOTAL_OVER_FIELD_COUNT_FIELD.getPreferredName(), totalOverFieldCount);
        builder.field(TOTAL_PARTITION_FIELD_COUNT_FIELD.getPreferredName(), totalPartitionFieldCount);
        builder.field(BUCKET_ALLOCATION_FAILURES_COUNT_FIELD.getPreferredName(), bucketAllocationFailuresCount);
        builder.field(MEMORY_STATUS_FIELD.getPreferredName(), memoryStatus);
        builder.timeField(LOG_TIME_FIELD.getPreferredName(), LOG_TIME_FIELD.getPreferredName() + "_string", logTime.getTime());
        if (timestamp != null) {
            builder.timeField(TIMESTAMP_FIELD.getPreferredName(), TIMESTAMP_FIELD.getPreferredName() + "_string", timestamp.getTime());
        }

        builder.endObject();
        return builder;
    }

    public String getJobId() {
        return jobId;
    }

    public long getModelBytes() {
        return modelBytes;
    }

    public Long getModelBytesExceeded() {
        return modelBytesExceeded;
    }

    public Long getModelBytesMemoryLimit() {
        return modelBytesMemoryLimit;
    }

    public long getTotalByFieldCount() {
        return totalByFieldCount;
    }

    public long getTotalPartitionFieldCount() {
        return totalPartitionFieldCount;
    }

    public long getTotalOverFieldCount() {
        return totalOverFieldCount;
    }

    public long getBucketAllocationFailuresCount() {
        return bucketAllocationFailuresCount;
    }

    public MemoryStatus getMemoryStatus() {
        return memoryStatus;
    }

    /**
     * The timestamp of the last processed record when this instance was created.
     *
     * @return The record time
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * The wall clock time at the point when this instance was created.
     *
     * @return The wall clock time
     */
    public Date getLogTime() {
        return logTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, modelBytes, modelBytesExceeded, modelBytesMemoryLimit, totalByFieldCount, totalOverFieldCount,
            totalPartitionFieldCount, this.bucketAllocationFailuresCount, memoryStatus, timestamp, logTime);
    }

    /**
     * Compare all the fields.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ModelSizeStats that = (ModelSizeStats) other;

        return this.modelBytes == that.modelBytes && Objects.equals(this.modelBytesExceeded, that.modelBytesExceeded)
            && Objects.equals(this.modelBytesMemoryLimit, that.modelBytesMemoryLimit) && this.totalByFieldCount == that.totalByFieldCount
            && this.totalOverFieldCount == that.totalOverFieldCount && this.totalPartitionFieldCount == that.totalPartitionFieldCount
            && this.bucketAllocationFailuresCount == that.bucketAllocationFailuresCount
            && Objects.equals(this.memoryStatus, that.memoryStatus) && Objects.equals(this.timestamp, that.timestamp)
            && Objects.equals(this.logTime, that.logTime)
            && Objects.equals(this.jobId, that.jobId);
    }

    public static class Builder {

        private final String jobId;
        private long modelBytes;
        private Long modelBytesExceeded;
        private Long modelBytesMemoryLimit;
        private long totalByFieldCount;
        private long totalOverFieldCount;
        private long totalPartitionFieldCount;
        private long bucketAllocationFailuresCount;
        private MemoryStatus memoryStatus;
        private Date timestamp;
        private Date logTime;

        public Builder(String jobId) {
            this.jobId = jobId;
            memoryStatus = MemoryStatus.OK;
            logTime = new Date();
        }

        public Builder(ModelSizeStats modelSizeStats) {
            this.jobId = modelSizeStats.jobId;
            this.modelBytes = modelSizeStats.modelBytes;
            this.modelBytesExceeded = modelSizeStats.modelBytesExceeded;
            this.modelBytesMemoryLimit = modelSizeStats.modelBytesMemoryLimit;
            this.totalByFieldCount = modelSizeStats.totalByFieldCount;
            this.totalOverFieldCount = modelSizeStats.totalOverFieldCount;
            this.totalPartitionFieldCount = modelSizeStats.totalPartitionFieldCount;
            this.bucketAllocationFailuresCount = modelSizeStats.bucketAllocationFailuresCount;
            this.memoryStatus = modelSizeStats.memoryStatus;
            this.timestamp = modelSizeStats.timestamp;
            this.logTime = modelSizeStats.logTime;
        }

        public Builder setModelBytes(long modelBytes) {
            this.modelBytes = modelBytes;
            return this;
        }

        public Builder setModelBytesExceeded(long modelBytesExceeded) {
            this.modelBytesExceeded = modelBytesExceeded;
            return this;
        }

        public Builder setModelBytesMemoryLimit(long modelBytesMemoryLimit) {
            this.modelBytesMemoryLimit = modelBytesMemoryLimit;
            return this;
        }

        public Builder setTotalByFieldCount(long totalByFieldCount) {
            this.totalByFieldCount = totalByFieldCount;
            return this;
        }

        public Builder setTotalPartitionFieldCount(long totalPartitionFieldCount) {
            this.totalPartitionFieldCount = totalPartitionFieldCount;
            return this;
        }

        public Builder setTotalOverFieldCount(long totalOverFieldCount) {
            this.totalOverFieldCount = totalOverFieldCount;
            return this;
        }

        public Builder setBucketAllocationFailuresCount(long bucketAllocationFailuresCount) {
            this.bucketAllocationFailuresCount = bucketAllocationFailuresCount;
            return this;
        }

        public Builder setMemoryStatus(MemoryStatus memoryStatus) {
            Objects.requireNonNull(memoryStatus, "[" + MEMORY_STATUS_FIELD.getPreferredName() + "] must not be null");
            this.memoryStatus = memoryStatus;
            return this;
        }

        public Builder setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setLogTime(Date logTime) {
            this.logTime = logTime;
            return this;
        }

        public ModelSizeStats build() {
            return new ModelSizeStats(jobId, modelBytes, modelBytesExceeded, modelBytesMemoryLimit, totalByFieldCount, totalOverFieldCount,
                totalPartitionFieldCount, bucketAllocationFailuresCount, memoryStatus, timestamp, logTime);
        }
    }
}
