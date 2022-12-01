package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.TemplateLogicStatusContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.TemplateLogicStatusDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wuxuan
 * @date 2022/11/14
 */
@Service("templateLogicBlockWriteHandler")
public class TemplateLogicBlockWriteHandler extends BaseWorkOrderHandler {

    @Autowired
    private TemplateLogicManager templateLogicManager;

    /**
     * 工单是否自动审批
     *
     * @param workOrder 工单类型
     * @return result
     */
    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        TemplateLogicStatusContent content = JSON.parseObject(extensions, TemplateLogicStatusContent.class);

        return ConvertUtil.obj2Obj(content, TemplateLogicStatusDetail.class);
    }

    @Override
    public List<UserBriefVO> getApproverList(AbstractOrderDetail detail) {
        return getOPList();
    }

    @Override
    public Result<Void> checkAuthority(WorkOrderPO orderPO, String userName) {
        if (isOP(userName)) {
            return Result.buildSucc();
        }
        return Result.buildFail(ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
    }

    /**
     * 验证用户提供的参数
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        TemplateLogicStatusContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),TemplateLogicStatusContent.class);

        if (AriusObjUtils.isNull(content.getProjectId())) {
            return Result.buildParamIllegal("项目id为空");
        }

        if (AriusObjUtils.isNull(content.getTemplateId())) {
            return Result.buildParamIllegal("模板Id为空");
        }

        if (AriusObjUtils.isNull(content.getOperator())) {
            return Result.buildParamIllegal("操作者为空");
        }

        if (AriusObjUtils.isNull(content.getStatus())) {
            return Result.buildParamIllegal("禁用/启用写的状态为空");
        }

        if (AriusObjUtils.isNull(content.getName())) {
            return Result.buildParamIllegal("索引模板名称为空");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        TemplateLogicStatusContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                TemplateLogicStatusContent.class);

        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }

        return content.getName() + workOrderTypeEnum.getMessage();
    }


    /**************************************** protected method ******************************************/

    /**
     * 验证用户是否有该工单权限
     *
     * @param workOrder 工单内容
     * @return result
     */
    @Override
    protected Result<Void> validateConsoleAuth(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    /**
     * 验证平台参数
     *
     * @param workOrder 工单内容
     * @return result
     */
    @Override
    protected Result<Void> validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    /**
     * 处理工单
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        TemplateLogicStatusContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                TemplateLogicStatusContent.class);
        Result<Void> result = templateLogicManager.blockWrite(content.getTemplateId(),
                content.getStatus(),content.getOperator(),content.getProjectId());

        if (result.success()) {
            //操作记录
            //查询模板读写变更记录
            operateRecordService.save(new OperateRecord.Builder().operationTypeEnum(OperateTypeEnum.TEMPLATE_MANAGEMENT_BLOCK_WRITE)
                    .project(projectService.getProjectBriefByProjectId(workOrder.getSubmitorProjectId()))
                    .content(String.format("项目%d的模板%s启用/禁用写状态变更", content.getProjectId(),content.getTemplateId()))
                    .userOperation(workOrder.getSubmitor())
                    .buildDefaultManualTrigger());
        }

        return Result.buildFrom(result);
    }

}
