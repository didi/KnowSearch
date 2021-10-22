package com.didichuxing.datachannel.arius.admin.persistence.mysql.task;

import com.didichuxing.datachannel.arius.admin.common.bean.po.task.ecm.EcmTaskPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EcmTaskDAO {
    int save(EcmTaskPO param);

    List<EcmTaskPO> listAll();

    EcmTaskPO getById(Long id);

    int update(EcmTaskPO param);

    int updateStatusById(@Param("id") Long  id, @Param("status") String status);

    List<EcmTaskPO> listUndoWorkOrderTaskByClusterId(Long physicClusterId);

    // todo 去掉 running，只查询处于 waiting状态的工单任务
    EcmTaskPO getRunningWorkOrderTaskByClusterId(Integer physicClusterId);
}
