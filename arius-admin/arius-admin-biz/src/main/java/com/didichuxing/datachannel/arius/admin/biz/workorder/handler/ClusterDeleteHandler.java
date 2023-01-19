package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.ClusterDeleteContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.ClusterDeleteOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.common.vo.user.UserBriefVO;

/**
 * @author linyunan
 * @date 2021-06-11
 */
@Service
@Deprecated
public class ClusterDeleteHandler extends BaseWorkOrderHandler {
    protected static final ILog     LOGGER = LogFactory.getLog(ClusterDeleteHandler.class);

    @Autowired
    private ClusterContextManager   clusterContextManager;

    @Autowired
    private ClusterPhyManager       clusterPhyManager;
    @Autowired
    private ClusterLogicManager clusterLogicManager;

   

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        ClusterDeleteContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterDeleteContent.class);
        if (!clusterPhyManager.isClusterExists(content.getPhyClusterName())) {
            return Result.buildFail(String.format("物理集群[%s]不存在", content.getPhyClusterName()));
        }
        
        List<String> clusterLogicIdList = clusterLogicManager.getClusterPhyAssociatedClusterLogicNames(content.getPhyClusterName());
        
        if (CollectionUtils.isNotEmpty(clusterLogicIdList)) {
            return Result.buildFail(String.format("物理集群[%s]和逻辑集群[%s]关联", content.getPhyClusterName(),
                ListUtils.strList2String(clusterLogicIdList)));
        }

        List<String> templatePhyNameList = indexTemplatePhyService
            .getNormalTemplateByCluster(content.getPhyClusterName()).stream().map(IndexTemplatePhy::getName)
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(templatePhyNameList)) {
            return Result.buildFail(String.format("物理集群[%s]中已经存在模板[%s]", content.getPhyClusterName(),
                ListUtils.strList2String(templatePhyNameList)));
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        ClusterDeleteContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterDeleteContent.class);
        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getPhyClusterName() + workOrderTypeEnum.getMessage();
    }

    @Override
    protected Result<Void> validateConsoleAuth(WorkOrder workOrder) {
        if (!isOP(workOrder.getSubmitor())) {
            return Result.buildOpForBidden("非运维人员不能操作集群扩缩容！");
        }

        return Result.buildSucc();
    }

    @Override
    protected Result<Void> validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) {
        ClusterDeleteContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterDeleteContent.class);
        final Result<ClusterPhy> clusterPhyResult = clusterPhyManager.getClusterByName(content.getPhyClusterName());
        if (null == clusterPhyResult.getData()) {
            return Result.buildFail(String.format("物理集群[%s]不存在", content.getPhyClusterName()));
        }

        Result<Boolean> deleteClusterResult = clusterPhyManager.deleteCluster(clusterPhyResult.getData().getId(),
            workOrder.getSubmitor(), AuthConstant.SUPER_PROJECT_ID);
        if (deleteClusterResult.failed()) {
            return Result.buildFail(deleteClusterResult.getMessage());
        }

        return Result.buildSucc();
    }

    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        return ConvertUtil.obj2Obj(extensions, ClusterDeleteOrderDetail.class);
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
}