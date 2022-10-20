package com.didichuxing.datachannel.arius.admin.persistence.mysql.task;

import com.didichuxing.datachannel.arius.admin.common.bean.po.task.ecm.EcmTaskDetailPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ES集群 工单任务详情 Mapper 接口
 * @author didi
 * @since 2020-08-24
 */
@Repository
public interface EcmTaskDetailDAO {
    int replace(EcmTaskDetailPO param);

    int save(EcmTaskDetailPO param);

    EcmTaskDetailPO getById(Long taskDetailId);

    List<EcmTaskDetailPO> listByWorkOrderTaskId(Long workOrderTaskId);

    int updateStatus(@Param("workOrderTaskId") Long workOrderTaskId, @Param("hostname") String hostname,
                     @Param("status") String status);

    List<EcmTaskDetailPO> listByTaskIdAndRoleAndWorkOrderTaskId(@Param("workOrderTaskId") Integer workOrderTaskId,
                                                                @Param("role") String role,
                                                                @Param("taskId") Integer taskId);

    int updateTaskIdByRoleAndWorkOrderTaskId(@Param("workOrderTaskId") Long workOrderTaskId, @Param("role") String role,
                                             @Param("taskId") Long taskId);

    int update(EcmTaskDetailPO param);

    EcmTaskDetailPO getByWorkOderIdAndHostName(@Param("workOrderTaskId") Long workOrderId,
                                               @Param("hostname") String hostname);

    int deleteEcmTaskDetailsByTaskOrder(Long workOrderTaskId);
}
