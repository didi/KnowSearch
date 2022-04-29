package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.dsl;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.dsl.DslMetricsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DslMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.SearchDslTemplateResponseVO;

import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;

@NoArgsConstructor
@RestController()
@RequestMapping(V3_OP + "/dsl/metrics")
@Api(tags = "DSL指标接口(REST)")
public class DslMetricsController {

    @Autowired
    private DslMetricsManager dslMetricsManager;

    @GetMapping(path = "/listByAppid.do")
    @ApiOperation(value = "根据appid获取dsl的指标信息", notes = "根据appid获取dsl的指标信息", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用账号", required = false, example = "1"),
            @ApiImplicitParam(name = "startDate", value = "开始时刻", required = true, example = "1550160000000"),
            @ApiImplicitParam(name = "startDate", value = "开始时刻", required = true, example = "1550160000000")
    })
    public Result<List<DslTemplateVO>> listByAppid(@RequestParam(value = "appId", required = false) Integer appId,
                                                   @RequestParam(value = "startDate") Long startDate,
                                                   @RequestParam(value = "endDate") Long endDate, HttpServletRequest request) {
        appId = appId == null ? HttpRequestUtils.getAppId(request) : appId;
        return dslMetricsManager.getDSLMetricsInfoByAppId(appId, startDate, endDate);
    }

    @GetMapping(path = "/detail.do")
    @ApiOperation(value = "根据appid和模板名称获取dsl的指标信息", notes = "根据appid获取dsl的指标信息", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用账号", required = false, example = "1"),
            @ApiImplicitParam(name = "dslTemplateMd5", value = "查询模板MD5", required = true, example = "V2_EA317B2029682DB83A191CBD797A66FE"),
            @ApiImplicitParam(name = "startDate", value = "开始时刻", required = true, example = "1550160000000"),
            @ApiImplicitParam(name = "endDate", value = "结束时刻", required = true, example = "1550246399999")
    })
    public Result<List<DslMetricsVO>> getDetailMetrics(@RequestParam(value = "appId", required = false) Integer appId,
                                                       @RequestParam(value = "dslTemplateMd5") String dslTemplateMd5,
                                                       @RequestParam(value = "startDate") Long startDate, @RequestParam(value = "endDate") Long endDate,
                                                       HttpServletRequest request) {
        appId = appId == null ? HttpRequestUtils.getAppId(request) : appId;
        return dslMetricsManager.getDetailMetrics(appId, dslTemplateMd5, startDate, endDate);
    }

    @GetMapping(path = "/listByCondition.do")
    @ApiOperation(value = "根据查询条件获取查询模板数据", notes = "根据查询条件获取查询模板数据", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用账号", required = false, example = "1"),
            @ApiImplicitParam(name = "searchKeyword", value = "查询关键字（如果是模糊查询则后面带上*）", required = false, example = "dos_order*"),
            @ApiImplicitParam(name = "dslTag", value = "危害标签", required = false, example = "dslTag"),
            @ApiImplicitParam(name = "sortInfo", value = "排序信息", required = false, example = ""),
            @ApiImplicitParam(name = "from", value = "分页起始", required = true, example = "0"),
            @ApiImplicitParam(name = "size", value = "每页记录数", required = true, example = "10"),
            @ApiImplicitParam(name = "startDate", value = "开始时刻", required = true, example = "1550160000000"),
            @ApiImplicitParam(name = "endDate", value = "结束时刻", required = true, example = "1550246399999")
    })
    public Result<SearchDslTemplateResponseVO> searchTemplate(@RequestParam(value = "appId", required = false) Integer appId,
                                                              @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
                                                              @RequestParam(value = "dslTag", required = false) String dslTag,
                                                              @RequestParam(value = "sortInfo", required = false) String sortInfo,
                                                              @RequestParam(value = "from") Long from, @RequestParam(value = "size") Long size,
                                                              @RequestParam(value = "startDate") Long startDate, @RequestParam(value = "endDate") Long endDate,
                                                              HttpServletRequest request) {
        appId = appId == null ? HttpRequestUtils.getAppId(request) : appId;
        return dslMetricsManager.getDslTemplateByCondition(appId, searchKeyword, dslTag, sortInfo, from, size,
            startDate, endDate);
    }
}
