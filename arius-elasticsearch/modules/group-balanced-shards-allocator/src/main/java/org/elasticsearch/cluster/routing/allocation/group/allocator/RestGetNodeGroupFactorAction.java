package org.elasticsearch.cluster.routing.allocation.group.allocator;

import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.RoutingNode;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.allocation.allocator.IndexGroupSettings;
import org.elasticsearch.common.Table;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.action.RestResponseListener;
import org.elasticsearch.rest.action.cat.AbstractCatAction;
import org.elasticsearch.rest.action.cat.RestTable;

import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.cluster.routing.ShardRoutingState.RELOCATING;

/**
 * author weizijun
 * date：2019-07-18
 */
public class RestGetNodeGroupFactorAction extends AbstractCatAction {

    public RestGetNodeGroupFactorAction(Settings settings, RestController controller) {
        controller.registerHandler(RestRequest.Method.GET, "/_cat/node/group/factor/{node}/{group}", this);
        controller.registerHandler(RestRequest.Method.GET, "/_cat/node/group/factor/{node}", this);
    }

    @Override
    public RestChannelConsumer doCatRequest(final RestRequest request, final NodeClient client) {
        final String node = request.param("node");
        final String group = request.param("group");
        final ClusterStateRequest clusterStateRequest = new ClusterStateRequest();
        clusterStateRequest.local(true);
        clusterStateRequest.clear().nodes(true).metaData(true).routingTable(true);
        return channel -> client.admin().cluster().state(clusterStateRequest, new RestResponseListener<ClusterStateResponse>(channel) {
            @Override
            public RestResponse buildResponse(final ClusterStateResponse clusterStateResponse) throws Exception {
                return RestTable.buildResponse(buildTable(request, clusterStateResponse, node, group), channel);
            }
        });
    }

    @Override
    public String getName() {
        return "get_group_factor_action";
    }

    @Override
    protected void documentation(StringBuilder sb) {
        sb.append("/_cat/node/group/factor\n");
    }

    @Override
    protected Table getTableWithHeader(final RestRequest request) {
        Table table = new Table();
        table.startHeaders()
            .addCell("node")
            .addCell("group")
            .addCell("index")
            .addCell("factor")
            .endHeaders();
        return table;
    }

    private Table buildTable(RestRequest request, ClusterStateResponse state, String node, String groupFilter) {
        Table table = getTableWithHeader(request);
        MetaData metaData = state.getState().metaData();
        Map<String, Map<String, Float>> groupIndexFactors = new HashMap<>();

        DiscoveryNode discoveryNode = state.getState().nodes().resolveNode(node);
        RoutingNode routingNode = state.getState().getRoutingNodes().node(discoveryNode.getId());
        for (ShardRouting shard : routingNode) {
            if (shard.state() != RELOCATING) {
                // add group factor
                IndexMetaData indexMetaData = metaData.index(shard.getIndexName());
                float shardFactor = indexMetaData.getSettings().getAsFloat(IndexGroupSettings.INDEX_GROUP_FACTOR, 0f);
                String group = indexMetaData.getSettings().get(IndexGroupSettings.INDEX_GROUP_NAME);

                if (group != null) {
                    Map<String, Float> indexFactors = groupIndexFactors.get(group);
                    if (indexFactors == null) {
                        indexFactors = new HashMap<>();
                        groupIndexFactors.put(group, indexFactors);
                    }

                    String index = shard.index().getName();

                    Float totalFactor = indexFactors.get(index);
                    if (totalFactor == null) {
                        indexFactors.put(index, shardFactor);
                    } else {
                        indexFactors.put(index, totalFactor + shardFactor);
                    }
                }
            }
        }

        groupIndexFactors.forEach((group, value) -> {
            value.forEach((index, factor) -> {
                if (groupFilter != null && !group.equals(groupFilter)) {
                    return;
                }

                table.startRow();
                table.addCell(node);
                table.addCell(group);
                table.addCell(index);
                table.addCell(factor);
                table.endRow();
            });
        });

        return table;
    }

}
