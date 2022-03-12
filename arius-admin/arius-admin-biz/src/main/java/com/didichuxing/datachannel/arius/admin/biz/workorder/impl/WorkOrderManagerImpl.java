package com.didichuxing.datachannel.arius.admin.biz.workorder.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.workorder.WorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.WorkOrderManager;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.PhyClusterPluginOperationContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.utils.WorkOrderTaskConverter;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host.HostsParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.workorder.WorkOrderDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.workorder.WorkOrderProcessDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.order.OrderTypeVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.order.WorkOrderSubmittedVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.order.WorkOrderVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.order.detail.OrderDetailBaseVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.user.AriusUserInfoVO;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.OrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.order.OrderStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.task.EcmTaskDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.workorder.WorkOrderDAO;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.client.constant.workorder.BpmAuditTypeEnum.AGREE;
import static com.didichuxing.datachannel.arius.admin.client.constant.workorder.BpmAuditTypeEnum.DISAGREE;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service
public class WorkOrderManagerImpl implements WorkOrderManager {

    private static final Logger  LOGGER           = LoggerFactory.getLogger(WorkOrderManagerImpl.class);

    private static final int     APP_ID_TO_CREATE = -99;

    @Autowired
    private HandleFactory        handleFactory;

    @Autowired
    private AppService           appService;

    @Autowired
    private WorkOrderDAO         orderDao;

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    @Autowired
    private EcmTaskDAO           ecmTaskDao;

    @Autowired
    private ClusterPhyService    esClusterPhyService;

    @Override
    public Result<List<OrderTypeVO>> getOrderTypes() {
        List<OrderTypeVO> orderTypeVOS = new ArrayList<>();

        for (WorkOrderTypeEnum elem : WorkOrderTypeEnum.values()) {
            if (WorkOrderTypeEnum.UNKNOWN.getName().equals(elem.getName())) {
                continue;
            }

            orderTypeVOS.add(new OrderTypeVO(elem.getName(), elem.getMessage()));
        }

        return Result.buildSucc(orderTypeVOS);
    }

    @Override
    public Result<WorkOrderSubmittedVO> submit(WorkOrderDTO workOrderDTO) throws AdminOperateException {

        String workOrderJSONString = JSON.toJSONString(workOrderDTO);
        LOGGER.info("class=WorkOrderManagerImpl||method=WorkOrderController.process||workOrderDTO={}||envInfo={}||dataCenter={}",
            workOrderJSONString, EnvUtil.getStr(), workOrderDTO.getDataCenter());

        initWorkOrderDTO(workOrderDTO);

        Result<Void> submitValidResult = checkSubmitValid(workOrderDTO);
        if (submitValidResult.failed()) {
            return Result.buildFrom(submitValidResult);
        }

        WorkOrderHandler handler = (WorkOrderHandler) handleFactory.getByHandlerNamePer(workOrderDTO.getType());

        Result<WorkOrderPO> submitResult = handler.submit(ConvertUtil.obj2Obj(workOrderDTO, WorkOrder.class));
        if (submitResult.failed()) {
            return Result.buildFail(submitResult.getMessage());
        }

        return Result.buildSucc(convert2WorkOrderSubmittedVO(submitResult.getData()));
    }

    @Override
    public Result<Void> process(WorkOrderProcessDTO processDTO) {
        Result<Void> checkProcessResult = checkProcessValid(processDTO);
        if (checkProcessResult.failed()) {
            return checkProcessResult;
        }

        WorkOrderPO orderPO = orderDao.getById(processDTO.getOrderId());
        if (AriusObjUtils.isNull(orderPO)) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }

