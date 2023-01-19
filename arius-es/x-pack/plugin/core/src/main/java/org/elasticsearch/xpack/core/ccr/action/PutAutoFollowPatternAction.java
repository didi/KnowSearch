/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ccr.action;

import org.elasticsearch.Version;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.xpack.core.ccr.AutoFollowMetadata.AutoFollowPattern;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.elasticsearch.action.ValidateActions.addValidationError;
import static org.elasticsearch.xpack.core.ccr.AutoFollowMetadata.AutoFollowPattern.REMOTE_CLUSTER_FIELD;

public class PutAutoFollowPatternAction extends ActionType<AcknowledgedResponse> {

    public static final String NAME = "cluster:admin/xpack/ccr/auto_follow_pattern/put";
    public static final PutAutoFollowPatternAction INSTANCE = new PutAutoFollowPatternAction();
    private static final int MAX_NAME_BYTES = 255;

    private PutAutoFollowPatternAction() {
        super(NAME, AcknowledgedResponse::new);
    }

    public static class Request extends AcknowledgedRequest<Request> implements ToXContentObject {

        // Note that Request should be the Value class here for this parser with a 'parameters' field that maps to
        // PutAutoFollowPatternParameters class. But since two minor version are already released with duplicate
        // follow parameters in several APIs, PutAutoFollowPatternParameters is now the Value class here.
        private static final ObjectParser<PutAutoFollowPatternParameters, Void> PARSER =
            new ObjectParser<>("put_auto_follow_pattern_request", PutAutoFollowPatternParameters::new);

        static {
            PARSER.declareString((params, value) -> params.remoteCluster = value, REMOTE_CLUSTER_FIELD);
            PARSER.declareStringArray((params, value) -> params.leaderIndexPatterns = value, AutoFollowPattern.LEADER_PATTERNS_FIELD);
            PARSER.declareString((params, value) -> params.followIndexNamePattern = value, AutoFollowPattern.FOLLOW_PATTERN_FIELD);
            FollowParameters.initParser(PARSER);
        }

        public static Request fromXContent(XContentParser parser, String name) throws IOException {
            PutAutoFollowPatternParameters parameters = PARSER.parse(parser, null);
            Request request = new Request();
            request.setName(name);
            request.setRemoteCluster(parameters.remoteCluster);
            request.setLeaderIndexPatterns(parameters.leaderIndexPatterns);
            request.setFollowIndexNamePattern(parameters.followIndexNamePattern);
            request.setParameters(parameters);
            return request;
        }

        private String name;
        private String remoteCluster;
        private List<String> leaderIndexPatterns;
        private String followIndexNamePattern;
        private FollowParameters parameters = new FollowParameters();

        public Request() {
        }

