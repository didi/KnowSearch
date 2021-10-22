package com.didichuxing.datachannel.arius.admin.biz.gateway;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.client.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.GatewayAppVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.gateway.GatewayNodeVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.GatewayTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.GatewayTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;

public interface GatewayManager {

    /**
     * gateway心跳接口
     * @param heartbeat
     * @return
     */
    Result heartbeat(GatewayHeartbeat heartbeat);

    /**
     * gateway存活接口
     * @param clusterName
     * @return
     */
    Result<Integer> heartbeat(String clusterName);

    /**
     * 获取gateway存活节点列表接口
     * @param clusterName
     * @return
     */
    Result<List<GatewayNodeVO>> getGatewayAliveNode(String clusterName);

    /**
     * 获取app列表,包含APP全部元信息
     * @param request
     * @return
     */
    Result<List<GatewayAppVO>> listApp(HttpServletRequest request);

    /**
     * 以map结构组织,key是表达式
     * @param cluster
     * @return
     */
    Result<Map<String, GatewayTemplatePhysicalVO>> getTemplateMap(String cluster);

    /**
     * 获取模板信息，包含主主从结构组织
     * @param dataCenter
     * @return
     */
    Result<Map<String, GatewayTemplateDeployInfoVO>> listDeployInfo(String dataCenter);

    /**
     * 滚动获取查询模板数据
     * @param request
     * @return
     */
    Result<ScrollDslTemplateResponse> scrollSearchDslTemplate(ScrollDslTemplateRequest request);
}
