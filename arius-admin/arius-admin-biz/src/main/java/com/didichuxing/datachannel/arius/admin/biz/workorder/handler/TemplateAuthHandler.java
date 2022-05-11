package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.TemplateAuthContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.TemplateAuthOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author d06679
 * @date 2019/4/29
 */
@NoArgsConstructor
@Service("templateAuthHandler")
public class TemplateAuthHandler extends BaseWorkOrderHandler {

    @Value("${admin.url.console}")
    private String                                      adminUrlConsole;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private AppLogicTemplateAuthService                 appLogicTemplateAuthService;

    @Autowired
    private TemplateLabelService                        templateLabelService;

    @Autowired
    private AppLogicTemplateAuthService                 logicTemplateAuthService;

    @Autowired
    private AppClusterLogicAuthService                  logicClusterAuthService;

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
    public List<AriusUserInfo> getApproverList(AbstractOrderDetail detail) {
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

        AppTemplateAuthEnum authEnum = AppTemplateAuthEnum.valueOf(content.getAuthCode());
        if (AppTemplateAuthEnum.NO_PERMISSION.equals(authEnum)) {
            return Result.buildParamIllegal("权限类型非法");
        }

        if (authEnum.equals(AppTemplateAuthEnum.OWN)
                && AriusObjUtils.isNull(content.getResponsible())) {
            return Result.buildParamIllegal("管理责任人为空");
        }

        List<AppTemplateAuth> auths = appLogicTemplateAuthService.getTemplateAuthsByAppId(workOrder.getSubmitorAppid());
        Map<Integer, AppTemplateAuth> logicId2AppTemplateAuthMap = ConvertUtil.list2Map(auths,
            AppTemplateAuth::getTemplateId);
        AppTemplateAuth templateAuth = logicId2AppTemplateAuthMap.get(content.getId());
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
        TemplateAuthContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), TemplateAuthContent.class);
        Integer logicTemplateId = content.getId();

        AppTemplateAuthEnum authEnum = AppTemplateAuthEnum.valueOf(content.getAuthCode());

        if (authEnum.equals(AppTemplateAuthEnum.OWN)) {
            // 逻辑模板移交
            return Result.buildFrom(indexTemplateService.turnOverLogicTemplate(logicTemplateId, workOrder.getSubmitorAppid(),
                    content.getResponsible(), workOrder.getSubmitor()));
        } else {
            // 对于索引的工单任务，若没有集群权限，则添加所在集群的访问权限
            // 获取所在集群
            ClusterLogic clusterLogic = indexTemplateService
                .getLogicTemplateWithClusterAndMasterTemplate(logicTemplateId).getLogicCluster();

            if (clusterLogic == null) {
                // 不应该走到这一步，防御编程
                return Result.buildFail(String.format("找不到模板%s所属的逻辑集群", logicTemplateId));
            }
            AppClusterLogicAuthEnum logicClusterAuthEnum = logicClusterAuthService
                .getLogicClusterAuthEnum(workOrder.getSubmitorAppid(), clusterLogic.getId());

            // 没有集群权限则添加访问权限
            if (logicClusterAuthEnum == AppClusterLogicAuthEnum.NO_PERMISSIONS) {
                logicClusterAuthService.ensureSetLogicClusterAuth(workOrder.getSubmitorAppid(), clusterLogic
								.getId(),
                    AppClusterLogicAuthEnum.ACCESS, workOrder.getSubmitor(), workOrder.getSubmitor());
            }

            // 逻辑模板权限设置
            Result<Void> result = logicTemplateAuthService.ensureSetLogicTemplateAuth(workOrder.getSubmitorAppid(),
                logicTemplateId, authEnum, workOrder.getSubmitor(), workOrder.getSubmitor());

            return Result.buildFrom(result);
        }
    }
}
