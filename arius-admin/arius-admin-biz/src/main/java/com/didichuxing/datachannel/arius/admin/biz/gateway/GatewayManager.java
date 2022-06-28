package com.didichuxing.datachannel.arius.admin.biz.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.GatewayESUserVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplatePhysicalVO;
import java.util.List;
import java.util.Map;

/**
 * @author didi
 */
public interface GatewayManager {

    /**
     * gateway心跳接口
     * @param heartbeat 心跳对象
     * @return Result<Void>
     */
    Result<Void> heartbeat(GatewayHeartbeat heartbeat);

    /**
     * gateway存活接口
     * @param clusterName 集群名
     * @return Result<Integer>
     */
    Result<Integer> heartbeat(String clusterName);

    /**
     * 获取gateway存活节点列表接口
     * @param clusterName 集群名
     * @return Result<List<GatewayNodeVO>>
     */
    Result<List<GatewayClusterNodeVO>> getGatewayAliveNode(String clusterName);

    /**
     * 获取gateway存活节点列表接口
     * @param clusterName 集群名
     * @return Result<List<String>>
     */
    Result<List<String>> getGatewayAliveNodeNames(String clusterName);

    /**
     * 获取app列表,包含APP全部元信息
     *
     * @return Result<List < GatewayESUserVO>>
     */
    Result<List<GatewayESUserVO>> listESUserByProject();

    /**
     * 以map结构组织,key是表达式
     * @param cluster 集群
     * @return Result<Map<String, GatewayTemplatePhysicalVO>>
     */
    Result<Map<String, GatewayTemplatePhysicalVO>> getTemplateMap(String cluster);

    /**
     * 获取模板信息，包含主主从结构组织
     * @param dataCenter 数据中心
     * @return Result<Map<String, GatewayTemplateDeployInfoVO>>
     */
    Result<Map<String, GatewayTemplateDeployInfoVO>> listDeployInfo(String dataCenter);

    /**
     * 滚动获取查询模板数据
     * @param request 请求
     * @return Result<ScrollDslTemplateResponse>
     */
    Result<ScrollDslTemplateResponse> scrollSearchDslTemplate(ScrollDslTemplateRequest request);

    /**
     * addAlias
     * @param indexTemplateAliasDTO 模板dto
     * @return Result<Boolean>
     */
    Result<Boolean> addAlias(IndexTemplateAliasDTO indexTemplateAliasDTO);

    /**
     * delAlias
     * @param indexTemplateAliasDTO 模板dto
     * @return Result<Boolean>
     */
    Result<Boolean> delAlias(IndexTemplateAliasDTO indexTemplateAliasDTO);

    /**
     * sql语句翻译
     * @param sql sql查询语句
     * @param projectId 项目id
     * @return 翻译结果
     */
    Result<String> sqlExplain(String sql, Integer projectId);

    /**
     * sql语句直接查询
     * @param sql sql查询语句
     * @param phyClusterName 指定查询物理集群名
     * @param projectId 项目id
     * @return 数据查询结果
     */
    Result<String> directSqlSearch(String sql, String phyClusterName, Integer projectId);

}