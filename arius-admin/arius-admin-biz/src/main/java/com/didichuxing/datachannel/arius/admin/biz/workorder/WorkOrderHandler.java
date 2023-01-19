package com.didichuxing.datachannel.arius.admin.biz.workorder;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didiglobal.knowframework.security.common.vo.user.UserBriefVO;

/**
 * @author d06679
 * @date 2018/9/18
 */
public interface WorkOrderHandler extends BaseHandle {

    /**提交
     * 创建一个工单
     *
     * 1、校验工单内容是否合法
     * 2、构建工单请求
     * 3、提交工单
     *
     * @param workOrder 工单数据
     * @return result
     @throws AdminOperateException 管理操作Exception
     */
    Result<WorkOrderPO> submit(WorkOrder workOrder) throws AdminOperateException;

    /**过程一致
     * 处理工单
     * @param workOrder 工单内容
     * @return result
     @param approver 审批人
     @param opinion 意见
     @throws AdminOperateException 管理操作Exception
     */
    Result<Void> processAgree(WorkOrder workOrder, String approver, String opinion) throws AdminOperateException;

    /**过程不同意
     * 处理审核不同意的工单
     * @param processDTO 工单内容
     * @return result
     @param orderPO 订单订单
     */
    Result<Void> processDisagree(WorkOrderPO orderPO, WorkOrderProcessDTO processDTO);

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
    List<UserBriefVO> getApproverList(AbstractOrderDetail detail);

    /**检查机关
     * 是否审批人员
     * @param orderPO 订单信息, userName 审批人名称
     * @return Result
     @param userName 用户名
     */
    Result<Void> checkAuthority(WorkOrderPO orderPO, String userName);

}