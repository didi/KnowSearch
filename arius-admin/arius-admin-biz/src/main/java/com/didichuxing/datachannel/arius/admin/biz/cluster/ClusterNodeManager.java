package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostWithRegionInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import java.util.List;

/**
 * @author ohushenglin_v
 * @date 2022-05-30
 */
public interface ClusterNodeManager {
    /**
     * 获取可划分至region的节点信息
     * @param clusterId   物理集群Id
     * @return            Result<List<ESClusterRoleHostVO>>
     */
    @Deprecated
    Result<List<ESClusterRoleHostWithRegionInfoVO>> listDivide2ClusterNodeInfo(Long clusterId);

    /**
     * 获取可划分至region的节点信息
     * @param clusterId   物理集群Id
     * @param divideType  region划分方式
     * @return            Result<List<ESClusterRoleHostVO>>
     */
    Result<List<ESClusterRoleHostWithRegionInfoVO>> listDivide2ClusterNodeInfoWithDivideType(Long clusterId, String divideType);
    /**
     * 划分指定节点至region
     *
     * @param params    集群带节点信息的Region实体
     * @param operator  操作者
     * @param projectId
     * @return Result<Long>
     */
    Result<List<Long>> createMultiNode2Region(List<ClusterRegionWithNodeInfoDTO> params, String operator,
                                              Integer projectId) throws AdminOperateException;

    /**
     * 编辑节点的region属性
     *
     * @param params        集群带节点信息的Region实体
     * @param operator      操作者
     * @param projectId
     * @param operationEnum
     * @return Result<Boolean>
     */
    Result<Boolean> editMultiNode2Region(List<ClusterRegionWithNodeInfoDTO> params, String operator,
                                         Integer projectId, OperationEnum operationEnum) throws AdminOperateException;

    /**
     * 获取物理集群节点列表
     *
     * @param clusterId 集群id
     * @return {@link Result}<{@link List}<{@link ESClusterRoleHostVO}>>
     */
    Result<List<ESClusterRoleHostVO>> listClusterPhyNode(Integer clusterId);

    /**
     * 获取逻辑集群节点列表
     *
     * @param clusterId 集群id
     * @return {@link Result}<{@link List}<{@link ESClusterRoleHostVO}>>
     */
    Result<List<ESClusterRoleHostVO>> listClusterLogicNode(Integer clusterId);

    /**
     * 通过逻辑集群名称获取节点
     * @param clusterLogicName
     * @return
     */
    Result listClusterLogicNodeByName(String clusterLogicName);

    Result<List<ESClusterRoleHostVO>> listClusterRoleHostByRegionId(Long regionId);

    /**
     * 采集集群节点数据
     *
     * @param cluster 集群
     * @return boolean
     * @throws AdminTaskException 管理任务异常
     */
    boolean collectNodeSettings(String cluster) throws AdminTaskException;
    
    /**
     * > 该功能用于删除集群节点，但该节点必须离线且未绑定 region
     *
     * @param ids       要删除的节点的 id
     * @param projectId 项目编号
     * @param operator  操作员是执行操作的用户。
     */
    Result<Void> delete(List<Integer> ids, Integer projectId, String operator);

    /**
     * 校验节点的region划分
     * @param params
     * @param operator
     * @param projectId
     * @return
     */
    Result<Boolean> checkMultiNode2Region(List<ClusterRegionWithNodeInfoDTO> params, String operator, Integer projectId);
}