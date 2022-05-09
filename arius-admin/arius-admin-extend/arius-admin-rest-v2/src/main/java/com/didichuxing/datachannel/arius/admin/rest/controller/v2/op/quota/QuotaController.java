package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.quota;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateCostVO;
import com.didichuxing.datachannel.arius.admin.common.constant.quota.NodeSpecifyEnum;
import com.didichuxing.datachannel.arius.admin.core.component.QuotaTool;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController()
@RequestMapping(V2 + "/quota")
@Api(tags = "quota管控接口")
public class QuotaController {

    @Autowired
    private TemplateQuotaManager templateQuotaManager;

    @Autowired
    private QuotaTool              quotaTool;

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

}
