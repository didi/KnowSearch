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
     * @param templateId           模板ID
     * @param targetCluster       物理集群名称
     * @param regionId            regionId信息
     * @param operator            操作人
     * @return Result
     *
     @throws AdminOperateException 管理操作Exception
     */
    Result<Void> copyAndCreateDCDR(Integer templateId, String targetCluster, Integer regionId,
                                   String operator) throws AdminOperateException;

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
     * @param templateId 模板ID
     * @param operator 操作人
     * @return result
     * @throws ESOperateException
     */
    Result<Void> deleteDCDR(Integer templateId, String operator) throws ESOperateException;

    /**
     * deletePhyDCDR
     * @param param
     * @param operator
     * @return
     * @throws ESOperateException
     */
    Result<Void> deletePhyDCDR(TemplatePhysicalDCDRDTO param, String operator) throws ESOperateException;

    /**
     * 批量DCDR主从切换
     * @param dcdrMasterSlaveSwitchDTO
     * @param operator
     * @return
     */
    Result<WorkTaskVO> batchDCDRSwitchMaster2Slave(DCDRMasterSlaveSwitchDTO dcdrMasterSlaveSwitchDTO, String operator);

    /**
     * 根据任务id和模板id取消DCDR主从切换
     * @param taskId
     * @param templateId
     * @param fullDeleteFlag
     * @param operator
     * @return
     * @throws ESOperateException
     */
    Result<Void> cancelDCDRSwitchMasterSlaveByTaskIdAndTemplateIds(Integer taskId, List<Long> templateId,
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
    Result<Void> cancelDCDRSwitchMasterSlaveByTaskId(Integer taskId, String operator) throws ESOperateException;

  /**
     * 刷新dcdrChannel状态
     *
     * @param taskId 任务id
     * @param templateId 模板id
     * @param operator 操作人或角色
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> refreshDCDRChannelState(Integer taskId, Integer templateId, String operator);

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
     * @param taskId
     * @param templateId
     * @param operator
     * @return
     */
    Result<Void> forceSwitchMasterSlave(Integer taskId, Integer templateId, String operator);

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
    boolean syncExistTemplateDCDR(Long physicalId, String replicaCluster);

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
    Tuple<Long/*主模板位点*/, Long/*从模板位点*/> getMasterAndSlaveTemplateCheckPoint(Integer templateId);

    /**
     * 获取模板DCDR信息
     * @param templateId
     * @return
     */
    Result<TemplateDCDRInfoVO> getTemplateDCDRInfoVO(Integer templateId);
}