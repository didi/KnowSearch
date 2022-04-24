package com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm.ESConfigVO;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author cjm
 */
public class ESClusterConfigControllerMethod {

    public static final String CLUSTER_CONFIG = V3_OP + "/cluster/config";

    public static Result<List<ESConfigVO>> gainEsClusterConfigs(Long clusterId) throws IOException {
        String path = String.format("%s/%d/list", CLUSTER_CONFIG, clusterId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<ESConfigVO>>>(){});
    }

    public static Result<ESConfigVO> gainEsClusterConfig(Long configId) throws IOException {
        String path = String.format("%s/%d", CLUSTER_CONFIG, configId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<ESConfigVO>>(){});
    }

    public static Result<Set<String>> gainEsClusterRoles(Long clusterId) throws IOException {
        String path = String.format("%s/%d/roles", CLUSTER_CONFIG, clusterId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<Set<String>>>(){});
    }

    public static Result<ESConfigVO> gainEsClusterTemplateConfig(String type) throws IOException {
        String path = String.format("%s/%s/template", CLUSTER_CONFIG, type);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<ESConfigVO>>(){});
    }

    public static Result<Void> editEsClusterConfigDesc(ESConfigDTO param) throws IOException {
        String path = CLUSTER_CONFIG;
        return JSON.parseObject(AriusClient.put(path, param), new TypeReference<Result<Void>>(){});
    }
}
