package com.didichuxing.datachannel.arius.admin.biz.workorder.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.workorder.BpmAuditTypeEnum.AGREE;
import static com.didichuxing.datachannel.arius.admin.common.constant.workorder.BpmAuditTypeEnum.DISAGREE;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.workorder.WorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.WorkOrderManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.OrderInfoDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.AriusWorkOrderInfoSubmittedVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.OrderTypeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.WorkOrderVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.detail.OrderDetailBaseVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.OrderStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.workorder.WorkOrderDAO;
import com.didiglobal.logi.security.common.entity.dept.Dept;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.service.DeptService;
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.service.UserService;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service
public class WorkOrderManagerImpl implements WorkOrderManager {

    private static final Logger  LOGGER           = LoggerFactory.getLogger(WorkOrderManagerImpl.class);

    private static final int PROJECT_ID_TO_CREATE = -99;

    @Autowired
    private HandleFactory        handleFactory;

    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    @Autowired
    private WorkOrderDAO orderDao;
    @Autowired
    private DeptService deptService;
    @Autowired
    private RoleTool roleTool;



    @Override
    public Result<List<OrderTypeVO>> getOrderTypes() {
        List<OrderTypeVO> orderTypeVOList = new ArrayList<>();
        orderTypeVOList.add(new OrderTypeVO(WorkOrderTypeEnum.LOGIC_CLUSTER_CREATE.getName(),
                WorkOrderTypeEnum.LOGIC_CLUSTER_CREATE.getMessage()));
        orderTypeVOList.add(new OrderTypeVO(WorkOrderTypeEnum.LOGIC_CLUSTER_INDECREASE.getName(),
                WorkOrderTypeEnum.LOGIC_CLUSTER_INDECREASE.getMessage()));
        return Result.buildSucc(orderTypeVOList);
    }

    @Override
    public Result<AriusWorkOrderInfoSubmittedVO> submit(WorkOrderDTO workOrderDTO) throws AdminOperateException {

        String workOrderJsonString = JSON.toJSONString(workOrderDTO);
        LOGGER.info("class=WorkOrderManagerImpl||method=WorkOrderController.process||workOrderDTO={}||envInfo={}||dataCenter={}",
            workOrderJsonString, EnvUtil.getStr(), workOrderDTO.getDataCenter());

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
            if (orderPO.getApproverProjectId() == null) {
                orderPO.setApproverProjectId(AuthConstant.SUPER_PROJECT_ID);
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
            //是用户 但不是管理员
            if (!roleTool.isAdmin(approver) && Objects.nonNull(userService.getUserBriefByUserName(approver))) {
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
            if (!roleTool.isAdmin(approver)) {
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
        
        if (roleTool.isAdmin(userName)) {
            return orderList;
        } else {
            orderList = handleOrderList(userName, orderList);
        }
        return orderList;
    }

    @Override
    public OrderInfoDetail getBaseDetail(WorkOrderPO orderPO) {
        if (AriusObjUtils.isNull(orderPO)) {
            return null;
        }

        OrderInfoDetail detailBaseDTO = new OrderInfoDetail();
        detailBaseDTO.setDescription(orderPO.getDescription());
        detailBaseDTO.setCreateTime(orderPO.getCreateTime());
        detailBaseDTO.setFinishTime(orderPO.getFinishTime());
        detailBaseDTO.setId(orderPO.getId());
        detailBaseDTO.setOpinion(orderPO.getOpinion());
        detailBaseDTO.setStatus(orderPO.getStatus());
        detailBaseDTO.setType(orderPO.getType());
        detailBaseDTO.setTitle(orderPO.getTitle());
        detailBaseDTO.setApplicantProjectId(orderPO.getApplicantProjectId());
        UserBriefVO userBriefVO = userService.getUserBriefByUserName(orderPO.getApplicant());
        Map<Integer, Dept> map = deptService.getAllDeptMap();
        if (Objects.nonNull(userBriefVO) && map.containsKey(userBriefVO.getDeptId())) {
            detailBaseDTO.setAppDeptName(map.get(userBriefVO.getDeptId()).getDeptName());
        
        }

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
        UserBriefVO briefVO = userService.getUserBriefByUserName(orderPO.getApplicant());
        detailBaseDTO.setApplicant(briefVO);
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

        if (AriusObjUtils.isNull(userService.getUserBriefByUserName(workOrderDTO.getSubmitor()))) {
            return Result.buildParamIllegal("提交人非法");
        }
    
        if (AriusObjUtils.isNull(workOrderDTO.getSubmitorProjectid())) {
            return Result.buildParamIllegal("提交projectID为空");
        }
        if (PROJECT_ID_TO_CREATE != workOrderDTO.getSubmitorProjectid() && !projectService.checkProjectExist(
                workOrderDTO.getSubmitorProjectid())) {
            return Result.buildNotExist("提交projectId不存在");
        }

        return Result.buildSucc();
    }

    private void initWorkOrderDTO(WorkOrderDTO workOrderDTO) {
        workOrderDTO.setSubmitor(SpringTool.getUserName());
    }

    private AriusWorkOrderInfoSubmittedVO convert2WorkOrderSubmittedVO(WorkOrderPO workOrderPO) {
        if (AriusObjUtils.isNull(workOrderPO)) {
            return null;
        }
        AriusWorkOrderInfoSubmittedVO vo = new AriusWorkOrderInfoSubmittedVO();
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
        workOrder.setSubmitorProjectId(orderPO.getApplicantProjectId());
        workOrder.setType(orderPO.getType());
        workOrder.setOpinion(processDTO.getComment());
        //合并两Json属性
        JSONObject jsonObject = JSON.parseObject(orderPO.getExtensions());
        jsonObject.putAll(JSON.parseObject(ConvertUtil.obj2Json(processDTO.getContentObj())));
        workOrder.setContentObj(jsonObject.toJavaObject(Object.class));
        return workOrder;
    }

    private OrderDetailBaseVO convert2DetailBaseVO(OrderInfoDetail orderInfoDetail) {
        OrderDetailBaseVO baseVO = new OrderDetailBaseVO();

        baseVO.setId(orderInfoDetail.getId());
        baseVO.setType(orderInfoDetail.getType());
        baseVO.setStatus(orderInfoDetail.getStatus());
        baseVO.setAppDeptName(orderInfoDetail.getAppDeptName());
        baseVO.setApplicant(orderInfoDetail.getApplicant());
        baseVO.setApplicantProjectId(orderInfoDetail.getApplicantProjectId());
        baseVO.setApplicantAppName(getApplicantAppName(orderInfoDetail.getApplicantProjectId()));
        baseVO.setApproverList(orderInfoDetail.getApproverList());
        baseVO.setFinishTime(orderInfoDetail.getFinishTime());
        baseVO.setCreateTime(orderInfoDetail.getCreateTime());
        baseVO.setTitle(orderInfoDetail.getTitle());
        baseVO.setOpinion(orderInfoDetail.getOpinion());
        baseVO.setDescription(orderInfoDetail.getDescription());
        baseVO.setDetail(JSON.toJSONString(orderInfoDetail.getDetail()));

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

                List<UserBriefVO> ariusUserInfos = handler.getApproverList(abstractOrderDetail);

                for (UserBriefVO userBriefVO : ariusUserInfos) {
                    if (userName.equals(userBriefVO.getUserName())) {
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

    private String getApplicantAppName(Integer applicantProjectId) {
        ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(applicantProjectId);
        if (null != briefVO) {
            return briefVO.getProjectName();
        }

        return null;
    }
}