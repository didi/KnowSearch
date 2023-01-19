/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.rollup.job;


import org.elasticsearch.Version;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.persistent.PersistentTaskState;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.xpack.core.indexing.IndexerState;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static org.elasticsearch.common.xcontent.ConstructingObjectParser.constructorArg;
import static org.elasticsearch.common.xcontent.ConstructingObjectParser.optionalConstructorArg;

/**
 * This class is essentially just a wrapper around the IndexerState and the
 * indexer's current position.  When the allocated task updates its status,
 * it is providing a new version of this.
 */
public class RollupJobStatus implements Task.Status, PersistentTaskState {
    public static final String NAME = "xpack/rollup/job";

    private final IndexerState state;

    @Nullable
    private final TreeMap<String, Object> currentPosition;

    // Flag holds the state of the ID scheme, e.g. if it has been upgraded to the
    // concatenation scheme.  See #32372 for more details
    private boolean upgradedDocumentID;

    private static final ParseField STATE = new ParseField("job_state");
    private static final ParseField CURRENT_POSITION = new ParseField("current_position");
    private static final ParseField UPGRADED_DOC_ID = new ParseField("upgraded_doc_id");

    public static final ConstructingObjectParser<RollupJobStatus, Void> PARSER =
            new ConstructingObjectParser<>(NAME,
                    args -> new RollupJobStatus((IndexerState) args[0],
                        (HashMap<String, Object>) args[1],
                        (Boolean)args[2]));

    static {
        PARSER.declareField(constructorArg(), p -> {
            if (p.currentToken() == XContentParser.Token.VALUE_STRING) {
                return IndexerState.fromString(p.text());
            }
            throw new IllegalArgumentException("Unsupported token [" + p.currentToken() + "]");
        }, STATE, ObjectParser.ValueType.STRING);
        PARSER.declareField(optionalConstructorArg(), p -> {
            if (p.currentToken() == XContentParser.Token.START_OBJECT) {
                return p.map();
            }
            if (p.currentToken() == XContentParser.Token.VALUE_NULL) {
                return null;
            }
            throw new IllegalArgumentException("Unsupported token [" + p.currentToken() + "]");
        }, CURRENT_POSITION, ObjectParser.ValueType.VALUE_OBJECT_ARRAY);

        // Optional to accommodate old versions of state
        PARSER.declareBoolean(ConstructingObjectParser.optionalConstructorArg(), UPGRADED_DOC_ID);
    }

    public RollupJobStatus(IndexerState state, @Nullable Map<String, Object> position,
                           @Nullable Boolean upgradedDocumentID) {
        this.state = state;
        this.currentPosition = position == null ? null : new TreeMap<>(position);
        this.upgradedDocumentID = upgradedDocumentID != null ? upgradedDocumentID : false;  //default to false if missing
    }

    public RollupJobStatus(StreamInput in) throws IOException {
        state = IndexerState.fromStream(in);
        currentPosition = in.readBoolean() ? new TreeMap<>(in.readMap()) : null;
        if (in.getVersion().onOrAfter(Version.V_6_4_0)) {
            upgradedDocumentID = in.readBoolean();
        } else {
            // If we're getting this job from a pre-6.4.0 node,
            // it is using the old ID scheme
            upgradedDocumentID = false;
        }
    }

    public IndexerState getIndexerState() {
        return state;
    }

    public Map<String, Object> getPosition() {
        return currentPosition;
    }

    public boolean isUpgradedDocumentID() {
        return upgradedDocumentID;
    }

    public static RollupJobStatus fromXContent(XContentParser parser) {
        try {
            return PARSER.parse(parser, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(STATE.getPreferredName(), state.value());
        if (currentPosition != null) {
            builder.field(CURRENT_POSITION.getPreferredName(), currentPosition);
        }
        builder.field(UPGRADED_DOC_ID.getPreferredName(), upgradedDocumentID);
        builder.endObject();
        return builder;
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        state.writeTo(out);
        out.writeBoolean(currentPosition != null);
        if (currentPosition != null) {
            out.writeMap(currentPosition);
        }
        if (out.getVersion().onOrAfter(Version.V_6_4_0)) {
            out.writeBoolean(upgradedDocumentID);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        RollupJobStatus that = (RollupJobStatus) other;

        return Objects.equals(this.state, that.state)
            && Objects.equals(this.currentPosition, that.currentPosition)
            && Objects.equals(this.upgradedDocumentID, that.upgradedDocumentID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, currentPosition, upgradedDocumentID);
    }
}
