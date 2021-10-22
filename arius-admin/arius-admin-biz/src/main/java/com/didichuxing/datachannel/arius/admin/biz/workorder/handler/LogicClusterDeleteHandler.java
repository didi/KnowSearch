package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterDeleteContent;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterDeleteOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;

/**
 * Created by linyunan on 2021-06-11
 */
@Service
public class LogicClusterDeleteHandler extends BaseWorkOrderHandler {

	@Autowired
	private ESClusterLogicService esClusterLogicService;

	@Override
	protected Result validateConsoleParam(WorkOrder workOrder) {
		return Result.buildSucc();
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
	protected Result validateConsoleAuth(WorkOrder workOrder) {
		return Result.buildSucc();
	}

	@Override
	protected Result validateParam(WorkOrder workOrder)  {
		LogicClusterDeleteContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
				LogicClusterDeleteContent.class);

		ESLogicClusterDTO resourceLogicDTO = ConvertUtil.obj2Obj(content, ESLogicClusterDTO.class);
		resourceLogicDTO.setAppId(workOrder.getSubmitorAppid());
		resourceLogicDTO.setDataCenter(EnvUtil.getDC().getCode());
		return esClusterLogicService.validateLogicClusterParams(resourceLogicDTO, OperationEnum.CHECK);
	}

    @Override
    protected Result doProcessAgree(WorkOrder workOrder, String approver) {
        List<String> administrators = getOPList().stream().map(AriusUserInfo::getName).collect(Collectors.toList());
        return Result.buildSucc(
            String.format("请联系管理员【%s】进行后续操作", administrators.get(new Random().nextInt(administrators.size()))));
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
	public Result checkAuthority(WorkOrderPO orderPO, String userName) {
		if (isOP(userName)) {
			return Result.buildSucc(true);
		}
		return Result.buildFail(ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
	}
}
