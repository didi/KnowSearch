package com.didichuxing.datachannel.arius.admin.biz.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterBriefVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import java.util.List;

/**
 * 网关集群
 *
 * @author shizeying
 * @date 2022/10/31
 * @since 0.3.2
 */
public interface GatewayClusterManager {
    
    /**
     * 列出所有网关集群简单信息
     *
     * @return GatewayClusterBriefVO 对象的列表。
     */
    Result<List<GatewayClusterBriefVO>> listBriefInfo();
    
    /**
     * 加入网关集群
     *
     * @param param     加入集群所需的参数。
     * @param projectId 项目id
     * @return 网关集群VO
     */
    Result<GatewayClusterVO> join(GatewayClusterJoinDTO param, Integer projectId);
    
    /**
     * 按页面获取网关集群列表
     *
     * @param condition 查询的条件，它是一个 GatewayConditionDTO 对象。
     * @param projectId 项目id
     * @return PaginationResult<GatewayClusterVO>
     */
    PaginationResult<GatewayClusterVO> pageGetCluster(GatewayConditionDTO condition,
        Integer projectId) throws NotFindSubclassException;
}