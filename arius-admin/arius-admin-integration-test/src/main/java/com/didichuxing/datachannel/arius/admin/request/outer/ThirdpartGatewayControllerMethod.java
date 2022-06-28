package com.didichuxing.datachannel.arius.admin.request.outer;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_THIRD_PART;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.GatewayESUserVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.util.AriusClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wuxuan
 * @Date 2022/6/16
 */
public class ThirdpartGatewayControllerMethod {
    public static final String THIRDPART_GATEWAY = V2_THIRD_PART + "/gateway";

    public static Result<Void> heartbeat(GatewayHeartbeat heartbeat) throws IOException {
        String path = String.format("%s/heartbeat", THIRDPART_GATEWAY);
        return JSON.parseObject(AriusClient.put(path, heartbeat), new TypeReference<Result<Void>>() {
        });
    }

    public static Result<Integer> heartbeat(String clusterName) throws IOException {
        String path = String.format("%s/alivecount", THIRDPART_GATEWAY);
        Map<String, Object> params = new HashMap<>(1);
        params.put("clusterName", clusterName);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<Result<Integer>>() {
        });
    }

    public static Result<List<String>> getGatewayAliveNodeNames() throws IOException {
        String path = String.format("%s/aliveNodeName", THIRDPART_GATEWAY);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<String>>>() {
        });
    }

    public static Result<List<GatewayESUserVO>> listApp() throws IOException {
        String path = String.format("%s/listApp", THIRDPART_GATEWAY);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<GatewayESUserVO>>>() {
        });
    }

    public static Result<Map<String, GatewayTemplatePhysicalVO>> getTemplateMap(String cluster) throws IOException {
        String path = String.format("%s/getTemplateMap", THIRDPART_GATEWAY);
        Map<String, Object> params = new HashMap<>(1);
        params.put("cluster", cluster);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<Result<Map<String, GatewayTemplatePhysicalVO>>>() {
        });
    }

    public static Result<Map<String, GatewayTemplateDeployInfoVO>> listDeployInfo(String dataCenter) throws IOException {
        String path = String.format("%s/listDeployInfo", THIRDPART_GATEWAY);
        Map<String, Object> params = new HashMap<>(1);
        params.put("dataCenter", dataCenter);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<Result<Map<String, GatewayTemplateDeployInfoVO>>>() {
        });
    }

    public static Result<ScrollDslTemplateResponse> scrollSearchDslTemplate(ScrollDslTemplateRequest request) throws IOException {
        String path = String.format("%s/dsl/scrollDslTemplates",THIRDPART_GATEWAY);
        return JSON.parseObject(AriusClient.post(path,request),new TypeReference<Result<ScrollDslTemplateResponse>>(){});
    }

}