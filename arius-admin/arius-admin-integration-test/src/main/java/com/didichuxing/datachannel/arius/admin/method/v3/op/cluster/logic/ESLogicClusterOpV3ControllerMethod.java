package com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.logic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;

import java.io.IOException;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author cjm
 */
public class ESLogicClusterOpV3ControllerMethod {

    public static final String LOGIC_CLUSTER = V3_OP + "/logic/cluster";

    public static Result<List<String>> getAppLogicClusterNames() throws IOException {
        String path = String.format("%s/clusterNames", LOGIC_CLUSTER);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<String>>>(){});
    }

    public static Result<List<ClusterLogicVO>> getAppLogicClusterInfo() throws IOException {
        String path = String.format("%s/list", LOGIC_CLUSTER);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<ClusterLogicVO>>>(){});
    }

    public static Result<List<ClusterLogicVO>> getAppLogicClusterInfoByType(Integer type) throws IOException {
        String path = String.format("%s/%d", LOGIC_CLUSTER, type);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<ClusterLogicVO>>>(){});
    }

    public static PaginationResult<ClusterLogicVO> pageGetConsoleClusterVOS(ClusterLogicConditionDTO condition) throws IOException {
        String path = String.format("%s/page", LOGIC_CLUSTER);
        return JSON.parseObject(AriusClient.post(path, condition), new TypeReference<PaginationResult<ClusterLogicVO>>(){});
    }

    public static Result<ClusterLogicVO> get(Long clusterLogicId) throws IOException {
        String path = String.format("%s/%d/overView", LOGIC_CLUSTER, clusterLogicId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<ClusterLogicVO>>(){});
    }

    public static Result<Void> checkTemplateValidForCreate(Long logicClusterId, String templateSize) throws IOException {
        String path = String.format("%s/%d/%s/sizeCheck", LOGIC_CLUSTER, logicClusterId, templateSize);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<Void>>(){});
    }
}
