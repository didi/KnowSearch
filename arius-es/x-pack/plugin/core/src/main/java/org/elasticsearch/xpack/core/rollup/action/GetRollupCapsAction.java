/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.rollup.action;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.xpack.core.rollup.RollupField;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class GetRollupCapsAction extends ActionType<GetRollupCapsAction.Response> {

    public static final GetRollupCapsAction INSTANCE = new GetRollupCapsAction();
    public static final String NAME = "cluster:monitor/xpack/rollup/get/caps";
    public static final ParseField CONFIG = new ParseField("config");
    public static final ParseField STATUS = new ParseField("status");

    private GetRollupCapsAction() {
        super(NAME, GetRollupCapsAction.Response::new);
    }

    public static class Request extends ActionRequest implements ToXContentFragment {
        private String indexPattern;

        public Request(String indexPattern) {
            if (Strings.isNullOrEmpty(indexPattern) || indexPattern.equals("*")) {
                this.indexPattern = MetaData.ALL;
            } else {
                this.indexPattern = indexPattern;
            }
        }

        public Request() {}

        public Request(StreamInput in) throws IOException {
            super(in);
            this.indexPattern = in.readString();
        }

        public String getIndexPattern() {
            return indexPattern;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeString(indexPattern);
        }

        @Override
        public ActionRequestValidationException validate() {
            return null;
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(RollupField.ID.getPreferredName(), indexPattern);
            return builder;
        }

        @Override
        public int hashCode() {
            return Objects.hash(indexPattern);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Request other = (Request) obj;
            return Objects.equals(indexPattern, other.indexPattern);
        }
    }

    public static class RequestBuilder extends ActionRequestBuilder<Request, Response> {

        protected RequestBuilder(ElasticsearchClient client, GetRollupCapsAction action) {
            super(client, action, new Request());
        }
    }

    public static class Response extends ActionResponse implements Writeable, ToXContentObject {

        private Map<String, RollableIndexCaps> jobs = Collections.emptyMap();

        public Response() {

        }

        public Response(Map<String, RollableIndexCaps> jobs) {
            this.jobs = Collections.unmodifiableMap(Objects.requireNonNull(jobs));
        }

        Response(StreamInput in) throws IOException {
            jobs = Collections.unmodifiableMap(in.readMap(StreamInput::readString, RollableIndexCaps::new));
        }

        public Map<String, RollableIndexCaps> getJobs() {
            return jobs;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeMap(jobs, StreamOutput::writeString, (out1, value) -> value.writeTo(out1));
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            {
                for (Map.Entry<String, RollableIndexCaps> entry : jobs.entrySet()) {
                    entry.getValue().toXContent(builder, params);
                }
            }
            builder.endObject();
            return builder;
        }

        @Override
        public int hashCode() {
            return Objects.hash(jobs);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Response other = (Response) obj;
            return Objects.equals(jobs, other.jobs);
        }

        @Override
        public final String toString() {
            return Strings.toString(this);
        }
    }
}
