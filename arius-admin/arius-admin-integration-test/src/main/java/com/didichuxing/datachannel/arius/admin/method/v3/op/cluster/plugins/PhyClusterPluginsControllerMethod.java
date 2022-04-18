package com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.plugins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author cjm
 */
public class PhyClusterPluginsControllerMethod {

    public static final String CLUSTER_PHY_PLUGINS = V3_OP + "/cluster/phy/plugins";

    public static Result<List<PluginVO>> pluginList(String cluster) throws IOException {
        String path = String.format("%s/%s/get", CLUSTER_PHY_PLUGINS, cluster);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<PluginVO>>>(){});
    }

    public static Result<Long> add(Map<String, Object> map, String fileFormKey, File fileFormValue) throws IOException {
        String path = CLUSTER_PHY_PLUGINS;
        return JSON.parseObject(AriusClient.postForFileForm(path, fileFormKey, fileFormValue, map), new TypeReference<Result<Long>>(){});
    }

    public static Result<Long> deleteEsClusterConfig(Long pluginId) throws IOException {
        String path = String.format("%s/%d", CLUSTER_PHY_PLUGINS, pluginId);
        return JSON.parseObject(AriusClient.delete(path), new TypeReference<Result<Long>>(){});
    }

    public static Result<Long> edit(PluginDTO pluginDTO) throws IOException {
        String path = CLUSTER_PHY_PLUGINS;
        return JSON.parseObject(AriusClient.put(path, pluginDTO), new TypeReference<Result<Long>>(){});
    }
}
