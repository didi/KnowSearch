package com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.DCDRMasterSlaveSwitchDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalDCDRDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DCDRSingleTemplateMasterSlaveSwitchDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DCDRTasksDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateDCDRInfoVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import java.util.List;

/**
 * DCDR服务
 * @author didi
 */
public interface TemplateDCDRManager {
    /**
     * 复制并且创建DCDR链路
     *
     * @param templateId    模板ID
     * @param targetCluster 物理集群名称
     * @param regionId      regionId信息
     * @param operator      操作人
     * @param projectId
     * @return Result
     * @throws AdminOperateException 管理操作Exception
     */
    Result<Void> copyAndCreateDCDR(Integer templateId, String targetCluster, Integer regionId, String operator,
                                   Integer projectId) throws AdminOperateException;

    /**
     * createPhyDCDR
     * @param param
     * @param operator
     * @return
     * @throws ESOperateException
     */
    Result<Void> createPhyDCDR(TemplatePhysicalDCDRDTO param, String operator) throws ESOperateException;

    /**
     * 删除DCDR
     *
     * @param templateId 模板ID
     * @param operator   操作人
     * @param projectId
     * @return result
     * @throws ESOperateException
     */
    Result<Void> deleteDCDR(Integer templateId, String operator, Integer projectId,boolean isDCDRForce) throws ESOperateException;

    /**
     * deletePhyDCDR
     *
     * @param param
     * @param operator
     * @param projectId
     * @return
     * @throws ESOperateException
     */
    Result<Void> deletePhyDCDR(TemplatePhysicalDCDRDTO param, String operator, Integer projectId) throws ESOperateException;

    /**
     * 批量DCDR主从切换
     *
     * @param dcdrMasterSlaveSwitchDTO
     * @param operator
     * @param projectId
     * @return
     */
    Result<WorkTaskVO> batchDCDRSwitchMaster2Slave(DCDRMasterSlaveSwitchDTO dcdrMasterSlaveSwitchDTO, String operator,
                                                   Integer projectId);

    /**
     * 根据任务id和模板id取消DCDR主从切换
     *
     * @param taskId
     * @param templateIds
     * @param fullDeleteFlag
     * @param operator
     * @param projectId
     * @return
     * @throws ESOperateException
     */
    Result<Void> cancelDCDRSwitchMasterSlaveByTaskIdAndTemplateIds(Integer taskId, List<Long> templateIds,
                                                                   boolean fullDeleteFlag, String operator,
                                                                   Integer projectId) throws ESOperateException;

    /**
     * 取消dcdr主从切换任务id 根据任务id取消DCDR主从切换
     *
     * @param taskId    任务id
     * @param operator  操作人或角色
     * @param projectId
     * @return {@link Result}<{@link Void}>
     * @throws ESOperateException esoperateException
     */
    Result<Void> cancelDCDRSwitchMasterSlaveByTaskId(Integer taskId, String operator,
                                                     Integer projectId) throws ESOperateException;

    /**
     * 刷新dcdrChannel状态
     *
     * @param taskId     任务id
     * @param templateId 模板id
     * @param operator   操作人或角色
     * @param projectId
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> refreshDCDRChannelState(Integer taskId, Integer templateId, String operator, Integer projectId);

    /**
     * 异步刷新DCDR任务状态
     *
     * @param taskId     业务key
     * @param templateId 模板id
     *
     @param operator 操作人或角色
      * @return {@link Result}<{@link Void}>
     */
    Result<Void> asyncRefreshDCDRChannelState(Integer taskId, Integer templateId, String operator);

    /**
     * 主从强制切换接口
     *
     * @param taskId
     * @param templateId
     * @param operator
     * @param projectId
     * @return
     */
    Result<Void> forceSwitchMasterSlave(Integer taskId, Integer templateId, String operator, Integer projectId);

    /**
     * 获取DCDR主从切换任务详情
     *
     * @param taskId
     * @return
     */
    Result<DCDRTasksDetailVO> getDCDRMasterSlaveSwitchDetailVO(Integer taskId);

    /**
     * 获取单个模板DCDR主从切换详情
     * @param taskId         任务id
     * @param templateId     模板id
     * @return
     */
    Result<DCDRSingleTemplateMasterSlaveSwitchDetailVO> getDCDRSingleTemplateMasterSlaveSwitchDetailVO(Integer taskId,
                                                                                                       Long templateId);

    /**
     * 创建DCDR模板
     * @param physicalId 物理模板ID
     * @param replicaCluster 从集群名称
     * @return result
     @param retryCount 重试计数
     @throws ESOperateException esoperateException
     */
    boolean syncCreateTemplateDCDR(Long physicalId, String replicaCluster, int retryCount) throws ESOperateException;

    /**
     * 删除DCDR模板
     * @param physicalId 物理模板ID
     * @param replicaCluster 从集群名称
     * @return result
     @param retryCount 重试计数
     @throws ESOperateException esoperateException
     */
    boolean syncDeleteTemplateDCDR(Long physicalId, String replicaCluster, int retryCount) throws ESOperateException;

    /**
     * 是否存在
     * @param physicalId 物理模板ID
     * @param replicaCluster 从集群名称
     * @return true/false
     */
    boolean syncExistTemplateDCDR(Long physicalId, String replicaCluster) throws ESOperateException;

    /**
     * 删除索引DCDR链路
     * @param cluster 集群
     * @param replicaCluster 从集群
     * @param indices 索引列表
     * @param retryCount 重试次数
     * @return result
     @throws ESOperateException esoperateException
     */
    boolean syncDeleteIndexDCDR(String cluster, String replicaCluster, List<String> indices,
                                int retryCount) throws ESOperateException;

    /**
     * 修改索引配置
     * @param cluster 集群
     * @param indices 索引
     * @param replicaIndex DCDR配置
     * @param retryCount 重试次数
     * @return result
     @throws ESOperateException esoperateException
     */
    boolean syncDCDRSetting(String cluster, List<String> indices, boolean replicaIndex,
                            int retryCount) throws ESOperateException;

    /**
     * 判断集群是否支持DCDR
     * @param cluster 集群名称
     * @return
     */
    boolean clusterSupport(String cluster);

    /**
     * 根据模板Id获取主从DCDR位点
     * @param templateId    模板id
     * @return
     */
    Tuple<Long/*主模板位点*/, Long/*从模板位点*/> getMasterAndSlaveTemplateCheckPoint(Integer templateId)
		    throws ESOperateException;

    /**
     * 获取模板DCDR信息
     * @param templateId
     * @return
     */
    Result<TemplateDCDRInfoVO> getTemplateDCDRInfoVO(Integer templateId) throws ESOperateException;
    
    /**
     * 重建DCDR链路异常索引
     *
     * @param cluster       源集群的集群名称
     * @param targetCluster 目标集群名称
     * @param indices       要重建的索引
     * @return 操作的结果。
     */
    Result<Void> rebuildDCDRLinkAbnormalIndices(String cluster, String targetCluster,
                                                List<String> indices)throws ESOperateException;
    
}