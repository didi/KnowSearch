package com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.logic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESClusterTemplateSrvVO;

import java.io.IOException;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author cjm
 */
public class ClusterLogicTemplateSrvControllerMethod {

    public static final String LOGIC_CLUSTER_TEMPLATESRV = V3_OP + "/logic/cluster/templateSrv";

    public static Result<List<ESClusterTemplateSrvVO>> list(Long clusterLogicId) throws IOException {
        String path = String.format("%s/%d", LOGIC_CLUSTER_TEMPLATESRV, clusterLogicId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<ESClusterTemplateSrvVO>>>(){});
    }

    public static Result<List<ESClusterTemplateSrvVO>> listSelect(Long clusterLogicId) throws IOException {
        String path = String.format("%s/%d/select", LOGIC_CLUSTER_TEMPLATESRV, clusterLogicId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<ESClusterTemplateSrvVO>>>(){});
    }

    public static Result<Boolean> addTemplateSrvId(Long clusterLogicId, String templateSrvId) throws IOException {
        String path = String.format("%s/%d/%s", LOGIC_CLUSTER_TEMPLATESRV, clusterLogicId, templateSrvId);
        return JSON.parseObject(AriusClient.put(path), new TypeReference<Result<Boolean>>(){});
    }

    public static Result<Boolean> delTemplateSrvId(Long clusterLogicId, String templateSrvId) throws IOException {
        String path = String.format("%s/%d/%s", LOGIC_CLUSTER_TEMPLATESRV, clusterLogicId, templateSrvId);
        return JSON.parseObject(AriusClient.delete(path), new TypeReference<Result<Boolean>>() {});
    }
}
