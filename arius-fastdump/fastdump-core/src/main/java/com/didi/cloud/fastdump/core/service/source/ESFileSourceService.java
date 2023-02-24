package com.didi.cloud.fastdump.core.service.source;

import static com.didi.cloud.fastdump.common.utils.RetryUtil.DEFAULT_INTERVAL_MILLS;
import static com.didi.cloud.fastdump.common.utils.RetryUtil.DEFAULT_TIME;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.cloud.fastdump.common.bean.es.ShardInfo;
import com.didi.cloud.fastdump.common.bean.source.es.ESIndexSource;
import com.didi.cloud.fastdump.common.client.es.ESRestClient;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.enums.ESClusterVersionEnum;
import com.didi.cloud.fastdump.common.enums.HealthEnum;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.common.utils.ESVersionUtil;

/**
 * Created by linyunan on 2022/8/10
 */
@Component
public class ESFileSourceService implements FileSourceService<ESIndexSource> {
    @Override
    public void parseSource(ESIndexSource esIndexSource) throws Exception {
        String sourceClusterAddress  = esIndexSource.getSourceClusterAddress();
        String sourceClusterUserName = esIndexSource.getSourceClusterUserName();
        String sourceClusterPassword = esIndexSource.getSourceClusterPassword();

        try (ESRestClient esRestClient = new ESRestClient(sourceClusterAddress, sourceClusterUserName, sourceClusterPassword)) {
            // 1. 获取集群版本
            esIndexSource.setSourceClusterVersion(esRestClient.syncRetryGetClusterVersion(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS));

            // 2. flush index
            String indexHealth = esRestClient.syncRetryGetIndicesHealthMap(
                    esIndexSource.getSourceIndex(),
                            DEFAULT_TIME,
                            DEFAULT_INTERVAL_MILLS)
                    .get(esIndexSource.getSourceIndex());
            if (StringUtils.isNotBlank(indexHealth) && !HealthEnum.RED.getDesc().equals(indexHealth)) {
                esRestClient.syncRetryFlushIndices(esIndexSource.getSourceIndex(), DEFAULT_TIME, DEFAULT_INTERVAL_MILLS);
            }

            // 3. 获取索引uuid, 用以拼凑 lucene data path
            // 如index uuid dxEscVvuSNmiCByFlht9hQ
            // 单个lucene(ES6x/7x/8x等版本)文件所在目录: /Users/didi/es-package/elasticsearch-7.6.0/data/nodes/0/indices/dxEscVvuSNmiCByFlht9hQ/0/index
            esIndexSource.setUuid(esRestClient.syncRetryGetIndexUuid(
                    esIndexSource.getSourceIndex(),
                    DEFAULT_TIME,
                    DEFAULT_INTERVAL_MILLS));

            // 4. 获取索引shard分配信息
            List<ShardInfo> shardInfoList = esRestClient.syncRetryGetShardInfoList(
                    esIndexSource.getSourceIndex(),
                    DEFAULT_TIME,
                    DEFAULT_INTERVAL_MILLS);

            // 5. 获取集群数据节点分布详细
            Map<String/*shardNum*/, String/*ip@dataPath*/> shardNum2DataPathMap = getShardNum2DataPathMap(esRestClient,
                    esIndexSource, shardInfoList);
            esIndexSource.setShardNum2DataPathMap(shardNum2DataPathMap);
        }
    }

    private Map<String/*shardNum*/, String/*ip@dataPath*/> getShardNum2DataPathMap(ESRestClient esRestClient,
                                                                                   ESIndexSource esIndexSource,
                                                                                   List<ShardInfo> shardInfoList) throws BaseException {
        // 解析_nodes/stats/fs, 获取data.path中数据
        JSONObject nodeStateJsonObj = esRestClient.syncRetryGetNodeStateJsonObj(DEFAULT_TIME, DEFAULT_INTERVAL_MILLS);

        Map<String, String> shardNum2DataPathMap = new HashMap<>();

        // 2.3.3版本直接用名称，6.6以上的版本使用
        String dataPathIndexPrefix = parseDataPathIndexPrefixByESVersion(esIndexSource);

        for (ShardInfo shardInfo : shardInfoList) {
            // 仅使用主 shard, es是先写主shard成功后转发从
            if ("r".equals(shardInfo.getPrirep())) { continue;}

            JSONArray jsonArray = nodeStateJsonObj.getJSONObject(shardInfo.getNodeId()).getJSONObject("fs").getJSONArray("data");

            String path = jsonArray.getJSONObject(0).getString("path") + "/indices/" + dataPathIndexPrefix +
                    "/" + shardInfo.getShard() + "/index";

            shardNum2DataPathMap.put(shardInfo.getShard(), shardInfo.getIp() + "@" + path);
        }
        return shardNum2DataPathMap;
    }

    private String parseDataPathIndexPrefixByESVersion(ESIndexSource esIndexSource) throws BaseException {
        String sourceClusterVersion = esIndexSource.getSourceClusterVersion();
        String es233Version = ESClusterVersionEnum.ES_2_3_3.getVersion();
        String es661Version = ESClusterVersionEnum.ES_6_6_1.getVersion();
        String es670Version = ESClusterVersionEnum.ES_6_7_0.getVersion();
        String es760Version = ESClusterVersionEnum.ES_7_6_0.getVersion();
        String es840Version = ESClusterVersionEnum.ES_8_4_0.getVersion();
        if (null !=  es233Version && es233Version.equals(sourceClusterVersion)) {
            return esIndexSource.getSourceIndex();
        }
        if (null != es661Version && es661Version.equals(sourceClusterVersion)) {
            return esIndexSource.getUuid();
        }
        if (null != es670Version && es670Version.equals(sourceClusterVersion)) {
            return esIndexSource.getUuid();
        }
        if (null != es760Version && es760Version.equals(sourceClusterVersion)) {
            return esIndexSource.getUuid();
        }
        if (null != es840Version && es840Version.equals(sourceClusterVersion)) {
            return esIndexSource.getUuid();
        }

        throw new BaseException(String.format("暂不支持ES集群Version[%s]", esIndexSource.getSourceClusterVersion()), ResultType.FAIL);
    }
}
