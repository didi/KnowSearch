package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.quota;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.TemplateCostVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.quota.ESTemplateQuotaUsageRecordVO;
import com.didichuxing.datachannel.arius.admin.client.constant.quota.NodeSpecifyEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.component.QuotaTool;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2;

/**
 * @author d06679
 * @date 2019/4/24
 */
@RestController()
@RequestMapping(V2 + "/quota")
@Api(value = "quota管控接口")
public class QuotaController {

    @Autowired
    private TemplateQuotaManager templateQuotaManager;

    @Autowired
    private QuotaTool              quotaTool;

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @GetMapping("/template/cost")
    @ResponseBody
    @ApiOperation(value = "计算模板成本", notes = "参数是返回:元/月；参数是节点规格：1是16C64G3T")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Double", name = "quota", value = "配额"),
                         @ApiImplicitParam(paramType = "query", dataType = "Double", name = "diskG", value = "数据总量(G)"),
                         @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "逻辑集群ID") })
    public Result<TemplateCostVO> templateCost(@RequestParam(value = "quota", required = false) Double quota,
                                             @RequestParam(value = "diskG", required = false) Double diskG,
                                             @RequestParam(value = "clusterId", required = false) Long resourceId) {

        TemplateCostVO templateCostVO = new TemplateCostVO();

        if (quota != null) {
            templateCostVO
                .setTotalPrice( templateQuotaManager.computeCostByQuota(NodeSpecifyEnum.DOCKER.getCode(), quota));
            return Result.buildSucc(templateCostVO);
        }

        if (diskG != null && resourceId != null) {
            templateCostVO.setTotalPrice( templateQuotaManager.computeCostByDisk(diskG, resourceId));
        }

        return Result.buildSucc(templateCostVO);
    }

    @GetMapping("/cluster/cost")
    @ResponseBody
    @ApiOperation(value = "计算独立集群成本", notes = "参数是返回:元/月；参数是节点规格：1是16C64G3T")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "nodeSpecify", value = "节点规格"),
                         @ApiImplicitParam(paramType = "query", dataType = "Double", name = "quota", value = "配额") })
    public Result<Double> clusterCost(@RequestParam("nodeSpecify") Integer nodeSpecify,
                                      @RequestParam("quota") Double quota) {
        return Result.buildSucc(quotaTool.computeCostByQuota(nodeSpecify, quota));
    }

    @GetMapping("/template/enableClt")
    @ResponseBody
    @ApiOperation(value = "获取模板是否在quota管控", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicID", value = "逻辑模板ID") })
    public Result<String> clusterCost(@RequestParam("logicId") Integer logicId) {
        if (templateQuotaManager.enableClt(logicId)) {
            return Result.buildSucc( templateQuotaManager.getCtlRange(logicId));
        }
        return Result.buildSucc("not control");
    }

    @GetMapping("/template/record/list.do")
    @ApiOperation(value = "获取某个模板的某段时间的quota利用率统计", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "logicTemplateId", value = "逻辑模板id", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "Long", name = "startDate", value = "查询开始时间，毫秒时间戳", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "Long", name = "endDate", value = "查询结束时间，毫秒时间戳", required = true) })
    public Result<List<ESTemplateQuotaUsageRecordVO>> getTemplateRecordByLogicTemplateId(@RequestParam(value = "logicTemplateId") Integer logicTemplateId,
                                                                                         @RequestParam(value = "startDate") Long startDate,
                                                                                         @RequestParam(value = "endDate") Long endDate) {
        if(EnvUtil.isTest() || EnvUtil.isDev()){
            logicTemplateId = ariusConfigInfoService.intSetting("arius.test.meta.replace", "logic.template.id", logicTemplateId);
        }

        return Result.buildSucc(
            ConvertUtil.list2List( templateQuotaManager.getByLogicIdAndTime(logicTemplateId, startDate, endDate),
                ESTemplateQuotaUsageRecordVO.class));
    }
}
