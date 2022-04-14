package com.didichuxing.datachannel.arius.admin.persistence.mysql.monitor;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.ClusterMonitorTaskPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterMonitorTaskDAO {

    /**
     *
     * @return
     */
    List<ClusterMonitorTaskPO> getAllTask();

    /**
     *
     * @return
     */
    List<ClusterMonitorTaskPO> getAllTaskByDataCentre(String dataCentre);

    /**
     *
     * @param monitorHost
     * @return
     */
    List<ClusterMonitorTaskPO> getTaskByHost(@Param("monitorHost") String monitorHost, @Param("size") int size);

    /**
     * 使用这个方法时要特别小心
     * @param clusterMonitorTaskPO
     * @return
     */
    int updateMonitorHost(ClusterMonitorTaskPO clusterMonitorTaskPO);

    /**
     *
     * @return
     */
    int updateMonitorTime(ClusterMonitorTaskPO clusterMonitorTaskPO);

    /**
     *
     * @return
     */
    int insertBatch(List<ClusterMonitorTaskPO> newTasks);

    /**
     *
     * @return
     */
    int insert(ClusterMonitorTaskPO taskEntity);

    /**
     *
     * @return
     */
    int deleteBatch(List<Long> deleteIds);
}
