package org.elasticsearch.plugin.spatial.router;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.plugin.spatial.config.IndexSpatialConfig;
import org.elasticsearch.plugin.spatial.config.SpatialConfig;
import org.elasticsearch.plugin.spatial.util.SpatialUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Router {
    private SpatialConfig spatialConfig;
    public void setNodeClient(NodeClient client) {
        this.spatialConfig = new SpatialConfig(client);
    }

    public RouterResult doIndexRequest(IngestDocument ingestDocument) {
        // 获得具体的索引名
        String indexName = ingestDocument.getFieldValue(IngestDocument.MetaData.INDEX.getFieldName(), String.class);
        IndexSpatialConfig indexConfig = spatialConfig.getIndexConfig(indexName);

        RouterParam param = new RouterParam();
        param.lat = ingestDocument.getFieldValue(indexConfig.getGeoField() + ".lat", Double.class);
        param.lng = ingestDocument.getFieldValue(indexConfig.getGeoField() + ".lon", Double.class);

        RouterResult rr = doRouter(indexConfig, param);
        rr.toRoutings(indexConfig);
        return  rr;
    }


    public RouterResult doQueryRequest(SearchRequest searchRequest) {
        String[] indices = searchRequest.indices();
        if(indices==null || indices.length!=1) {
            throw new RuntimeException("can not search multi index");
        }

        String indexName = indices[0];

        IndexSpatialConfig indexConfig = spatialConfig.getIndexConfig(indexName);
        RouterParam rp = SpatialUtils.getSpatialInfo(indexConfig, searchRequest.source());

        return  doRouter(indexConfig, rp);
    }

    /* 计算routing信息*/
    private RouterResult doRouter(IndexSpatialConfig indexConfig, RouterParam param) {
        try {
            Set<Integer> cityIdShardIds = indexConfig.getRoutingByCityId(param.cityIds);
            Set<Integer> spatialShardIds = indexConfig.getRoutingBySpatial(param.lng, param.lat, param.radius);

            Set<Integer> shardIds = new HashSet<>();
            shardIds.addAll(cityIdShardIds);
            shardIds.addAll(spatialShardIds);

            Iterator<Integer> iterator = shardIds.iterator();
            while (iterator.hasNext()) {
                int shardId = iterator.next();
                if (shardId < 0 || shardId >= indexConfig.getShardNum()) {
                    iterator.remove();
                }
            }

            if (shardIds.isEmpty()) {
                throw new Exception("result shard ids is empty");
            }

            RouterResult rr = new RouterResult(indexConfig.getIndexName(), shardIds);
            return rr;
        } catch (Throwable t) {
            throw new RuntimeException("spatial search error" + t.getMessage(), t);
        }
    }
}
