package com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.phy;

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
public class ESPhyClusterTemplateSrvControllerMethod {

    public static final String PHY_CLUSTER_TEMPLATESRV = V3_OP + "/phy/cluster/templateSrv";

    public static Result<List<ESClusterTemplateSrvVO>> list(String clusterName) throws IOException {
        String path = String.format("%s/%s", PHY_CLUSTER_TEMPLATESRV, clusterName);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<ESClusterTemplateSrvVO>>>(){});
    }

    public static Result<List<ESClusterTemplateSrvVO>> listSelect(String clusterName) throws IOException {
        String path = String.format("%s/%s/select", PHY_CLUSTER_TEMPLATESRV, clusterName);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<ESClusterTemplateSrvVO>>>(){});
    }

    public static Result<Boolean> addTemplateSrvId(String clusterName, String templateSrvId) throws IOException {
        String path = String.format("%s/%s/%s", PHY_CLUSTER_TEMPLATESRV, clusterName, templateSrvId);
        return JSON.parseObject(AriusClient.put(path), new TypeReference<Result<Boolean>>(){});
    }

    public static Result<Boolean> delTemplateSrvId(String clusterName, String templateSrvId) throws IOException {
        String path = String.format("%s/%s/%s", PHY_CLUSTER_TEMPLATESRV, clusterName, templateSrvId);
        return JSON.parseObject(AriusClient.delete(path), new TypeReference<Result<Boolean>>(){});
    }
}