        @Override
        public ActionRequestValidationException validate() {
            ActionRequestValidationException validationException = parameters.validate();
            if (name == null) {
                validationException = addValidationError("[name] is missing", validationException);
            }
            if (name != null) {
                if (name.contains(",")) {
                    validationException = addValidationError("[name] name must not contain a ','", validationException);
                }
                if (name.startsWith("_")) {
                    validationException = addValidationError("[name] name must not start with '_'", validationException);
                }
                int byteCount = name.getBytes(StandardCharsets.UTF_8).length;
                if (byteCount > MAX_NAME_BYTES) {
                    validationException = addValidationError("[name] name is too long (" + byteCount + " > " + MAX_NAME_BYTES + ")",
                        validationException);
                }
            }
            if (remoteCluster == null) {
                validationException = addValidationError("[" + REMOTE_CLUSTER_FIELD.getPreferredName() +
                    "] is missing", validationException);
            }
            if (leaderIndexPatterns == null || leaderIndexPatterns.isEmpty()) {
                validationException = addValidationError("[" + AutoFollowPattern.LEADER_PATTERNS_FIELD.getPreferredName() +
                    "] is missing", validationException);
            }
            return validationException;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRemoteCluster() {
            return remoteCluster;
        }

        public void setRemoteCluster(String remoteCluster) {
            this.remoteCluster = remoteCluster;
        }

        public List<String> getLeaderIndexPatterns() {
            return leaderIndexPatterns;
        }

        public void setLeaderIndexPatterns(List<String> leaderIndexPatterns) {
            this.leaderIndexPatterns = leaderIndexPatterns;
        }

        public String getFollowIndexNamePattern() {
            return followIndexNamePattern;
        }

        public void setFollowIndexNamePattern(String followIndexNamePattern) {
            this.followIndexNamePattern = followIndexNamePattern;
        }

        public FollowParameters getParameters() {
            return parameters;
        }

        public void setParameters(FollowParameters parameters) {
            this.parameters = parameters;
        }

        public Request(StreamInput in) throws IOException {
            super(in);
            name = in.readString();
            remoteCluster = in.readString();
            leaderIndexPatterns = in.readStringList();
            followIndexNamePattern = in.readOptionalString();
            if (in.getVersion().onOrAfter(Version.V_6_7_0)) {
                parameters = new FollowParameters(in);
            } else {
                parameters = new FollowParameters();
                parameters.maxReadRequestOperationCount = in.readOptionalVInt();
                parameters.maxReadRequestSize = in.readOptionalWriteable(ByteSizeValue::new);
                parameters.maxOutstandingReadRequests = in.readOptionalVInt();
                parameters.maxWriteRequestOperationCount = in.readOptionalVInt();
                parameters.maxWriteRequestSize = in.readOptionalWriteable(ByteSizeValue::new);
                parameters.maxOutstandingWriteRequests = in.readOptionalVInt();
                parameters.maxWriteBufferCount = in.readOptionalVInt();
                parameters.maxWriteBufferSize = in.readOptionalWriteable(ByteSizeValue::new);
                parameters.maxRetryDelay = in.readOptionalTimeValue();
                parameters.readPollTimeout = in.readOptionalTimeValue();
            }
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeString(name);
            out.writeString(remoteCluster);
            out.writeStringCollection(leaderIndexPatterns);
            out.writeOptionalString(followIndexNamePattern);
            if (out.getVersion().onOrAfter(Version.V_6_7_0)) {
                parameters.writeTo(out);
            } else {
                out.writeOptionalVInt(parameters.maxReadRequestOperationCount);
                out.writeOptionalWriteable(parameters.maxReadRequestSize);
                out.writeOptionalVInt(parameters.maxOutstandingReadRequests);
                out.writeOptionalVInt(parameters.maxWriteRequestOperationCount);
                out.writeOptionalWriteable(parameters.maxWriteRequestSize);
                out.writeOptionalVInt(parameters.maxOutstandingWriteRequests);
                out.writeOptionalVInt(parameters.maxWriteBufferCount);
                out.writeOptionalWriteable(parameters.maxWriteBufferSize);
                out.writeOptionalTimeValue(parameters.maxRetryDelay);
                out.writeOptionalTimeValue(parameters.readPollTimeout);
            }
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            {
                builder.field(REMOTE_CLUSTER_FIELD.getPreferredName(), remoteCluster);
                builder.field(AutoFollowPattern.LEADER_PATTERNS_FIELD.getPreferredName(), leaderIndexPatterns);
                if (followIndexNamePattern != null) {
                    builder.field(AutoFollowPattern.FOLLOW_PATTERN_FIELD.getPreferredName(), followIndexNamePattern);
                }
                parameters.toXContentFragment(builder);
            }
            builder.endObject();
            return builder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Request request = (Request) o;
            return Objects.equals(name, request.name) &&
                Objects.equals(remoteCluster, request.remoteCluster) &&
                Objects.equals(leaderIndexPatterns, request.leaderIndexPatterns) &&
                Objects.equals(followIndexNamePattern, request.followIndexNamePattern) &&
                Objects.equals(parameters, request.parameters);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, remoteCluster, leaderIndexPatterns, followIndexNamePattern, parameters);
        }

        // This class only exists for reuse of the FollowParameters class, see comment above the parser field.
        private static class PutAutoFollowPatternParameters extends FollowParameters {

            private String remoteCluster;
            private List<String> leaderIndexPatterns;
            private String followIndexNamePattern;
        }

    }

}
