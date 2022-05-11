package com.didichuxing.datachannel.arius.admin.biz.template.srv;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterTemplateSrv;

public interface TemplateSrvManager {

    /**
     * 根据索引服务的id获取索引服务的描述
     * @param srvId
     * @return
     */
    ClusterTemplateSrv getTemplateServiceBySrvId(int srvId);

    /**
     * 查询开启了某个索引服务的物理集群列表
     * @param srvId
     * @return
     */
    List<String> getPhyClusterByOpenTemplateSrv(int srvId);

    /**
     * 查询开启了某个索引服务的物理集群列表
     * @param clusterPhies
     * @param srvId
     * @return
     */
    List<String> getPhyClusterByOpenTemplateSrv(List<ClusterPhy> clusterPhies, int srvId);

    /**
     * 判断物理集群是否打开了某个索引服务
     * @param phyCluster        物理集群名称
     * @param srvId
     * @return
     */
    boolean isPhyClusterOpenTemplateSrv(String phyCluster, int srvId);

    /**
     * 判断物理集群是否打开了某个索引服务
     * @param phyCluster    ClusterPhy 物理集群对象
     * @param srvId
     * @return
     */
    boolean isPhyClusterOpenTemplateSrv(ClusterPhy phyCluster, int srvId);
    /**
     * 获取物理集群可选择的索引服务
     * @param phyCluster
     * @return
     */
    Result<List<ClusterTemplateSrv>> getPhyClusterSelectableTemplateSrv(String phyCluster);

    /**
     * 获取逻辑集群可选择的索引服务
     * @param clusterLogicId 逻辑集群Id
     * @return
     */
    Result<List<ESClusterTemplateSrvVO>> getClusterLogicSelectableTemplateSrv(Long clusterLogicId);

    /**
     * 获取phyCluster已经开启的索引服务
     * @param phyCluster
     * @return
     */
    Result<List<ClusterTemplateSrv>> getPhyClusterTemplateSrv(String phyCluster);

    /**
     * 获取phyCluster已经开启的索引服务
     * @param clusterPhy
     * @return
     */
    Result<List<ClusterTemplateSrv>> getPhyClusterTemplateSrv(ClusterPhy clusterPhy);
    /**
     * 获取逻辑集群索引服务
     * @param clusterLogicId
     * @return
     */
    Result<List<ESClusterTemplateSrvVO>> getClusterLogicTemplateSrv(Long clusterLogicId);

    /**
     * 获取物理集群索引服务列表Id
     * @param phyCluster
     * @return
     */
    List<Integer> getPhyClusterTemplateSrvIds(String phyCluster);

    /**
     * 获取某个逻辑集群已经开启的索引服务（逻辑集群绑定的物理集群已经开启的索引服务）
     * @param logicClusterId
     * @return
     */
    Result<List<ClusterTemplateSrv>> getLogicClusterTemplateSrv(Long logicClusterId);

    /**
     * 为一个物理集群增加一个索引服务
     * @param phyCluster 物理集群名称
     * @param strId 模板服务id
     * @param operator 操作人员
     * @return 校验结果
     */
    Result<Boolean> checkTemplateSrv(String phyCluster, String strId, String operator);

    /**
     * 校验一个接入的集群是否可以开启指定的索引服务
     * @param httpAddresses client的地址
     * @param password 集群认证信息
     * @param strId 模板服务id
     * @param password 密码
     * @return 校验结果 返回确认可开启的索引服务id
     */
    Result<Boolean> checkTemplateSrvWhenJoin(String httpAddresses, String password, String strId);

    /**
     * 为逻辑集群增加一个索引服务
     * @param clusterLogicId  逻辑集群Id
     * @param templateSrvId   索引服务Id
     * @param operator        操作者
     * @return
     */
    Result<Boolean> addTemplateSrvForClusterLogic(Long clusterLogicId, String templateSrvId, String operator);

    /**
     * 为一个物理集群增加多个索引服务
     * @param phyCluster     物理集群
     * @param templateSrvIds 索引服务列表
     * @param operator       操作人
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> replaceTemplateServes(String phyCluster, List<Integer> templateSrvIds, String operator);

    /**
     * 为一个物理集群删除一个索引服务
     * @param phyCluster    物理集群名称
     * @param templateSrvId templateSrvId
     * @param operator      操作人
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> delTemplateSrv(String phyCluster, String templateSrvId, String operator);

    /**
     * 逻辑集群删除一个索引服务
     * @param clusterLogicId
     * @param templateSrvId
     * @param operator
     * @return
     */
    Result<Boolean> delTemplateSrvForClusterLogic(Long clusterLogicId, String templateSrvId, String operator);

    /**
     * 清理所有索引服务
     * @param clusterPhy 物理集群名称
     * @param operator   操作人
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> delAllTemplateSrvByClusterPhy(String clusterPhy, String operator);
}
