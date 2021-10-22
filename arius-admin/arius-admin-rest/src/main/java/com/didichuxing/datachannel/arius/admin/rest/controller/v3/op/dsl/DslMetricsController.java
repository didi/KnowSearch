package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.dsl;

import com.didichuxing.datachannel.arius.admin.biz.dsl.DslMetricsManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.DslMetricsVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.DslTemplateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

@NoArgsConstructor
@RestController()
@RequestMapping(V3_OP + "/dsl/metrics")
@Api(tags = "DSL指标接口(REST)")
public class DslMetricsController {

    @Autowired
    private DslMetricsManager dslMetricsManager;

    /**
     * 获取批量dslMetrics接口
     * @return
     */
    @GetMapping(path = "/listByAppid.do")
    @ApiOperation(value = "根据appid获取dsl的指标信息", notes = "根据appid获取dsl的指标信息", httpMethod = "GET")
    public Result<List<DslTemplateVO>> listByAppid(@ApiParam(name="appId", value="应用账号", required = true, example = "1")
                                                   @RequestParam(value = "appId")     Long appId,

                                                   @ApiParam(name="startDate", value="开始时刻", required = true, example = "1550160000000")
                                                   @RequestParam(value = "startDate") Long startDate,

                                                   @ApiParam(name="endDate", value="结束时刻", required = true, example = "1550246399999")
                                                   @RequestParam(value = "endDate")   Long endDate){
        return dslMetricsManager.getDSLMetricsInfoByAppId(appId, startDate, endDate);
    }

    /**
     * 获取批量dslMetrics接口
     * @return
     */
    @GetMapping(path = "/detail.do")
    @ApiOperation(value = "根据appid获取dsl的指标信息", notes = "根据appid获取dsl的指标信息", httpMethod = "GET")
    public Result<List<DslMetricsVO>> getDetailMetrics(@ApiParam(name="appId", value="应用账号", required = true, example = "1")
                                                       @RequestParam(value = "appId") int appId,

                                                       @ApiParam(name="dslTemplateMd5", value="查询模板MD5", required = true, example = "V2_EA317B2029682DB83A191CBD797A66FE")
                                                       @RequestParam(value = "dslTemplateMd5")  String dslTemplateMd5,

                                                       @ApiParam(name="startDate", value="开始时刻", required = true, example = "1550160000000")
                                                       @RequestParam(value = "startDate") Long startDate,

                                                       @ApiParam(name="endDate", value="结束时刻", required = true, example = "1550246399999")
                                                       @RequestParam(value = "endDate") Long endDate){
        return dslMetricsManager.getDetailMetrics(appId, dslTemplateMd5, startDate, endDate);
    }
}
