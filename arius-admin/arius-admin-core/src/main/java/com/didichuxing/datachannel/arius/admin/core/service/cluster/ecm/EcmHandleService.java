package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant.EcmActionEnum;
import com.google.common.collect.Multimap;

/**
 * ES集群表 服务类
 * @author didi
 * @since 2020-08-24
 */
public interface EcmHandleService {
    /**
     * 新建一个集群
     * @param ecmParamBaseList
     * @return Result
     */
    Result<Long> saveESCluster(List<EcmParamBase> ecmParamBaseList);

    /**
     * 启动集群
     * @param ecmParamBase  集群ID
     * @param operator      操作人
     * @return
     */
    Result<EcmOperateAppBase> startESCluster(EcmParamBase ecmParamBase, String operator);

    /**
     * 集群扩缩容
     * @param ecmParamBase  集群角色
     * @param operator      操作人
     * @return Result
     */
    Result<EcmOperateAppBase> scaleESCluster(EcmParamBase ecmParamBase, String operator);

    /**
     * 集群升级
     * @param   ecmParamBase  集群角色
     * @param   operator      操作人
     * @return Result
     */
    Result<EcmOperateAppBase> upgradeESCluster(EcmParamBase ecmParamBase, String operator);

    /**
     * 重启集群
     * @param ecmParamBase  集群角色
     * @param operator      操作人
     * @return Result
     */
    Result<EcmOperateAppBase> restartESCluster(EcmParamBase ecmParamBase, String operator);

    /**
     * 根据物理集群Id删除集群
     * @param clusterId     集群Id
     * @param operator      操作人
     * @return
     */
    Result<Void> deleteESCluster(Long clusterId, String operator);

    /**
     * 继续启动角色集群中未完成的节点任务
     * @param operator  操作人
     * @return Result
     */
    Result<EcmOperateAppBase> actionUnfinishedESCluster(EcmActionEnum ecmActionEnum, EcmParamBase actionParamBase,
                                                        String hostname, String operator);

    /**
     * 获取集群节点信息
     * @param clusterId
     * @param operator  操作人
     * @return Result
     */
    Result<String> infoESCluster(Long clusterId, String operator);

    /**
     * 获取容器云节点日志信息
     * @param ecmParamBase   容器云任务Id
     * @param hostname       容器云主机IP
     * @return Result
     */
    Result<EcmSubTaskLog> getSubTaskLog(EcmParamBase ecmParamBase, String hostname, String operator);

    /**
     * 获取本次操作所有机器的状态
     * @param actionParamBase
     * @param operator
     * @return
     */
    Result<List<EcmTaskStatus>> getESClusterStatus(EcmParamBase actionParamBase, Integer orderType, String operator);

    /**
     * 依据集群ID & 角色列表构造EcmParamBase
     * @param  phyClusterId  物理集群ID
     * @param  roleNameList  角色列表
     * @return Result
     */
    Result<List<EcmParamBase>> buildEcmParamBaseList(Integer phyClusterId, List<String> roleNameList);

    /**
     * 获取带集群配置的Ecm参数列表
     * @param phyClusterId
     * @param roleNameList
     * @param role2ConfigIdsMultiMap
     * @param actionType
     * @return
     */
    Result<List<EcmParamBase>> buildEcmParamBaseListWithConfigAction(Integer phyClusterId, List<String> roleNameList,
                                                                     Multimap<String, Long> role2ConfigIdsMultiMap,
                                                                     Integer actionType);

    /**
     * 获取带集群Plugin的Ecm参数列表
     */
    Result<List<EcmParamBase>> buildEcmParamBaseListWithEsPluginAction(Integer phyClusterId, List<String> roleNameList,
                                                                       Long EsPluginId, Integer actionType);
}
