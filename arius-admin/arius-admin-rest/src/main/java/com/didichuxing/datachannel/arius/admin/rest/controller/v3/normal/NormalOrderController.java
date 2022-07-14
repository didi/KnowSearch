package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.workorder.WorkOrderManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder.WorkOrderProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.AriusWorkOrderInfoSubmittedVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.OrderTypeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.WorkOrderVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.detail.OrderDetailBaseVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.exception.OperateForbiddenException;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didiglobal.logi.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @author fengqiongfeng
 * @date 2020/08/24
 */
@Api(tags = "Normal-工单相关接口(REST)")
@RestController
@RequestMapping(V3 + "/order")
public class NormalOrderController {

    @Autowired
    private WorkOrderManager workOrderManager;

    @GetMapping("/type-enums")
    @ResponseBody
    @ApiOperation(value = "获取工单类型")
    public Result<List<OrderTypeVO>> getOrderTypes() {
        return workOrderManager.getOrderTypes();
    }

    @PutMapping("/{type}/submit")
    @ResponseBody
    @ApiOperation(value = "提交工单接口")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "String", name = "type", value = "工单类型", required = true) })
    public Result<AriusWorkOrderInfoSubmittedVO> submit(@PathVariable(value = "type") String type,
                                                        @RequestBody WorkOrderDTO workOrderDTO) throws AdminOperateException {
        return workOrderManager.submit(workOrderDTO);
    }

    @PutMapping("/{orderId}")
    @ResponseBody
    @ApiOperation(value = "审核")
    public Result<Void> process(@PathVariable(value = "orderId") Long orderId,
                                @RequestBody WorkOrderProcessDTO processDTO) throws NotFindSubclassException,
                                                                             OperateForbiddenException {
        //设置当前操作人
        processDTO.setAssignee(SpringTool.getUserName());
        return workOrderManager.process(processDTO);
    }

    @DeleteMapping("/{orderId}")
    @ResponseBody
    @ApiOperation(value = "工单撤销")
    public Result<Void> cancelOrder(@PathVariable(value = "orderId") Long orderId) throws OperateForbiddenException {
        return workOrderManager.cancelOrder(orderId, SpringTool.getUserName());
    }

    @GetMapping("/{orderId}")
    @ResponseBody
    @ApiOperation(value = "工单详情")
    public Result<OrderDetailBaseVO> getOrderDetail(@PathVariable(value = "orderId") Long orderId) {
        return workOrderManager.getById(orderId);
    }

    @GetMapping("/orders")
    @ResponseBody
    @ApiOperation(value = "工单申请列表")
    public Result<List<WorkOrderVO>> getOrderApplyList(HttpServletRequest httpServletRequest,
                                                       @RequestParam(value = "status") Integer status) throws OperateForbiddenException {
        return workOrderManager.getOrderApplyList(SpringTool.getUserName(), status,
            HttpRequestUtil.getProjectId(httpServletRequest));
    }

    @GetMapping("/approvals")
    @ResponseBody
    @ApiOperation(value = "工单审核列表")
    public Result<List<WorkOrderVO>> getOrderApprovalList(@RequestParam(value = "status", required = false) Integer status) throws OperateForbiddenException {
        return workOrderManager.getOrderApprovalListByStatus(status);
    }
}