/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.core.ccr.action;

import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.support.master.MasterNodeRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.xpack.core.ccr.AutoFollowStats;

import java.io.IOException;
import java.util.Objects;

public class CcrStatsAction extends ActionType<CcrStatsAction.Response> {

    public static final String NAME = "cluster:monitor/ccr/stats";
    public static final CcrStatsAction INSTANCE = new CcrStatsAction();

    private CcrStatsAction() {
        super(NAME, CcrStatsAction.Response::new);
    }

    public static class Request extends MasterNodeRequest<Request> {

        public Request(StreamInput in) throws IOException {
            super(in);
        }

        public Request() {
        }

        @Override
        public ActionRequestValidationException validate() {
            return null;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
        }
    }

    public static class Response extends ActionResponse implements ToXContentObject {

        private final AutoFollowStats autoFollowStats;
        private final FollowStatsAction.StatsResponses followStats;

        public Response(AutoFollowStats autoFollowStats, FollowStatsAction.StatsResponses followStats) {
            this.autoFollowStats = Objects.requireNonNull(autoFollowStats);
            this.followStats = Objects.requireNonNull(followStats);
        }

        public Response(StreamInput in) throws IOException {
            super(in);
            autoFollowStats = new AutoFollowStats(in);
            followStats = new FollowStatsAction.StatsResponses(in);
        }

        public AutoFollowStats getAutoFollowStats() {
            return autoFollowStats;
        }

        public FollowStatsAction.StatsResponses getFollowStats() {
            return followStats;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            autoFollowStats.writeTo(out);
            followStats.writeTo(out);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            {
                builder.field("auto_follow_stats", autoFollowStats, params);
                builder.field("follow_stats", followStats, params);
            }
            builder.endObject();
            return builder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Response response = (Response) o;
            return Objects.equals(autoFollowStats, response.autoFollowStats) &&
                    Objects.equals(followStats, response.followStats);
        }

        @Override
        public int hashCode() {
            return Objects.hash(autoFollowStats, followStats);
        }
    }

}
