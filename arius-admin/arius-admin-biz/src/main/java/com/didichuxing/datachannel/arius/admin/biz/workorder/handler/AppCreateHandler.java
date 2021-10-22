package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AppCreateOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.AppCreateContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.notify.AppCreateNotify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum.WORK_ORDER_APP_CREATE;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service("appCreateHandler")
public class AppCreateHandler extends BaseWorkOrderHandler {

    @Autowired
    private AppService appService;

    /**
     * 验证用户提供的参数
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result validateConsoleParam(WorkOrder workOrder) {
        AppDTO appDTO = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), AppDTO.class);
        return appService.validateApp(appDTO, OperationEnum.ADD);
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        AppDTO appDTO = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), AppDTO.class);

        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }

        return appDTO.getName() + workOrderTypeEnum.getMessage();
    }

    /**
     * 验证用户是否有该工单权限
     *
     * @param workOrder 工单内容
     * @return result
     */
    @Override
    protected Result validateConsoleAuth(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    /**
     * 工单是否自动审批
     *
     * @param workOrder 工单类型
     * @return result
     */
    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return true;
    }

    /**
     * 验证平台参数
     *
     * @param workOrder 工单内容
     * @return result
     */
    @Override
    protected Result validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    /**
     * 处理工单
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result doProcessAgree(WorkOrder workOrder, String approver) {
        AppCreateContent appCreateContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            AppCreateContent.class);
        AppDTO appDTO = new AppDTO();

        appDTO.setName(appCreateContent.getName());
        appDTO.setDepartment(appCreateContent.getDepartment());
        appDTO.setDepartmentId(appCreateContent.getDepartmentId());
        appDTO.setMemo(appCreateContent.getMemo());
        appDTO.setResponsible(appCreateContent.getResponsible());

        Result<Integer> result = appService.registerApp(appDTO, workOrder.getSubmitor());

        if (result.success()) {
            Integer appId = result.getData();
            App app = appService.getAppById(appId);
            sendNotify(WORK_ORDER_APP_CREATE, new AppCreateNotify(result.getData(), app.getName(), app.getVerifyCode()),
                Arrays.asList(workOrder.getSubmitor()));
        }

        return result;
    }

    @Override
    public List<AriusUserInfo> getApproverList(AbstractOrderDetail detail) {
        return getRDOrOPList();
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        AppDTO orderExtensionDTO = null;
        try {
            orderExtensionDTO = JSONObject.parseObject(extensions, AppDTO.class);
        } catch (Exception e) {
        }

        AppCreateOrderDetail orderDetailDTO = new AppCreateOrderDetail();
        orderDetailDTO.setName(orderExtensionDTO.getName());
        orderDetailDTO.setResponsible(orderExtensionDTO.getResponsible());
        orderDetailDTO.setDepartment(orderExtensionDTO.getDepartment());
        orderDetailDTO.setDepartmentId(orderExtensionDTO.getDepartmentId());
        App app = appService.getAppByName(orderExtensionDTO.getName());
        if (ValidateUtils.isNull(app)) {
            return orderDetailDTO;
        }

        orderDetailDTO.setAppId(app.getId());
        return orderDetailDTO;
    }

    @Override
    public Result checkAuthority(WorkOrderPO orderPO, String userName) {
        if (isRDOrOP(userName)) {
            return Result.buildSucc(true);
        }
        return Result.buildFail(ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
    }
}
