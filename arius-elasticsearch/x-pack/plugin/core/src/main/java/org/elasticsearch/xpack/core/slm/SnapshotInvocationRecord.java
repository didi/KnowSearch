/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.core.slm;

import org.elasticsearch.cluster.AbstractDiffable;
import org.elasticsearch.cluster.Diffable;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.Objects;

/**
 * Holds information about Snapshots kicked off by Snapshot Lifecycle Management in the cluster state, so that this information can be
 * presented to the user. This class is used for both successes and failures as the structure of the data is very similar.
 */
public class SnapshotInvocationRecord extends AbstractDiffable<SnapshotInvocationRecord>
    implements Writeable, ToXContentObject, Diffable<SnapshotInvocationRecord> {

    static final ParseField SNAPSHOT_NAME = new ParseField("snapshot_name");
    static final ParseField TIMESTAMP = new ParseField("time");
    static final ParseField DETAILS = new ParseField("details");

    private String snapshotName;
    private long timestamp;
    private String details;

    public static final ConstructingObjectParser<SnapshotInvocationRecord, String> PARSER =
        new ConstructingObjectParser<>("snapshot_policy_invocation_record", true,
            a -> new SnapshotInvocationRecord((String) a[0], (long) a[1], (String) a[2]));

    static {
        PARSER.declareString(ConstructingObjectParser.constructorArg(), SNAPSHOT_NAME);
        PARSER.declareLong(ConstructingObjectParser.constructorArg(), TIMESTAMP);
        PARSER.declareString(ConstructingObjectParser.optionalConstructorArg(), DETAILS);
    }

    public static SnapshotInvocationRecord parse(XContentParser parser, String name) {
        return PARSER.apply(parser, name);
    }

    public SnapshotInvocationRecord(String snapshotName, long timestamp, String details) {
        this.snapshotName = Objects.requireNonNull(snapshotName, "snapshot name must be provided");
        this.timestamp = timestamp;
        this.details = details;
    }

    public SnapshotInvocationRecord(StreamInput in) throws IOException {
        this.snapshotName = in.readString();
        this.timestamp = in.readVLong();
        this.details = in.readOptionalString();
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(snapshotName);
        out.writeVLong(timestamp);
        out.writeOptionalString(details);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        {
            builder.field(SNAPSHOT_NAME.getPreferredName(), snapshotName);
            builder.timeField(TIMESTAMP.getPreferredName(), "time_string", timestamp);
            if (Objects.nonNull(details)) {
                builder.field(DETAILS.getPreferredName(), details);
            }
        }
        builder.endObject();
        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnapshotInvocationRecord that = (SnapshotInvocationRecord) o;
        return getTimestamp() == that.getTimestamp() &&
            Objects.equals(getSnapshotName(), that.getSnapshotName()) &&
            Objects.equals(getDetails(), that.getDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSnapshotName(), getTimestamp(), getDetails());
    }
}
