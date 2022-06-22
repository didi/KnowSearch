package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.TemplateAuthContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.TemplateAuthOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author d06679
 * @date 2019/4/29
 */
@NoArgsConstructor
@Service("templateAuthHandler")
@Deprecated
public class TemplateAuthHandler extends BaseWorkOrderHandler {


    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;



    @Autowired
    private ProjectLogicTemplateAuthService logicTemplateAuthService;

    @Autowired
    private ProjectClusterLogicAuthService logicClusterAuthService;

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

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        TemplateAuthContent content = JSON.parseObject(extensions, TemplateAuthContent.class);
        return ConvertUtil.obj2Obj(content, TemplateAuthOrderDetail.class);
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

    /**************************************** protected method ****************************************************/

    /**
     * 验证用户提供的参数
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        TemplateAuthContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), TemplateAuthContent.class);

        if (AriusObjUtils.isNull(content.getId())) {
            return Result.buildParamIllegal("索引id为空");
        }

        if (AriusObjUtils.isNull(content.getName())) {
            return Result.buildParamIllegal("索引名字为空");
        }

        if (AriusObjUtils.isNull(content.getAuthCode())) {
            return Result.buildParamIllegal("权限类型为空");
        }

        ProjectTemplateAuthEnum authEnum = ProjectTemplateAuthEnum.valueOf(content.getAuthCode());
        if (ProjectTemplateAuthEnum.NO_PERMISSION.equals(authEnum)) {
            return Result.buildParamIllegal("权限类型非法");
        }

        if (authEnum.equals(ProjectTemplateAuthEnum.OWN)
                && AriusObjUtils.isNull(content.getResponsible())) {
            return Result.buildParamIllegal("管理责任人为空");
        }

        List<ProjectTemplateAuth> auths = projectLogicTemplateAuthService.getTemplateAuthsByProjectId(workOrder.getSubmitorProjectId());
        Map<Integer, ProjectTemplateAuth> logicId2AppTemplateAuthMap = ConvertUtil.list2Map(auths,
            ProjectTemplateAuth::getTemplateId);
        ProjectTemplateAuth templateAuth = logicId2AppTemplateAuthMap.get(content.getId());
        if (templateAuth != null && templateAuth.getType() <= content.getAuthCode()) {
            return Result.buildParamIllegal("您已经拥有该权限");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        TemplateAuthContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), TemplateAuthContent.class);

        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getName() + workOrderTypeEnum.getMessage();
    }

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
        return Result.buildSucc();
    }
}