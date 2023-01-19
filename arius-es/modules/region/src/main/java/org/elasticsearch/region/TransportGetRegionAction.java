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

package org.elasticsearch.region;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.transport.TransportService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransportGetRegionAction extends HandledTransportAction<GetRegionRequest, GetRegionResponse> {

    private final ClusterService clusterService;

    @Inject
    public TransportGetRegionAction(TransportService transportService,
                                    ActionFilters actionFilters,
                                    ClusterService clusterService) {

        super(GetRegionAction.NAME, transportService, actionFilters, GetRegionRequest::new);
        this.clusterService = clusterService;
    }

    @Override
    protected void doExecute(Task task, GetRegionRequest request, ActionListener<GetRegionResponse> listener) {
        Map<String, List<String>> result = new HashMap<>();
        Map<String, List<String>> groups = RegionSettings.CLUSTER_REGION_SEEDS.getAsMap(clusterService.state().metaData().persistentSettings());
        if (request.getRegion().equalsIgnoreCase("all")) {
            for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        } else {
            result.put(request.getRegion(), groups.get(request.getRegion()));
        }
        listener.onResponse(new GetRegionResponse(result));
    }
}
