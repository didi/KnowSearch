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
 * dcdr服务
 * @author didi
 */
public interface TemplateDcdrManager {

    /**
     * 创建dcdr
     * @param logicId 模板ID
     * @param operator 操作人
     * @return result
     * @throws ESOperateException
     */
    @Deprecated
    Result<Void> createDcdr(Integer logicId, String operator) throws ESOperateException;
    
    /**复制和创造dcdr
     * 复制并且创建dcdr链路
     * @param templateId           模板ID
     * @param targetCluster       物理集群名称
     * @param rack                rack信息
     * @param operator            操作人
     * @return Result
     *
     @throws AdminOperateException 管理操作Exception
     */
    Result<Void> copyAndCreateDcdr(Integer templateId, String targetCluster, String rack,
                                   String operator) throws AdminOperateException;

    /**
     * createPhyDcdr
     * @param param
     * @param operator
     * @return
     * @throws ESOperateException
     */
    Result<Void> createPhyDcdr(TemplatePhysicalDCDRDTO param, String operator) throws ESOperateException;

    /**
     * 删除dcdr
     * @param logicId 模板ID
     * @param operator 操作人
     * @return result
     * @throws ESOperateException
     */
    Result<Void> deleteDcdr(Integer logicId, String operator) throws ESOperateException;

    /**
     * deletePhyDcdr
     * @param param
     * @param operator
     * @return
     * @throws ESOperateException
     */
    Result<Void> deletePhyDcdr(TemplatePhysicalDCDRDTO param, String operator) throws ESOperateException;
    
    /**
     * dcdr主从切换
     * @param logicId 逻辑模板ID
     * @param expectMasterPhysicalId 主
     * @param operator 操作人
     * @return result
     @param step 一步
     */
    Result<Void> dcdrSwitchMasterSlave(Integer logicId, Long expectMasterPhysicalId, int step, String operator);

    /**
     * 批量dcdr主从切换
     * @param dcdrMasterSlaveSwitchDTO
     * @param operator
     * @return
     */
    Result<WorkTaskVO> batchDcdrSwitchMaster2Slave(DCDRMasterSlaveSwitchDTO dcdrMasterSlaveSwitchDTO, String operator);

    /**
     * 根据任务id和模板id取消DCDR主从切换
     * @param taskId
     * @param templateId
     * @param fullDeleteFlag
     * @param operator
     * @return
     * @throws ESOperateException
     */
    Result<Void> cancelDcdrSwitchMasterSlaveByTaskIdAndTemplateIds(Integer taskId, List<Long> templateId,
                                                                   boolean fullDeleteFlag,
                                                                   String operator) throws ESOperateException;
    
    /**取消dcdr主从切换任务id
     * 根据任务id取消DCDR主从切换
     *
     @param taskId 任务id
     @param operator 操作人或角色
      * @return {@link Result}<{@link Void}>
     @throws ESOperateException esoperateException
     */
    Result<Void> cancelDcdrSwitchMasterSlaveByTaskId(Integer taskId, String operator) throws ESOperateException;
    
    /**
     * 刷新dcdrChannel状态
     *
     * @param taskId 任务id
     * @param templateId 模板id
     * @param operator 操作人或角色
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> refreshDcdrChannelState(Integer taskId, Integer templateId, String operator);
    
    /**异步刷新dcdrChannel状态
     * 异步刷新dcdr任务状态
     *
     * @param taskId     业务key
     * @param templateId 模板id
     *
     @param operator 操作人或角色
      * @return {@link Result}<{@link Void}>
     */
    Result<Void> asyncRefreshDcdrChannelState(Integer taskId, Integer templateId, String operator);

    /**
     * 主从强制切换接口
     * @param taskId
     * @param templateId
     * @param operator
     * @return
     */
    Result<Void> forceSwitchMasterSlave(Integer taskId, Integer templateId, String operator);

    /**
     * 获取dcdr主从切换任务详情
     *
     * @param taskId
     * @return
     */
    Result<DCDRTasksDetailVO> getDCDRMasterSlaveSwitchDetailVO(Integer taskId);

    /**
     * 获取单个模板dcdr主从切换详情
     * @param taskId         任务id
     * @param templateId     模板id
     * @return
     */
    Result<DCDRSingleTemplateMasterSlaveSwitchDetailVO> getDCDRSingleTemplateMasterSlaveSwitchDetailVO(Integer taskId,
                                                                                                       Long templateId);
    
    /**同步创建模板dcdr
     * 创建dcdr模板
     * @param physicalId 物理模板ID
     * @param replicaCluster 从集群名称
     * @return result
     @param retryCount 重试计数
     @throws ESOperateException esoperateException
     */
    boolean syncCreateTemplateDCDR(Long physicalId, String replicaCluster, int retryCount) throws ESOperateException;
    
    /**同步删除模板dcdr
     * 删除dcdr模板
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
    boolean syncExistTemplateDCDR(Long physicalId, String replicaCluster);
    
    /**同步删除索引dcdr
     * 删除索引dcdr链路
     * @param cluster 集群
     * @param replicaCluster 从集群
     * @param indices 索引列表
     * @param retryCount 重试次数
     * @return result
     @throws ESOperateException esoperateException
     */
    boolean syncDeleteIndexDCDR(String cluster, String replicaCluster, List<String> indices,
                                int retryCount) throws ESOperateException;
    
    /**同步dcdrsetting
     * 修改索引配置
     * @param cluster 集群
     * @param indices 索引
     * @param replicaIndex dcdr配置
     * @param retryCount 重试次数
     * @return result
     @throws ESOperateException esoperateException
     */
    boolean syncDCDRSetting(String cluster, List<String> indices, boolean replicaIndex,
                            int retryCount) throws ESOperateException;

    /**
     * 判断集群是否支持dcdr
     * @param cluster 集群名称
     * @return
     */
    boolean clusterSupport(String cluster);

    /**
     * 根据模板Id获取主从dcdr位点
     * @param templateId    模板id
     * @return
     */
    Tuple<Long/*主模板位点*/, Long/*从模板位点*/> getMasterAndSlaveTemplateCheckPoint(Integer templateId);

    /**
     * 获取模板dcdr信息
     * @param templateId
     * @return
     */
    Result<TemplateDCDRInfoVO> getTemplateDCDRInfoVO(Integer templateId);
}