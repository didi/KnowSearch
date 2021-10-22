package com.didichuxing.datachannel.arius.admin.persistence.mysql.workorder;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import org.springframework.stereotype.Repository;

/**
 * @author fengqiongfeng
 * @date 2020/8/26
 */
@Repository
public interface WorkOrderDAO {

    int insert(WorkOrderPO param);

    WorkOrderPO getById(@Param("id")Long id);

    List<WorkOrderPO> list();

    int updateOrderStatusById(@Param("id")Long id,
                              @Param("status")Integer status);

    int update(WorkOrderPO param);

    List<WorkOrderPO> listByApplicantAndStatus(@Param("applicant")String applicant,
                                              @Param("status")Integer status);

    List<WorkOrderPO> listByApproverAndStatus(@Param("approver")String approver,
                                             @Param("status")Integer status);

    List<WorkOrderPO> listByStatus(@Param("status")Integer status);

    int updateExtensionsById(WorkOrderPO param);

    List<WorkOrderPO> listByHandleTime(@Param("startTime")Date startTime,
                                      @Param("endTime")Date endTime);
}
