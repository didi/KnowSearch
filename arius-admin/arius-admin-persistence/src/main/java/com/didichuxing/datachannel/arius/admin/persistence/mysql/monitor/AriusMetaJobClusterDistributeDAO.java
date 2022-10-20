package com.didichuxing.datachannel.arius.admin.persistence.mysql.monitor;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.AriusMetaJobClusterDistributePO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author ohushenglin_v
 */
@Repository
public interface AriusMetaJobClusterDistributeDAO {

    /**
     * 获取所有的采集集群
     *
     * @return 可采集的集群信息
     */
    List<AriusMetaJobClusterDistributePO> getAllTask();

    /**
     * 根据数据中心获取所有的采集集群
     *
     * @param dataCentre 数据中心
     * @return 可采集的集群信息
     */
    List<AriusMetaJobClusterDistributePO> getAllTaskByDataCentre(String dataCentre);

    /**
     * 根据host获取所有的采集集群
     *
     * @param monitorHost   host
     * @param size          size
     * @return 可采集的集群信息
     */
    List<AriusMetaJobClusterDistributePO> getTaskByHost(@Param("monitorHost") String monitorHost,
                                                        @Param("size") int size);

    /**
     * 使用这个方法时要特别小心
     * @param ariusMetaJobClusterDistributePO  变更数据
     * @return 变更数量
     */
    int updateMonitorHost(AriusMetaJobClusterDistributePO ariusMetaJobClusterDistributePO);

    /**
     * 更新监控时间
     * @param ariusMetaJobClusterDistributePO   变更数据
     * @return 变更数量
     */
    int updateMonitorTime(AriusMetaJobClusterDistributePO ariusMetaJobClusterDistributePO);

    /**
     * 批量插入
     * @param newTasks 插入的数据
     * @return 变更数量
     */
    int insertBatch(List<AriusMetaJobClusterDistributePO> newTasks);

    /**
     * 新增
     * @param taskEntity 插入的数据
     * @return 变更数量
     */
    int insert(AriusMetaJobClusterDistributePO taskEntity);

    /**
     * 批量删除
     * @param deleteIds 删除ID列表
     * @return 变更数量
     */
    int deleteBatch(List<Long> deleteIds);
}
