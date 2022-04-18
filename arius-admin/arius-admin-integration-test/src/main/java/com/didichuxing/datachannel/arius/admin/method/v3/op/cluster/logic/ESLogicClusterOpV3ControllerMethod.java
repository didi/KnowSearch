package com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.logic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterVO;

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

    public static Result<List<ConsoleClusterVO>> getAppLogicClusterInfo() throws IOException {
        String path = String.format("%s/list", LOGIC_CLUSTER);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<ConsoleClusterVO>>>(){});
    }

    public static Result<List<ConsoleClusterVO>> getAppLogicClusterInfoByType(Integer type) throws IOException {
        String path = String.format("%s/%d", LOGIC_CLUSTER, type);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<ConsoleClusterVO>>>(){});
    }

    public static PaginationResult<ConsoleClusterVO> pageGetConsoleClusterVOS(ClusterLogicConditionDTO condition) throws IOException {
        String path = String.format("%s/page", LOGIC_CLUSTER);
        return JSON.parseObject(AriusClient.post(path, condition), new TypeReference<PaginationResult<ConsoleClusterVO>>(){});
    }

    public static Result<ConsoleClusterVO> get(Long clusterLogicId) throws IOException {
        String path = String.format("%s/%d/overView", LOGIC_CLUSTER, clusterLogicId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<ConsoleClusterVO>>(){});
    }

    public static Result<Void> checkTemplateValidForCreate(Long logicClusterId, String templateSize) throws IOException {
        String path = String.format("%s/%d/%s/sizeCheck", LOGIC_CLUSTER, logicClusterId, templateSize);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<Void>>(){});
    }
}
