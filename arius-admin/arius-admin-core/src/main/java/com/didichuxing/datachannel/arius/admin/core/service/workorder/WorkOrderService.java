package com.didichuxing.datachannel.arius.admin.core.service.workorder;

import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.WorkOrderVO;
import java.util.List;

/**
 * 工作订单service
 *
 * @author shizeying
 * @date 2022/08/12
 */
public interface WorkOrderService {
    /**
     * 获取通过id
     *
     * @param orderId 订单id
     * @return {@link WorkOrderPO}
     */
    WorkOrderPO getById(Long orderId);
    
    /**
     * 新增
     *
     * @param orderPO 订单po
     * @return int
     */
    int insert(WorkOrderPO orderPO);
    
    /**
     * 更新
     *
     * @param orderPO 订单po
     * @return int
     */
    int update(WorkOrderPO orderPO);
    
    /**
     * 列表
     *
     * @return {@link List}<{@link WorkOrderPO}>
     */
    List<WorkOrderPO> list();
    
    /**
     * 更新订单状态通过id
     *
     * @param id   id
     * @param code code
     * @return boolean
     */
    boolean updateOrderStatusById(Long id, Integer code);
    
    /**
     * 由申请人和状态列表
     *
     * @param applicant 申请人
     * @param status    状态
     * @return {@link List}<{@link WorkOrderVO}>
     */
    List<WorkOrderVO> listByApplicantAndStatus(String applicant, Integer status);
    
    /**
     * 通过状态和项目id列表
     *
     * @param status    状态
     * @param projectId 项目id
     * @return {@link List}<{@link WorkOrderVO}>
     */
    List<WorkOrderVO> listByStatusAndProjectId(Integer status, Integer projectId);
    
    /**
     * 由审批人列表和地位
     *
     * @param approver 审批人
     * @param status   status
     * @return {@link List}<{@link WorkOrderPO}>
     */
    List<WorkOrderPO> listByApproverAndStatus(String approver, Integer status);
    
    /**
     * 列表状态
     *
     * @param status 代码
     * @return {@link List}<{@link WorkOrderPO}>
     */
    List<WorkOrderPO> listByStatus(Integer status);
}