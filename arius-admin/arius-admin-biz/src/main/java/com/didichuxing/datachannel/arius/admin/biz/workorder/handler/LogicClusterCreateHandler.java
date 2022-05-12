package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterCreateContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterCreateOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service("logicClusterCreateHandler")
public class LogicClusterCreateHandler extends BaseWorkOrderHandler {

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Autowired
    private ClusterLogicManager clusterLogicManager;

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        LogicClusterCreateContent content = JSON.parseObject(extensions, LogicClusterCreateContent.class);

        return ConvertUtil.obj2Obj(content, LogicClusterCreateOrderDetail.class);
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

    /**************************************** protected method ******************************************/

    /**
     * 验证用户提供的参数
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        LogicClusterCreateContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterCreateContent.class);

        ESLogicClusterDTO resourceLogicDTO = ConvertUtil.obj2Obj(content, ESLogicClusterDTO.class);
        resourceLogicDTO.setAppId(workOrder.getSubmitorAppid());
        resourceLogicDTO.setType(ResourceLogicTypeEnum.PRIVATE.getCode());
        return clusterLogicService.validateClusterLogicParams(resourceLogicDTO, OperationEnum.ADD);
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        LogicClusterCreateContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterCreateContent.class);

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
     * 处理工单 这里分为两种，一种是分配逻辑集群，一种是申请逻辑集群，其中申请逻辑集群不会绑定region，会联系同学在运维侧进行操作
     */
    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) {
        ESLogicClusterWithRegionDTO esLogicClusterWithRegionDTO = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ESLogicClusterWithRegionDTO.class);
        esLogicClusterWithRegionDTO.setAppId(workOrder.getSubmitorAppid());

        // 创建逻辑集群并且批量绑定指定的region,默认是能成功
        Result<Void> result = Result.buildSucc();
        if(!CollectionUtils.isEmpty(esLogicClusterWithRegionDTO.getClusterRegionDTOS())) {
            result = clusterLogicManager.addLogicClusterAndClusterRegions(esLogicClusterWithRegionDTO,approver);
        }

        if (result.success()) {
            List<String> administrators = getOPList().stream().map(AriusUserInfo::getName).collect(Collectors.toList());
            return Result.buildSuccWithMsg(
                String.format("请联系管理员【%s】进行后续操作", administrators.get(new Random().nextInt(administrators.size()))));
        }

        return Result.buildFrom(result);
    }

    @Override
    protected Result validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }
}