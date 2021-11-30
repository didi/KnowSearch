package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterDeleteContent;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterDeleteOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * Created by linyunan on 2021-06-11
 */
@Service
public class LogicClusterDeleteHandler extends BaseWorkOrderHandler {

    protected static final ILog        LOGGER = LogFactory.getLog(LogicClusterDeleteHandler.class);

    @Autowired
    private ClusterLogicService        clusterLogicService;

    @Autowired
    private ClusterLogicManager        clusterLogicManager;

    @Autowired
    private ClusterContextManager      clusterContextManager;

    @Autowired
    private AppClusterLogicAuthService appClusterLogicAuthService;

	@Override
	protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
		LogicClusterDeleteContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
				LogicClusterDeleteContent.class);

		if (Boolean.FALSE.equals(clusterLogicService.isClusterLogicExists(content.getId()))) {
			return Result.buildFail(String.format("逻辑集群[%s]不存在", content.getName()));
		}

		if (clusterLogicService.hasLogicClusterWithTemplates(content.getId())) {
			return Result.buildFail(String.format("逻辑集群[%s]存在模板", content.getName()));
		}

		ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(content.getId());
		if (CollectionUtils.isNotEmpty(clusterLogicContext.getAssociatedClusterPhyNames())) {
			return Result.buildFail(String.format("逻辑集群[%s]和物理集群[%s]关联", content.getName(),
					ListUtils.strList2String(clusterLogicContext.getAssociatedClusterPhyNames())));
		}

		ESLogicClusterDTO resourceLogicDTO = ConvertUtil.obj2Obj(content, ESLogicClusterDTO.class);
		resourceLogicDTO.setAppId(workOrder.getSubmitorAppid());
		resourceLogicDTO.setDataCenter(EnvUtil.getDC().getCode());
		return clusterLogicService.validateClusterLogicParams(resourceLogicDTO, OperationEnum.CHECK);
	}

	@Override
	protected String getTitle(WorkOrder workOrder) {
		LogicClusterDeleteContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
				LogicClusterDeleteContent.class);

		WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
		if (workOrderTypeEnum == null) {
			return "";
		}
		return content.getName() + workOrderTypeEnum.getMessage();
	}

	@Override
	protected Result<Void> validateConsoleAuth(WorkOrder workOrder) {
		if (!isOP(workOrder.getSubmitor())) {
			return Result.buildOpForBidden("非运维人员不能操作集群扩缩容！");
		}

		return Result.buildSucc();
	}

	@Override
	protected Result<Void> validateParam(WorkOrder workOrder)  {
        return Result.buildSucc();
	}

    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) {
        LogicClusterDeleteContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterDeleteContent.class);
        try {
            Result<Void> deleteLogicClusterResult = clusterLogicManager.deleteLogicCluster(content.getId(), workOrder.getSubmitor(), workOrder.getSubmitorAppid());
            if (deleteLogicClusterResult.success()){
                appClusterLogicAuthService.deleteLogicClusterAuthByLogicClusterId(content.getId());
            }
        } catch (AdminOperateException e) {
            LOGGER.error("class=LogicClusterDeleteHandler||method=doProcessAgree||clusterLogicId={}||errMsg={}",
                content.getId(), e.getMessage(), e);
        }
        return Result.buildSucc();
    }

	@Override
	public boolean canAutoReview(WorkOrder workOrder) {
		return false;
	}

	@Override
	public AbstractOrderDetail getOrderDetail(String extensions) {
		return ConvertUtil.obj2Obj(extensions, LogicClusterDeleteOrderDetail.class);
	}

	@Override
	public List<AriusUserInfo> getApproverList(AbstractOrderDetail detail) {
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
