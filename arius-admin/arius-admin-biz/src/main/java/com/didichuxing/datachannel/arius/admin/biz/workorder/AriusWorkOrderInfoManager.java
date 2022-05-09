package com.didichuxing.datachannel.arius.admin.biz.workorder;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.OrderTypeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.AriusWorkOrderInfoSubmittedVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.AriusWorkOrderInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.detail.OrderDetailBaseVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.OrderInfoDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.AriusWorkOrderInfoPO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;

/**
 * @author d06679
 * @date 2019/4/29
 */
public interface AriusWorkOrderInfoManager {
    
    /**获取订单类型
     * 获取工代类型
     * @return {@link Result}<{@link List}<{@link OrderTypeVO}>>
     */
    Result<List<OrderTypeVO>> getOrderTypes();
    
    /**提交
     * 提交一个工单
     @param workOrderDTO 工作订单dto
      * @return {@link Result}<{@link AriusWorkOrderInfoSubmittedVO}>
     @throws AdminOperateException 管理操作Exception
     */
    Result<AriusWorkOrderInfoSubmittedVO> submit(WorkOrderDTO workOrderDTO) throws AdminOperateException;
    
    /**过程
     * 工单处理流程
     @param workOrderProcessDTO 工作订单流程dto
      * @return {@link Result}<{@link Void}>
     */
    Result<Void> process(WorkOrderProcessDTO workOrderProcessDTO);
    
    /**新增
     * 插入一条工单
     @param orderDO 订单做
     @return int
     */
    int insert(AriusWorkOrderInfoPO orderDO);
    
    /**更新订单id
     * 通过id更新工单
     @param orderDO 订单做
     @return int
     */
    int updateOrderById(AriusWorkOrderInfoPO orderDO);
    
    /**获取通过id
     * 通过id获取工单
     @param id id
      * @return {@link Result}<{@link OrderDetailBaseVO}>
     */
    Result<OrderDetailBaseVO> getById(Long id);

    /**
     * 获取所有的工单
     * @return List<OrderPO>
     */
    List<AriusWorkOrderInfoPO> list();
    
    /**取消订单
     * 撤销工单
     @param id id
     @param userName 用户名
      * @return {@link Result}<{@link Void}>
     */
    Result<Void> cancelOrder(Long id, String userName);
    
    /**过程顺序
     * 处理工单工单
     @param orderDO 订单做
      * @return {@link Result}<{@link Void}>
     */
    Result<Void> processOrder(AriusWorkOrderInfoPO orderDO);
    
    /**获取订单应用列表
     * 获取工单申请列表
     @param applicant 申请人
     @param status 状态
      * @return {@link Result}<{@link List}<{@link AriusWorkOrderInfoVO}>>
     */
    Result<List<AriusWorkOrderInfoVO>> getOrderApplyList(String applicant, Integer status);
    
    /**获取批准列表
     * 获取全部的工单审核列表
     @param approver 审批人
      * @return {@link List}<{@link AriusWorkOrderInfoPO}>
     */
    List<AriusWorkOrderInfoPO> getApprovalList(String approver);
    
    /**获取通过批准列表
     * 获取通过的工单审核列表
     @param approver 审批人
      * @return {@link List}<{@link AriusWorkOrderInfoPO}>
     */
    List<AriusWorkOrderInfoPO> getPassApprovalList(String approver);
    
    /**获取等待批准列表
     * 获取除指定类型的工单
     @param userName 用户名
      * @return {@link List}<{@link AriusWorkOrderInfoPO}>
     */
    List<AriusWorkOrderInfoPO> getWaitApprovalList(String userName);
    
    /**获取抽象类细节
     * 获取工单详情信息
     @param orderPO 订单订单
      * @return {@link OrderInfoDetail}
     */
    OrderInfoDetail getBaseDetail(AriusWorkOrderInfoPO orderPO);
    
    /**获取订单批准列表通过状态
     * 根据状态获取工单列表
     @param status 状态
      * @return {@link Result}<{@link List}<{@link AriusWorkOrderInfoVO}>>
     */
    Result<List<AriusWorkOrderInfoVO>> getOrderApprovalListByStatus(Integer status);
}