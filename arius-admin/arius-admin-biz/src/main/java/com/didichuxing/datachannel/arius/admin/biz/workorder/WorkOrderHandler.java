package com.didichuxing.datachannel.arius.admin.biz.workorder;

import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.workorder.WorkOrderProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;

/**
 * @author d06679
 * @date 2018/9/18
 */
public interface WorkOrderHandler extends BaseHandle {

    /**
     * 创建一个工单
     *
     * 1、校验工单内容是否合法
     * 2、构建工单请求
     * 3、提交工单
     *
     * @param workOrder 工单数据
     * @return result
     */
    Result<WorkOrderPO> submit(WorkOrder workOrder) throws AdminOperateException;

    /**
     * 处理工单
     * @param workOrder 工单内容
     * @return result
     */
    Result processAgree(WorkOrder workOrder, String approver, String opinion) throws AdminOperateException;

    /**
     * 处理审核不同意的工单
     * @param processDTO 工单内容
     * @return result
     */
    Result processDisagree(WorkOrderPO orderPO, WorkOrderProcessDTO processDTO);

    /**
     * 工单是否自动审批
     * @param workOrder 工单类型
     * @return result
     */
    boolean canAutoReview(WorkOrder workOrder);

    /**
     * 获取工单详细信息
     * @param extensions 扩展信息
     * @return AbstractOrderDetail
     */
    AbstractOrderDetail getOrderDetail(String extensions);

    /**
     * 获取审批人列表
     * @param detail 扩展信息
     * @return List<AriusUserInfo>
     */
    List<AriusUserInfo> getApproverList(AbstractOrderDetail detail);

    /**
     * 是否审批人员
     * @param orderPO 订单信息, userName 审批人名称
     * @return Result
     */
    Result checkAuthority(WorkOrderPO orderPO, String userName);

}