        return doProcessByWorkOrderHandle(orderPO, processDTO);
    }

    @Override
    public int insert(WorkOrderPO orderPO) {
        try {
            if (orderPO.getApproverAppId() == null) {
                orderPO.setApproverAppId(AdminConstant.DEFAULT_APP_ID);
            }
            orderDao.insert(orderPO);
            return orderPO.getId().intValue();
        } catch (Exception e) {
            LOGGER.error("class=WorkOrderManagerImpl||method=insert||orderPO={}||msg=add order failed!", orderPO, e);
        }
        return 0;
    }

    @Override
    public int updateOrderById(WorkOrderPO orderPO) {
        try {
            return orderDao.update(orderPO);
        } catch (Exception e) {
            LOGGER.error("class=WorkOrderManagerImpl||method=updateOrderById||orderPO={}||msg=update order failed!",
                orderPO, e);
        }
        return 0;
    }

    @Override
    public Result<OrderDetailBaseVO> getById(Long id) {
        try {
            WorkOrderPO orderPO = orderDao.getById(id);
            if (AriusObjUtils.isNull(orderPO)) {
                return Result.buildFail(ResultType.NOT_EXIST.getMessage());
            }

            return Result.buildSucc(convert2DetailBaseVO(getBaseDetail(orderPO)));

        } catch (Exception e) {
            LOGGER.error("class=WorkOrderManagerImpl||method=getById||id={}||msg=get order failed!", id, e);
        }
        return Result.buildFail();
    }

    @Override
    public List<WorkOrderPO> list() {
        try {
            return orderDao.list();
        } catch (Exception e) {
            LOGGER.error("class=WorkOrderManagerImpl||method=list||msg=get all order failed!", e);
        }
        return new ArrayList<>();
    }

    @Override
    public Result<Void> cancelOrder(Long id, String userName) {
        try {
            WorkOrderPO orderPO = orderDao.getById(id);
            if (AriusObjUtils.isNull(orderPO)) {
                return Result.buildFail(ResultType.NOT_EXIST.getMessage());
            }

            if (!userName.equals(orderPO.getApplicant())) {
                return Result.buildFail(ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
            }

            if (orderDao.updateOrderStatusById(id, OrderStatusEnum.CANCELLED.getCode()) > 0) {
                return Result.buildSucc();
            }

        } catch (Exception e) {
            LOGGER.error("class=WorkOrderManagerImpl||method=cancelOrder||id={}||msg=cancel order failed!", id, e);
            return Result.buildFail(ResultType.FAIL.getMessage());
        }
        return Result.buildFail(ResultType.FAIL.getMessage());
    }

    @Override
    public Result<Void> processOrder(WorkOrderPO order) {
        try {
            WorkOrderPO orderPO = orderDao.getById(order.getId());
            if (AriusObjUtils.isNull(orderPO)) {
                return Result.buildFail(ResultType.NOT_EXIST.getMessage());
            }
            if (orderDao.update(order) > 0) {
                return Result.buildSucc();
            }
        } catch (Exception e) {
            LOGGER.error("class=WorkOrderManagerImpl||method=processOrder||id={}||msg=cancel order failed!",
                order.getId(), e);
            return Result.buildFail(ResultType.FAIL.getMessage());
        }
        return Result.buildFail(ResultType.FAIL.getMessage());
    }

    @Override
    public Result<List<WorkOrderVO>> getOrderApplyList(String applicant, Integer status) {
        List<WorkOrderVO> orderDOList = Lists.newArrayList();
        try {
            orderDOList = ConvertUtil.list2List(orderDao.listByApplicantAndStatus(applicant, status),
                WorkOrderVO.class);
        } catch (Exception e) {
            LOGGER.error(
                "class=WorkOrderManagerImpl||method=getOrderApplyList||applicant={}||status={}||msg=get apply order failed!",
                applicant, status, e);
        }
        return Result.buildSucc(orderDOList);
    }

    @Override
    public List<WorkOrderPO> getApprovalList(String approver) {
        try {
            if (!ariusUserInfoService.isOPByDomainAccount(approver)) {
                return orderDao.listByApproverAndStatus(approver, null);
            }
            return orderDao.list();
        } catch (Exception e) {
            LOGGER.error(
                "class=WorkOrderManagerImpl||method=getApprovalList||approver={}||msg=get approval order failed!",
                approver, e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<WorkOrderPO> getPassApprovalList(String approver) {
        try {
            if (!ariusUserInfoService.isOPByDomainAccount(approver)) {
                return orderDao.listByApproverAndStatus(approver, OrderStatusEnum.PASSED.getCode());
            }
            return orderDao.listByStatus(OrderStatusEnum.PASSED.getCode());
        } catch (Exception e) {
            LOGGER.error(
                "class=WorkOrderManagerImpl||method=getPassApprovalList||approver={}||msg=get approval order list failed!",
                approver, e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<WorkOrderPO> getWaitApprovalList(String userName) {
        List<WorkOrderPO> orderList = new ArrayList<>();
        try {
            orderList = orderDao.listByStatus(OrderStatusEnum.WAIT_DEAL.getCode());
        } catch (Exception e) {
            LOGGER.error(
                "class=WorkOrderManagerImpl||method=getWaitApprovalList||userName={}||msg=get wait order list failed!",
                userName, e);
        }

        if (ariusUserInfoService.isOPByDomainAccount(userName) || ariusUserInfoService.isRDByDomainAccount(userName)) {
            return orderList;
        } else {
            orderList = handleOrderList(userName, orderList);
        }
        return orderList;
    }

    @Override
    public OrderDetail getBaseDetail(WorkOrderPO orderPO) {
        if (AriusObjUtils.isNull(orderPO)) {
            return null;
        }

        OrderDetail detailBaseDTO = new OrderDetail();
        detailBaseDTO.setDescription(orderPO.getDescription());
        detailBaseDTO.setCreateTime(orderPO.getCreateTime());
        detailBaseDTO.setFinishTime(orderPO.getFinishTime());
        detailBaseDTO.setId(orderPO.getId());
        detailBaseDTO.setOpinion(orderPO.getOpinion());
        detailBaseDTO.setStatus(orderPO.getStatus());
        detailBaseDTO.setType(orderPO.getType());
        detailBaseDTO.setTitle(orderPO.getTitle());
        detailBaseDTO.setApplicantAppId(orderPO.getApplicantAppId());
        detailBaseDTO.setAppDeptName("");

        WorkOrderTypeEnum typeEnum = WorkOrderTypeEnum.valueOfName(orderPO.getType());
        if (WorkOrderTypeEnum.UNKNOWN.equals(typeEnum)) {
            return null;
        }

        WorkOrderHandler handler = (WorkOrderHandler) handleFactory.getByHandlerNamePer(orderPO.getType());
        // 获取具体工单的详情
        try {
            detailBaseDTO.setDetail(handler.getOrderDetail(orderPO.getExtensions()));
            detailBaseDTO.setApproverList(handler.getApproverList(detailBaseDTO.getDetail()));
        } catch (Exception e) {
            LOGGER.error(
                "class=WorkOrderManagerImpl||method=getBaseDetail||extensions={}||msg=get order detail failed!",
                orderPO.getExtensions(), e);
        }
        detailBaseDTO.setApplicant(ariusUserInfoService.getByDomainAccount(orderPO.getApplicant()));
        return detailBaseDTO;
    }

    @Override
    public Result<List<WorkOrderVO>> getOrderApprovalListByStatus(Integer status) {
        List<WorkOrderPO> orderDOList = new ArrayList<>();

        String userName = SpringTool.getUserName();

        if (AriusObjUtils.isNull(status)) {
            orderDOList = getApprovalList(userName);

        } else if (OrderStatusEnum.WAIT_DEAL.getCode().equals(status)) {
            orderDOList = getWaitApprovalList(userName);

        } else if (OrderStatusEnum.PASSED.getCode().equals(status)) {
            orderDOList = getPassApprovalList(userName);
        }

        return Result.buildSucc(ConvertUtil.list2List(orderDOList, WorkOrderVO.class));
    }

    @Override
    public Result<String> getClusterTaskInfo(String cluster) {
        if (AriusObjUtils.isBlack(cluster)) {
            return Result.buildFail("cluster name 为空");
        }

        ClusterPhy clusterPhy = esClusterPhyService.getClusterByName(cluster);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail("不存在cluster name=" + cluster + "的物理集群");
        }

        EcmTask task = ConvertUtil.obj2Obj(ecmTaskDao.getUsefulWorkOrderTaskByClusterId(clusterPhy.getId()),
                EcmTask.class);
        if (AriusObjUtils.isNull(task)) {
            return Result.buildFail("当前集群没有待执行的工单任务");
        }

        List<EcmParamBase> ecmParamBases = WorkOrderTaskConverter.convert2EcmParamBaseList(task);
        if(CollectionUtils.isEmpty(ecmParamBases)) {
            return Result.buildFail("当前任务没有工单数据");
        }
        HostsParamBase hostsParamBase = (HostsParamBase) ecmParamBases.get(0);
        if(AriusObjUtils.isNull(hostsParamBase.getEsPluginAction())) {
            return Result.buildFail("当前没有集群插件操作的工单任务");
        }

        OrderDetailBaseVO orderDetailBaseVO = getById(task.getWorkOrderId()).getData();
        PhyClusterPluginOperationContent content = ConvertUtil.str2ObjByJson(orderDetailBaseVO.getDetail(),
                PhyClusterPluginOperationContent.class);

        return Result.build(Boolean.TRUE, JSON.toJSONString(content));
    }

    /*****************************************private*****************************************************/

    private Result<Void> checkSubmitValid(WorkOrderDTO workOrderDTO) {

        if (AriusObjUtils.isNull(workOrderDTO.getType())) {
            return Result.buildParamIllegal("工单类型为空");
        }

        if (AriusObjUtils.isNull(workOrderDTO.getContentObj())) {
            return Result.buildParamIllegal("工单内容为空");
        }

        WorkOrderTypeEnum typeEnum = WorkOrderTypeEnum.valueOfName(workOrderDTO.getType());
        if (WorkOrderTypeEnum.UNKNOWN.equals(typeEnum)) {
            return Result.buildNotExist("工单类型不存在");
        }

        if (AriusObjUtils.isNull(ariusUserInfoService.getByDomainAccount(workOrderDTO.getSubmitor()))) {
            return Result.buildParamIllegal("提交人非法");
        }

        if (!WorkOrderTypeEnum.APP_CREATE.getName().equals(workOrderDTO.getType())) {
            if (AriusObjUtils.isNull(workOrderDTO.getSubmitorAppid())) {
                return Result.buildParamIllegal("提交APPID为空");
            }
            if (APP_ID_TO_CREATE != workOrderDTO.getSubmitorAppid()
                && !appService.isAppExists(workOrderDTO.getSubmitorAppid())) {
                return Result.buildNotExist("提交appId不存在");
            }
        }

        return Result.buildSucc();
    }

    private void initWorkOrderDTO(WorkOrderDTO workOrderDTO) {
        workOrderDTO.setSubmitor(SpringTool.getUserName());
    }

    private WorkOrderSubmittedVO convert2WorkOrderSubmittedVO(WorkOrderPO workOrderPO) {
        if (AriusObjUtils.isNull(workOrderPO)) {
            return null;
        }
        WorkOrderSubmittedVO vo = new WorkOrderSubmittedVO();
        vo.setId(workOrderPO.getId());
        vo.setTitle(workOrderPO.getTitle());
        return vo;
    }

    private Result<Void> checkProcessValid(WorkOrderProcessDTO processDTO) {
        if (AriusObjUtils.isNull(processDTO)) {
            return Result.buildParamIllegal("处理工单不存在");
        }

        if (AriusObjUtils.isNull(processDTO.getOrderId())) {
            return Result.buildParamIllegal("orderId为空");
        }

        return Result.buildSucc();
    }

    private Result<Void> doProcessByWorkOrderHandle(WorkOrderPO orderPO, WorkOrderProcessDTO processDTO) {
        WorkOrderHandler handler = (WorkOrderHandler) handleFactory.getByHandlerNamePer(orderPO.getType());

        Result<Void> checkAuthResult = handler.checkAuthority(orderPO, processDTO.getAssignee());
        if (processDTO.getCheckAuthority().booleanValue() && checkAuthResult.failed()) {
            return checkAuthResult;
        }

        try {

            if (AGREE.getValue().equals(processDTO.getOutcome())) {
                return handler.processAgree(builderWorkOrder(orderPO, processDTO), processDTO.getAssignee(),
                    processDTO.getComment());
            }

            if (DISAGREE.getValue().equals(processDTO.getOutcome())) {
                return handler.processDisagree(orderPO, processDTO);
            }

        } catch (AdminOperateException e) {
            LOGGER.error("class=WorkOrderController||method=doProcessByWorkOrderHandle||errMsg={}", e.getMessage(), e);
            return Result.buildFail("操作失败, 请联系管理员");
        }

        return Result.buildFail("审批结果非法");
    }

    private WorkOrder builderWorkOrder(WorkOrderPO orderPO, WorkOrderProcessDTO processDTO) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(orderPO.getId());
        workOrder.setTitle(orderPO.getTitle());
        workOrder.setSubmitor(orderPO.getApplicant());
        workOrder.setSubmitorAppid(orderPO.getApplicantAppId());
        workOrder.setType(orderPO.getType());
        workOrder.setOpinion(processDTO.getComment());
        //合并两Json属性
        JSONObject jsonObject = JSON.parseObject(orderPO.getExtensions());
        jsonObject.putAll(JSON.parseObject(ConvertUtil.obj2Json(processDTO.getContentObj())));
        workOrder.setContentObj(jsonObject.toJavaObject(Object.class));
        return workOrder;
    }

    private OrderDetailBaseVO convert2DetailBaseVO(OrderDetail orderDetail) {
        OrderDetailBaseVO baseVO = new OrderDetailBaseVO();

        baseVO.setId(orderDetail.getId());
        baseVO.setType(orderDetail.getType());
        baseVO.setStatus(orderDetail.getStatus());
        baseVO.setAppDeptName(orderDetail.getAppDeptName());
        baseVO.setApplicant(ConvertUtil.obj2Obj(orderDetail.getApplicant(), AriusUserInfoVO.class));
        baseVO.setApplicantAppId(orderDetail.getApplicantAppId());
        baseVO.setApplicantAppName(getApplicantAppName(orderDetail.getApplicantAppId()));
        baseVO.setApproverList(ConvertUtil.list2List(orderDetail.getApproverList(), AriusUserInfoVO.class));
        baseVO.setFinishTime(orderDetail.getFinishTime());
        baseVO.setCreateTime(orderDetail.getCreateTime());
        baseVO.setTitle(orderDetail.getTitle());
        baseVO.setOpinion(orderDetail.getOpinion());
        baseVO.setDescription(orderDetail.getDescription());
        baseVO.setDetail(JSON.toJSONString(orderDetail.getDetail()));

        return baseVO;
    }

    private List<WorkOrderPO> handleOrderList(String userName, List<WorkOrderPO> orderList) {
        orderList = orderList.stream().filter(orderDO -> {
            try {
                WorkOrderHandler handler = (WorkOrderHandler) handleFactory.getByHandlerNamePer(orderDO.getType());

                AbstractOrderDetail abstractOrderDetail = handler.getOrderDetail(orderDO.getExtensions());
                if (null == abstractOrderDetail) {
                    return false;
                }

                List<AriusUserInfo> ariusUserInfos = handler.getApproverList(abstractOrderDetail);

                for (AriusUserInfo ariusUserInfo : ariusUserInfos) {
                    if (userName.equals(ariusUserInfo.getDomainAccount())) {
                        return true;
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("class=WorkOrderManagerImpl||method=getWaitApprovalList||userName={}||msg=exception!",
                    userName, e);
            }
            return false;
        }).collect(Collectors.toList());
        return orderList;
    }

    private String getApplicantAppName(Integer applicantAppId) {
        App app = appService.getAppById(applicantAppId);
        if (null != app) {
            return app.getName();
        }

        return null;
    }
}
