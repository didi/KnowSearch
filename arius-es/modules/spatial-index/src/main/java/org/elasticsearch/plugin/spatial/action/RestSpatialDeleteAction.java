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

package org.elasticsearch.plugin.spatial.action;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.plugin.spatial.router.Router;
import org.elasticsearch.plugin.spatial.router.RouterResult;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestActions;
import org.elasticsearch.rest.action.RestStatusToXContentListener;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.POST;

public class RestSpatialDeleteAction extends BaseRestHandler {
    private Router router;

    public RestSpatialDeleteAction(Settings settings, RestController controller, Router router) {
        controller.registerHandler(POST, "/{index}/{type}/{id}/_spatial_delete", this);

        this.router = router;
    }

    @Override
    public String getName() {
        return "spatial_document_delete_action";
    }

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(request.param("index"), request.param("type"), request.param("id"));
        deleteRequest.timeout(request.paramAsTime("timeout", DeleteRequest.DEFAULT_TIMEOUT));
        deleteRequest.setRefreshPolicy(request.param("refresh"));
        deleteRequest.version(RestActions.parseVersion(request));
        deleteRequest.versionType(VersionType.fromString(request.param("version_type"), deleteRequest.versionType()));
        deleteRequest.setIfSeqNo(request.paramAsLong("if_seq_no", deleteRequest.ifSeqNo()));
        deleteRequest.setIfPrimaryTerm(request.paramAsLong("if_primary_term", deleteRequest.ifPrimaryTerm()));
        deleteRequest.setPipeline(request.param("pipeline"));
        if (request.hasContent()) {
            deleteRequest.source(request.requiredContent(), request.getXContentType());
        }

        String waitForActiveShards = request.param("wait_for_active_shards");
        if (waitForActiveShards != null) {
            deleteRequest.waitForActiveShards(ActiveShardCount.parseString(waitForActiveShards));
        }

        IngestDocument ingestDocument = new IngestDocument(
            deleteRequest.index(),
            deleteRequest.type(),
            deleteRequest.id(),
            deleteRequest.routing(),
            deleteRequest.version(),
            deleteRequest.versionType(),
            XContentHelper.convertToMap(request.requiredContent(), false, request.getXContentType()).v2()
        );
        RouterResult rr = router.doIndexRequest(ingestDocument);
        if(rr.getShardIds().size()>1) {
            throw new RuntimeException("have multi shardId");
        }

        if(rr.getShardIds().size()==0) {
            throw new RuntimeException("not have shardId");
        }

        deleteRequest.routing(rr.getRoutingStr());

        return channel -> client.delete(deleteRequest, new RestStatusToXContentListener<>(channel));
    }
}
