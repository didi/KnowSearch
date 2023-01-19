/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.core.transform;

import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.xpack.core.XPackFeatureSet.Usage;
import org.elasticsearch.xpack.core.transform.transforms.TransformIndexerStats;
import org.elasticsearch.xpack.core.XPackField;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class TransformFeatureSetUsage extends Usage {

    private final Map<String, Long> transformCountByState;
    private final TransformIndexerStats accumulatedStats;

    public TransformFeatureSetUsage(StreamInput in) throws IOException {
        super(in);
        this.transformCountByState = in.readMap(StreamInput::readString, StreamInput::readLong);
        this.accumulatedStats = new TransformIndexerStats(in);
    }

    public TransformFeatureSetUsage(boolean available, boolean enabled, Map<String, Long> transformCountByState,
            TransformIndexerStats accumulatedStats) {
        super(XPackField.TRANSFORM, available, enabled);
        this.transformCountByState = Objects.requireNonNull(transformCountByState);
        this.accumulatedStats = Objects.requireNonNull(accumulatedStats);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeMap(transformCountByState, StreamOutput::writeString, StreamOutput::writeLong);
        accumulatedStats.writeTo(out);
    }

    @Override
    protected void innerXContent(XContentBuilder builder, Params params) throws IOException {
        super.innerXContent(builder, params);
        if (transformCountByState.isEmpty() == false) {
            builder.startObject(TransformField.TRANSFORMS.getPreferredName());
            long all = 0L;
            for (Entry<String, Long> entry : transformCountByState.entrySet()) {
                builder.field(entry.getKey(), entry.getValue());
                all+=entry.getValue();
            }
            builder.field(MetaData.ALL, all);
            builder.endObject();

            // if there are no transforms, do not show any stats
            builder.field(TransformField.STATS_FIELD.getPreferredName(), accumulatedStats);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, available, transformCountByState, accumulatedStats);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TransformFeatureSetUsage other = (TransformFeatureSetUsage) obj;
        return Objects.equals(name, other.name) && available == other.available && enabled == other.enabled
                && Objects.equals(transformCountByState, other.transformCountByState)
                && Objects.equals(accumulatedStats, other.accumulatedStats);
    }
}
