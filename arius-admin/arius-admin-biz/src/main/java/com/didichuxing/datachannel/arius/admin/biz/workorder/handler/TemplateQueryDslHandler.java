package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.TemplateQueryDslOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.TemplateQueryDslContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.AuditDsls;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslInfo;
import com.didichuxing.datachannel.arius.admin.metadata.service.DslStatisService;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service("templateQueryDslHandler")
public class TemplateQueryDslHandler extends BaseWorkOrderHandler {

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;

    @Autowired
    private DslStatisService dslStatisService;

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
        TemplateQueryDslContent content = JSON.parseObject(extensions, TemplateQueryDslContent.class);

        return ConvertUtil.obj2Obj(content, TemplateQueryDslOrderDetail.class);
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

    /**
     * 验证用户提供的参数
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        TemplateQueryDslContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            TemplateQueryDslContent.class);

        if (AriusObjUtils.isNull(content.getId())) {
            return Result.buildParamIllegal("索引id为空");
        }

        if (AriusObjUtils.isNull(content.getName())) {
            return Result.buildParamIllegal("索引名字为空");
        }

        if (AriusObjUtils.isNull(content.getDsl())) {
            return Result.buildParamIllegal("DSL语句为空");
        }

        if (AriusObjUtils.isNull(content.getMemo())) {
            return Result.buildParamIllegal("DSL语句说明为空");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        TemplateQueryDslContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            TemplateQueryDslContent.class);

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
        TemplateQueryDslContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            TemplateQueryDslContent.class);

        List<ProjectTemplateAuth> projectTemplateAuths = projectLogicTemplateAuthService
            .getTemplateAuthsByLogicTemplateId(content.getId());
        Map<Integer, ProjectTemplateAuth> projectId2ProjectTemplateAuthMap = ConvertUtil.list2Map(projectTemplateAuths,
            ProjectTemplateAuth::getProjectId);

        if (projectId2ProjectTemplateAuthMap.containsKey(workOrder.getSubmitorProjectId())) {
            return Result.buildSucc();
        }

        return Result.buildParamIllegal("当前project无该索引访问访问权限，请先申请查询权限");
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
        TemplateQueryDslContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            TemplateQueryDslContent.class);

        DslInfo dslInfo = new DslInfo();
        dslInfo.setDsl(content.getDsl());
        dslInfo.setMemo(content.getMemo());

        List<DslInfo> dslInfos = new ArrayList<>();
        dslInfos.add(dslInfo);

        // 修改模板quota及保存时长信息
        AuditDsls auditDsls = new AuditDsls(workOrder.getSubmitorProjectId(), workOrder.getSubmitor(), dslInfos);
        Result<String> result = dslStatisService.auditDsl(auditDsls);

        return Result.buildFrom(result);
    }
}