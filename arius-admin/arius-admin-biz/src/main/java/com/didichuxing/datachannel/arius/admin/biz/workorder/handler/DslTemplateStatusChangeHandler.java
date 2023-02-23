package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.dsl.DslTemplateManager;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.DslTemplateStatusContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.DslTemplateStatusDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.knowframework.security.common.vo.user.UserBriefVO;

/**
 * @author wuxuan
 * @date 2022/11/14
 */
@Service("dslTemplateStatusChangeHandler")
public class DslTemplateStatusChangeHandler extends BaseWorkOrderHandler {

    @Autowired
    private DslTemplateManager dslTemplateManager;

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
        DslTemplateStatusContent content = JSON.parseObject(extensions, DslTemplateStatusContent.class);

        return ConvertUtil.obj2Obj(content, DslTemplateStatusDetail.class);
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
        DslTemplateStatusContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),DslTemplateStatusContent.class);

        if (AriusObjUtils.isNull(content.getProjectId())) {
            return Result.buildParamIllegal("项目id为空");
        }

        if (AriusObjUtils.isNull(content.getDslTemplateMd5())) {
            return Result.buildParamIllegal("dslTemplateMd5为空");
        }

        if (AriusObjUtils.isNull(content.getOperator())) {
            return Result.buildParamIllegal("操作者为空");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        DslTemplateStatusContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                DslTemplateStatusContent.class);

        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }

        return content.getDslTemplateMd5() + workOrderTypeEnum.getMessage();
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
        DslTemplateStatusContent dslTemplateStatusContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                DslTemplateStatusContent.class);

        Result<Boolean> result = dslTemplateManager.changeDslTemplateStatus(dslTemplateStatusContent.getProjectId(),
                dslTemplateStatusContent.getOperator(), dslTemplateStatusContent.getDslTemplateMd5());

        if (result.success()) {
            //操作记录
            //查询模板读写变更记录
            operateRecordService.save(new OperateRecord.Builder().operationTypeEnum(OperateTypeEnum.QUERY_TEMPLATE_STATUS_CHANGE)
                    .project(projectService.getProjectBriefByProjectId(workOrder.getSubmitorProjectId()))
                    .content(String.format("项目%d的查询模板%s启用/禁用状态变更", dslTemplateStatusContent.getProjectId(),dslTemplateStatusContent.getDslTemplateMd5()))
                    .userOperation(workOrder.getSubmitor())
                    .buildDefaultManualTrigger());
        }

        return Result.buildFrom(result);
    }

}
