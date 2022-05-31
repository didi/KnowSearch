package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.dsl;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.biz.dsl.DslMetricsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DslMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.SearchDslTemplateResponseVO;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor
@RestController()
@RequestMapping(V3_OP + "/dsl/metrics")
@Api(tags = "DSL指标接口(REST)")
public class DslMetricsController {

    @Autowired
    private DslMetricsManager dslMetricsManager;

    @GetMapping(path = "/project.do")
    @ApiOperation(value = "根据projectId获取dsl的指标信息", notes = "根据projectId获取dsl的指标信息", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "应用账号", required = false, example = "1"),
            @ApiImplicitParam(name = "startDate", value = "开始时刻", required = true, example = "1550160000000"),
            @ApiImplicitParam(name = "startDate", value = "开始时刻", required = true, example = "1550160000000")
    })
    public Result<List<DslTemplateVO>> listByProjectId(@RequestParam(value = "projectId", required = false) Integer projectId,
                                                       @RequestParam(value = "startDate") Long startDate,
                                                       @RequestParam(value = "endDate") Long endDate, HttpServletRequest request) {
        projectId = projectId == null ? HttpRequestUtil.getProjectId(request) : projectId;
        return dslMetricsManager.getDSLMetricsInfoByProjectId(projectId, startDate, endDate);
    }

    @GetMapping(path = "/detail.do")
    @ApiOperation(value = "根据projectId和模板名称获取dsl的指标信息", notes = "根据projectId获取dsl的指标信息", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "应用账号", required = false, example = "1"),
            @ApiImplicitParam(name = "dslTemplateMd5", value = "查询模板MD5", required = true, example = "V2_EA317B2029682DB83A191CBD797A66FE"),
            @ApiImplicitParam(name = "startDate", value = "开始时刻", required = true, example = "1550160000000"),
            @ApiImplicitParam(name = "endDate", value = "结束时刻", required = true, example = "1550246399999")
    })
    public Result<List<DslMetricsVO>> getDetailMetrics(@RequestParam(value = "projectId", required = false) Integer projectId,
                                                       @RequestParam(value = "dslTemplateMd5") String dslTemplateMd5,
                                                       @RequestParam(value = "startDate") Long startDate, @RequestParam(value = "endDate") Long endDate,
                                                       HttpServletRequest request) {
        projectId = projectId == null ? HttpRequestUtil.getProjectId(request) : projectId;
        return dslMetricsManager.getDetailMetrics(projectId, dslTemplateMd5, startDate, endDate);
    }

    @GetMapping(path = "/condition.do")
    @ApiOperation(value = "根据查询条件获取查询模板数据", notes = "根据查询条件获取查询模板数据", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "应用账号", required = false, example = "1"),
            @ApiImplicitParam(name = "searchKeyword", value = "查询关键字（如果是模糊查询则后面带上*）", required = false, example = "dos_order*"),
            @ApiImplicitParam(name = "dslTag", value = "危害标签", required = false, example = "dslTag"),
            @ApiImplicitParam(name = "sortInfo", value = "排序信息", required = false, example = ""),
            @ApiImplicitParam(name = "from", value = "分页起始", required = true, example = "0"),
            @ApiImplicitParam(name = "size", value = "每页记录数", required = true, example = "10"),
            @ApiImplicitParam(name = "startDate", value = "开始时刻", required = true, example = "1550160000000"),
            @ApiImplicitParam(name = "endDate", value = "结束时刻", required = true, example = "1550246399999")
    })
    public Result<SearchDslTemplateResponseVO> searchTemplate(@RequestParam(value = "projectId", required = false) Integer projectId,
                                                              @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
                                                              @RequestParam(value = "dslTag", required = false) String dslTag,
                                                              @RequestParam(value = "sortInfo", required = false) String sortInfo,
                                                              @RequestParam(value = "from") Long from, @RequestParam(value = "size") Long size,
                                                              @RequestParam(value = "startDate") Long startDate, @RequestParam(value = "endDate") Long endDate,
                                                              HttpServletRequest request) {
        projectId = projectId == null ? HttpRequestUtil.getProjectId(request) : projectId;
        return dslMetricsManager.getDslTemplateByCondition(projectId, searchKeyword, dslTag, sortInfo, from, size,
            startDate, endDate);
    }
}