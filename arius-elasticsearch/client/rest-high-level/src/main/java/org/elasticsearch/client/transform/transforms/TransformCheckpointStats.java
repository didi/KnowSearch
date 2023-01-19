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

package org.elasticsearch.client.transform.transforms;

import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.Objects;

import static org.elasticsearch.common.xcontent.ConstructingObjectParser.optionalConstructorArg;

public class TransformCheckpointStats {

    public static final ParseField CHECKPOINT = new ParseField("checkpoint");
    public static final ParseField POSITION = new ParseField("position");
    public static final ParseField CHECKPOINT_PROGRESS = new ParseField("checkpoint_progress");
    public static final ParseField TIMESTAMP_MILLIS = new ParseField("timestamp_millis");
    public static final ParseField TIME_UPPER_BOUND_MILLIS = new ParseField("time_upper_bound_millis");

    public static final TransformCheckpointStats EMPTY = new TransformCheckpointStats(0L, null, null, 0L, 0L);

    private final long checkpoint;
    private final TransformIndexerPosition position;
    private final TransformProgress checkpointProgress;
    private final long timestampMillis;
    private final long timeUpperBoundMillis;

    public static final ConstructingObjectParser<TransformCheckpointStats, Void> LENIENT_PARSER = new ConstructingObjectParser<>(
            "transform_checkpoint_stats", true, args -> {
        long checkpoint = args[0] == null ? 0L : (Long) args[0];
        TransformIndexerPosition position = (TransformIndexerPosition) args[1];
        TransformProgress checkpointProgress = (TransformProgress) args[2];
        long timestamp = args[3] == null ? 0L : (Long) args[3];
        long timeUpperBound = args[4] == null ? 0L : (Long) args[4];

        return new TransformCheckpointStats(checkpoint, position, checkpointProgress, timestamp, timeUpperBound);
    });

    static {
        LENIENT_PARSER.declareLong(optionalConstructorArg(), CHECKPOINT);
        LENIENT_PARSER.declareObject(optionalConstructorArg(), TransformIndexerPosition.PARSER, POSITION);
        LENIENT_PARSER.declareObject(optionalConstructorArg(), TransformProgress.PARSER, CHECKPOINT_PROGRESS);
        LENIENT_PARSER.declareLong(optionalConstructorArg(), TIMESTAMP_MILLIS);
        LENIENT_PARSER.declareLong(optionalConstructorArg(), TIME_UPPER_BOUND_MILLIS);
    }

    public static TransformCheckpointStats fromXContent(XContentParser parser) throws IOException {
        return LENIENT_PARSER.parse(parser, null);
    }

    public TransformCheckpointStats(final long checkpoint, final TransformIndexerPosition position,
                                    final TransformProgress checkpointProgress, final long timestampMillis,
                                    final long timeUpperBoundMillis) {
        this.checkpoint = checkpoint;
        this.position = position;
        this.checkpointProgress = checkpointProgress;
        this.timestampMillis = timestampMillis;
        this.timeUpperBoundMillis = timeUpperBoundMillis;
    }

    public long getCheckpoint() {
        return checkpoint;
    }

    public TransformIndexerPosition getPosition() {
        return position;
    }

    public TransformProgress getCheckpointProgress() {
        return checkpointProgress;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public long getTimeUpperBoundMillis() {
        return timeUpperBoundMillis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkpoint, position, checkpointProgress, timestampMillis, timeUpperBoundMillis);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        TransformCheckpointStats that = (TransformCheckpointStats) other;

        return this.checkpoint == that.checkpoint
            && Objects.equals(this.position, that.position)
            && Objects.equals(this.checkpointProgress, that.checkpointProgress)
            && this.timestampMillis == that.timestampMillis
            && this.timeUpperBoundMillis == that.timeUpperBoundMillis;
    }
}
