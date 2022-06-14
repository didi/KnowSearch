package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import com.alibaba.fastjson.JSONArray;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.ShardDistributionVO;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectRequest;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandEnum.SHARD;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterQuickCommandEnum.SHARD_ASSIGNMENT;

/**
 * Created by linyunan on 3/22/22
 */
@Repository
public class ESShardDAO extends BaseESDAO {
    public List<ShardDistributionVO> catShard(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        List<ShardDistributionVO> ecSegmentsOnIps = null;
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=catShard||clusterName={}||errMsg=esClient is null", clusterName);
            return new ArrayList<>();
        }
        try {
            DirectRequest directRequest = new DirectRequest(SHARD.getMethod(), SHARD.getUri());
            DirectResponse directResponse = client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                    && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                ecSegmentsOnIps = JSONArray.parseArray(directResponse.getResponseContent(), ShardDistributionVO.class);
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=catShard||cluster={}||mg=get es segments fail", clusterName, e);
            return new ArrayList<>();
        }
        return ecSegmentsOnIps;
    }

    public String shardAssignment(String clusterName) {
        ESClient client = esOpClient.getESClient(clusterName);
        String result = null;
        if (Objects.isNull(client)) {
            LOGGER.error("class=ESClusterDAO||method=shardAssignment||clusterName={}||errMsg=esClient is null", clusterName);
            return null;
        }
        try {
            DirectRequest directRequest = new DirectRequest(SHARD_ASSIGNMENT.getMethod(), SHARD_ASSIGNMENT.getUri());
            DirectResponse directResponse = client.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
            if (directResponse.getRestStatus() == RestStatus.OK
                    && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
                result = directResponse.getResponseContent();
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterDAO||method=shardAssignment||cluster={}||mg=get es segments fail", clusterName, e);
            return null;
        }
        return result;
    }
}
