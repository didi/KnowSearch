package com.didichuxing.datachannel.arius.admin.biz.workorder;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.workorder.WorkOrderDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.workorder.WorkOrderProcessDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.order.OrderTypeVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.order.WorkOrderSubmittedVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.order.WorkOrderVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.order.detail.OrderDetailBaseVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.OrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;

/**
 * @author d06679
 * @date 2019/4/29
 */
public interface WorkOrderManager {

    /**
     * 获取工代类型
     */
    Result<List<OrderTypeVO>> getOrderTypes();

    /**
     * 提交一个工单
     */
    Result<WorkOrderSubmittedVO> submit(WorkOrderDTO workOrderDTO) throws AdminOperateException;

    /**
     * 工单处理流程
     */
    Result process(WorkOrderProcessDTO workOrderProcessDTO);

    /**
     * 插入一条工单
     */
    int insert(WorkOrderPO orderDO);

    /**
     * 通过id更新工单
     */
    int updateOrderById(WorkOrderPO orderDO);

    /**
     * 通过id获取工单
     */
    Result<OrderDetailBaseVO> getById(Long id);

    /**
     * 获取所有的工单
     * @return List<OrderPO>
     */
    List<WorkOrderPO> list();

    /**
     * 撤销工单
     */
    Result cancelOrder(Long id, String userName);

    /**
     * 处理工单工单
     */
    Result processOrder(WorkOrderPO orderDO);

    /**
     * 获取工单申请列表
     */
    Result<List<WorkOrderVO>> getOrderApplyList(String applicant, Integer status);

    /**
     * 获取全部的工单审核列表
     */
    List<WorkOrderPO> getApprovalList(String approver);

    /**
     * 获取通过的工单审核列表
     */
    List<WorkOrderPO> getPassApprovalList(String approver);

    /**
     * 获取除指定类型的工单
     */
    List<WorkOrderPO> getWaitApprovalList(String userName);

    /**
     * 获取工单详情信息
     */
    OrderDetail getBaseDetail(WorkOrderPO orderPO);

    /**
     * 根据集群名称，获取插件工单任务信息
     */
    Result<String> getClusterTaskInfo(String cluster);

    /**
     * 根据状态获取工单列表
     */
    Result<List<WorkOrderVO>> getOrderApprovalListByStatus(Integer status);
}
