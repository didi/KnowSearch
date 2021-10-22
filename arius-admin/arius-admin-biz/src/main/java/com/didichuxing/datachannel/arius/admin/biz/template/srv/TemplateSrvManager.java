package com.didichuxing.datachannel.arius.admin.biz.template.srv;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterTemplateSrv;

import java.util.List;

public interface TemplateSrvManager {

    /**
     * 根据索引服务的id获取索引服务的描述
     * @param srvId
     * @return
     */
    ESClusterTemplateSrv getTemplateServiceBySrvId(int srvId);

    /**
     * 判断物理集群是否打开了某个索引服务
     * @param phyCluster
     * @param srvId
     * @return
     */
    boolean isPhyClusterOpenTemplateSrv(String phyCluster, int srvId);

    /**
     * 获取phyCluster集群可选择的索引服务
     * @param phyCluster
     * @return
     */
    Result<List<ESClusterTemplateSrv>> getPhyClusterSelectableTemplateSrv(String phyCluster);

    /**
     * 获取phyCluster已经开启的索引服务
     * @param phyCluster
     * @return
     */
    Result<List<ESClusterTemplateSrv>> getPhyClusterTemplateSrv(String phyCluster);

    /**
     * 获取某个逻辑集群已经开启的索引服务（逻辑集群绑定的物理集群已经开启的索引服务）
     * @param logicClusterId
     * @return
     */
    Result<List<ESClusterTemplateSrv>> getLogicClusterTemplateSrv(Long logicClusterId);

    /**
     * 为一个物理集群增加一个索引服务
     * @param phyCluster
     * @param strId
     * @return
     */
    Result<Boolean> addTemplateSrv(String phyCluster, String strId, String operator);

    /**
     * 为一个物理集群删除一个索引服务
     * @param phyCluster
     * @param strId
     * @return
     */
    Result<Boolean> delTemplateSrv(String phyCluster, String strId, String operator);
}
