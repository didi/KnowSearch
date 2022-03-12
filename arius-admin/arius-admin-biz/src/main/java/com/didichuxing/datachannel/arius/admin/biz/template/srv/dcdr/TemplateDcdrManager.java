package com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplatePhysicalDCDRDTO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

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
    Result<Void> createDcdr(Integer logicId, String operator) throws ESOperateException;

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
     */
    Result<Void> dcdrSwitchMasterSlave(Integer logicId, Long expectMasterPhysicalId, int step, String operator);

    /**
     * 创建dcdr模板
     * @param physicalId 物理模板ID
     * @param replicaCluster 从集群名称
     * @return result
     */
    boolean syncCreateTemplateDCDR(Long physicalId, String replicaCluster, int retryCount) throws ESOperateException;

    /**
     * 删除dcdr模板
     * @param physicalId 物理模板ID
     * @param replicaCluster 从集群名称
     * @return result
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
     * 删除索引dcdr链路
     * @param cluster 集群
     * @param replicaCluster 从集群
     * @param indices 索引列表
     * @param retryCount 重试次数
     * @return result
     */
    boolean syncDeleteIndexDCDR(String cluster, String replicaCluster, List<String> indices,
                                int retryCount) throws ESOperateException;

    /**
     * 修改索引配置
     * @param cluster 集群
     * @param indices 索引
     * @param replicaIndex dcdr配置
     * @param retryCount 重试次数
     * @return result
     */
    boolean syncDCDRSetting(String cluster, List<String> indices, boolean replicaIndex,
                            int retryCount) throws ESOperateException;

    /**
     * 判断集群是否支持dcdr
     * @param cluster 集群名称
     * @return
     */
    boolean clusterSupport(String cluster);
}
