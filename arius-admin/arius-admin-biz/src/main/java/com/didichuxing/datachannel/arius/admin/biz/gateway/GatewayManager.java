package com.didichuxing.datachannel.arius.admin.biz.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.GatewayAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface GatewayManager {

    /**
     * gateway心跳接口
     * @param heartbeat
     * @return
     */
    Result<Void> heartbeat(GatewayHeartbeat heartbeat);

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
     * 获取gateway存活节点列表接口
     * @param clusterName
     * @return
     */
    Result<List<String>> getGatewayAliveNodeNames(String clusterName);

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

    /**
     * addAlias
     * @param indexTemplateAliasDTO
     * @return
     */
    Result<Boolean> addAlias(IndexTemplateAliasDTO indexTemplateAliasDTO);

    /**
     * delAlias
     * @param indexTemplateAliasDTO
     * @return
     */
    Result<Boolean> delAlias(IndexTemplateAliasDTO indexTemplateAliasDTO);

    /**
     * sql语句翻译
     * @param sql sql查询语句
     * @param appId 项目id
     * @return 翻译结果
     */
    Result<String> sqlExplain(String sql, Integer appId);

    /**
     * sql语句直接查询
     * @param sql sql查询语句
     * @param phyClusterName 指定查询物理集群名
     * @param appId 项目id
     * @return 数据查询结果
     */
    Result<String> directSqlSearch(String sql, String phyClusterName, Integer appId);

}
