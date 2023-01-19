/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ml.action;

import org.elasticsearch.Version;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.action.support.master.MasterNodeOperationRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.StatusToXContentObject;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.xpack.core.ml.job.config.Job;
import org.elasticsearch.xpack.core.ml.job.process.autodetect.state.ModelSnapshot;
import org.elasticsearch.xpack.core.ml.utils.ExceptionsHelper;

import java.io.IOException;
import java.util.Objects;

public class RevertModelSnapshotAction extends ActionType<RevertModelSnapshotAction.Response> {

    public static final RevertModelSnapshotAction INSTANCE = new RevertModelSnapshotAction();
    public static final String NAME = "cluster:admin/xpack/ml/job/model_snapshots/revert";

    private RevertModelSnapshotAction() {
        super(NAME, Response::new);
    }

    public static class Request extends AcknowledgedRequest<Request> implements ToXContentObject {

        public static final ParseField SNAPSHOT_ID = new ParseField("snapshot_id");
        public static final ParseField DELETE_INTERVENING = new ParseField("delete_intervening_results");

        private static final ObjectParser<Request, Void> PARSER = new ObjectParser<>(NAME, Request::new);
        static {
            PARSER.declareString((request, jobId) -> request.jobId = jobId, Job.ID);
            PARSER.declareString((request, snapshotId) -> request.snapshotId = snapshotId, SNAPSHOT_ID);
            PARSER.declareBoolean(Request::setDeleteInterveningResults, DELETE_INTERVENING);
        }

        public static Request parseRequest(String jobId, String snapshotId, XContentParser parser) {
            Request request = PARSER.apply(parser, null);
            if (jobId != null) {
                request.jobId = jobId;
            }
            if (snapshotId != null) {
                request.snapshotId = snapshotId;
            }
            return request;
        }

        private String jobId;
        private String snapshotId;
        private boolean deleteInterveningResults;

        public Request() {
        }

        public Request(StreamInput in) throws IOException {
            super(in);
            jobId = in.readString();
            snapshotId = in.readString();
            deleteInterveningResults = in.readBoolean();
        }

        public Request(String jobId, String snapshotId) {
            this.jobId = ExceptionsHelper.requireNonNull(jobId, Job.ID.getPreferredName());
            this.snapshotId = ExceptionsHelper.requireNonNull(snapshotId, SNAPSHOT_ID.getPreferredName());
        }

        public String getJobId() {
            return jobId;
        }

        public String getSnapshotId() {
            return snapshotId;
        }

        public boolean getDeleteInterveningResults() {
            return deleteInterveningResults;
        }

        public void setDeleteInterveningResults(boolean deleteInterveningResults) {
            this.deleteInterveningResults = deleteInterveningResults;
        }

        @Override
        public ActionRequestValidationException validate() {
            return null;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeString(jobId);
            out.writeString(snapshotId);
            out.writeBoolean(deleteInterveningResults);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            builder.field(Job.ID.getPreferredName(), jobId);
            builder.field(SNAPSHOT_ID.getPreferredName(), snapshotId);
            builder.field(DELETE_INTERVENING.getPreferredName(), deleteInterveningResults);
            builder.endObject();
            return builder;
        }

        @Override
        public int hashCode() {
            return Objects.hash(jobId, snapshotId, deleteInterveningResults);
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
            return Objects.equals(jobId, other.jobId) && Objects.equals(snapshotId, other.snapshotId)
                    && Objects.equals(deleteInterveningResults, other.deleteInterveningResults);
        }
    }

    static class RequestBuilder extends MasterNodeOperationRequestBuilder<Request, Response, RequestBuilder> {

        RequestBuilder(ElasticsearchClient client) {
            super(client, INSTANCE, new Request());
        }
    }

    public static class Response extends ActionResponse implements StatusToXContentObject {

        private static final ParseField MODEL = new ParseField("model");
        private ModelSnapshot model;

        public Response(StreamInput in) throws IOException {
            super(in);
            if (in.getVersion().before(Version.V_6_3_0)) {
                //the acknowledged flag was removed
                in.readBoolean();
            }
            model = new ModelSnapshot(in);
        }


        public Response(ModelSnapshot modelSnapshot) {
            model = modelSnapshot;
        }

        public ModelSnapshot getModel() {
            return model;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            if (out.getVersion().before(Version.V_6_3_0)) {
                //the acknowledged flag is no longer supported
                out.writeBoolean(true);
            }
            model.writeTo(out);
        }

        @Override
        public RestStatus status() {
            return RestStatus.OK;
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            builder.field(MODEL.getPreferredName());
            builder = model.toXContent(builder, params);
            builder.endObject();
            return builder;
        }

        @Override
        public int hashCode() {
            return Objects.hash(model);
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
            return Objects.equals(model, other.model);
        }

        @Override
        public final String toString() {
            return Strings.toString(this);
        }
    }

}
