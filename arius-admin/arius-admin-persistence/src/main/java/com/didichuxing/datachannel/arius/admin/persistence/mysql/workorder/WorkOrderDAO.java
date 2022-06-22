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
    
    /**
     * 新增
     *
     * @param param 入参
     * @return int
     */
    int insert(WorkOrderPO param);
    
    /**
     * 获取通过id
     *
     * @param id id
     * @return {@link WorkOrderPO}
     */
    WorkOrderPO getById(@Param("id")Long id);

    List<WorkOrderPO> list();
    
    /**
     * 更新订单状态通过id
     *
     * @param id id
     * @param status 状态
     * @return int
     */
    int updateOrderStatusById(@Param("id")Long id,
                              @Param("status")Integer status);

    int update(WorkOrderPO param);
    
    /**
     * 由申请人和状态列表
     *
     * @param applicant 申请人
     * @param status 状态
     * @return {@link List}<{@link WorkOrderPO}>
     */
    List<WorkOrderPO> listByApplicantAndStatus(@Param("applicant")String applicant,
                                               @Param("status")Integer status);
    
    /**
     * 审批人列表
     *
     * @param approver 审批人
     * @param status 状态
     * @return {@link List}<{@link WorkOrderPO}>
     */
    List<WorkOrderPO> listByApproverAndStatus(@Param("approver")String approver,
                                              @Param("status")Integer status);
    
    /**
     * 列表状态
     *
     * @param status
     * @return {@link List}<{@link WorkOrderPO}>
     */
    List<WorkOrderPO> listByStatus(@Param("status")Integer status);
    
    /**
     * 更新扩展id
     *
     * @param param 入参
     * @return int
     */
    int updateExtensionsById(WorkOrderPO param);
    
    /**
     * 列表处理时间
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return {@link List}<{@link WorkOrderPO}>
     */
    List<WorkOrderPO> listByHandleTime(@Param("startTime")Date startTime,
                                       @Param("endTime")Date endTime);
}