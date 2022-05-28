package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.TemplateTransferContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template.TemplateOperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.TemplateTransferOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service("templateTransferHandler")
public class TemplateTransferHandler extends BaseWorkOrderHandler {

    @Autowired
    private IndexTemplateService indexTemplateService;

 

    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService    userService;

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
        return JSON.parseObject(extensions, TemplateTransferOrderDetail.class);
    }

    @Override
    public List<UserBriefVO> getApproverList(AbstractOrderDetail detail) {
        return getRDOrOPList();
    }

    @Override
    public Result<Void> checkAuthority(WorkOrderPO orderPO, String userName) {
        if (isRDOrOP(userName)) {
            return Result.buildSucc();
        }
        return Result.buildFail(ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
    }

    /**************************************** protected method ******************************************/

    /**
     * 验证用户提供的参数
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        TemplateTransferContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            TemplateTransferContent.class);

        if (AriusObjUtils.isNull(content.getId())) {
            return Result.buildParamIllegal("索引id为空");
        }

        if (AriusObjUtils.isNull(content.getName())) {
            return Result.buildParamIllegal("索引名字为空");
        }

        if (AriusObjUtils.isNull(content.getTgtProjectId())) {
            return Result.buildParamIllegal("应用id为空");
        }

        if (AriusObjUtils.isNull(content.getTgtResponsible())) {
            return Result.buildParamIllegal("责任人为空");
        }
        
        if (AriusObjUtils.isNull(userService.getUserBriefByUserName(content.getTgtResponsible()))) {
            return Result.buildParamIllegal("责任人非法");
        }

        if (AriusObjUtils.isNull(indexTemplateService.getLogicTemplateById(content.getId()))) {
            return Result.buildNotExist("索引不存在");
        }

        if (projectService.checkProjectExist(content.getTgtProjectId())) {
            return Result.buildNotExist("应用不存在");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        TemplateTransferContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            TemplateTransferContent.class);

        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getName() + workOrderTypeEnum.getMessage();
    }

    /**
     * 验证用户是否有该工单权限
     */
    @Override
    protected Result<Void> validateConsoleAuth(WorkOrder workOrder) {
        TemplateTransferContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            TemplateTransferContent.class);

        if (AriusObjUtils.isNull(content.getSourceProjectId())) {
            return Result.buildParamIllegal("原appId为空");
        }

        if (AriusObjUtils.isNull(content.getTgtProjectId())) {
            return Result.buildParamIllegal("目标appId为空");
        }

        if (content.getTgtProjectId().equals(content.getSourceProjectId())) {
            return Result.buildFail("无效转让, 原始项目Id和目标项目ID相同");
        }

        if (AuthConstant.SUPER_PROJECT_ID.equals(workOrder.getSubmitorProjectId())) {
            return Result.buildSucc();
        }

        IndexTemplate templateLogic = indexTemplateService.getLogicTemplateById(content.getId());
        if (!templateLogic.getProjectId().equals(workOrder.getSubmitorProjectId())) {
            return Result.buildOpForBidden("您无权对该索引进行转让操作");
        }

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
        TemplateTransferContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            TemplateTransferContent.class);

        Result<Void> result = indexTemplateService.turnOverLogicTemplate(content.getId(), content.getTgtProjectId(),
            content.getTgtResponsible(), workOrder.getSubmitor());

        if (result.success()) {
            operateRecordService.save(ModuleEnum.TEMPLATE, OperationEnum.EDIT, content.getId(), JSON.toJSONString(
                    new TemplateOperateRecord(TemplateOperateRecordEnum.TRANSFER.getCode(), "模板从 appId:" + content.getSourceProjectId() + "转移到 appId:" + content.getTgtProjectId())), approver);
        }

        return Result.buildFrom(result);
    }
}