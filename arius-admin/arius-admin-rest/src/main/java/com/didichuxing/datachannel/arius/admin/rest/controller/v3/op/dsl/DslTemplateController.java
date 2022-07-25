package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.dsl;

import com.didichuxing.datachannel.arius.admin.biz.dsl.DslTemplateManager;
import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayJoinLogManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.DslQueryLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayJoinVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

/**
 * @author cjm
 */
@NoArgsConstructor
@RestController()
@RequestMapping(V3 + "/dsl/template")
@Api(tags = "DSL模版管理接口(REST)")
public class DslTemplateController {

    @Autowired
    private DslTemplateManager    dslTemplateManager;

    @Autowired
    private GatewayJoinLogManager gatewayJoinLogManager;

    @PostMapping("/page")
    @ApiOperation(value = "分页获取DSL模版信息", notes = "根据一些条件分页获取DSL模版信息")
    public PaginationResult<DslTemplateVO> page(@RequestBody DslTemplateConditionDTO query,
                                                HttpServletRequest request) throws NotFindSubclassException {
        return dslTemplateManager.getDslTemplatePage(HttpRequestUtil.getProjectId(request), query);
    }

    @GetMapping(path = "/detail/{dslTemplateMd5}")
    @ApiOperation(value = "根据dslTemplateMd5称获取DSL模版详情", notes = "根据DSL模板MD5称获取DSL模版详情")
    @ApiImplicitParam(name = "dslTemplateMd5", value = "查询模板MD5", required = true)
    public Result<DslTemplateVO> getDetailTemplate(@PathVariable(value = "dslTemplateMd5") String dslTemplateMd5,
                                                   HttpServletRequest request) {
        return dslTemplateManager.getDslTemplateDetail(HttpRequestUtil.getProjectId(request), dslTemplateMd5);
    }

    @PutMapping(path = "/change/status/{dslTemplateMd5}")
    @ApiOperation(value = "根据dslTemplateMd5修改DSL模版状态（启用或停用）", notes = "调用该接口，直接对状态取反")
    @ApiImplicitParams({@ApiImplicitParam(name = "dslTemplateMd5", value = "查询模板MD5List", required = true), @ApiImplicitParam(name = "appId", value = "查询模板对应的appId", required = false)})
    public Result<Boolean> changeStatus(@PathVariable(value = "dslTemplateMd5") String dslTemplateMd5, @RequestParam(value = "projectId",required = false) Integer projectId, HttpServletRequest request) {
        return dslTemplateManager.changeDslTemplateStatus(null != projectId ? projectId : HttpRequestUtil.getProjectId(request), dslTemplateMd5);
    }

    @PutMapping(path = "/update/queryLimit")
    @ApiOperation(value = "根据dslTemplateMd5修改查询模版限流值", notes = "可批量修改")
    public Result<Boolean> updateQueryLimit(@RequestBody List<DslQueryLimitDTO> dslTemplateList) {
        return dslTemplateManager.updateDslTemplateQueryLimit(dslTemplateList);
    }

    @PostMapping(path = "/slow/list")
    @ApiOperation(value = "dsl慢查询列表", notes = "根据指定页获取dsl慢查询列表")
    public Result<List<GatewayJoinVO>> slowList(@RequestBody GatewayJoinQueryDTO queryDTO, HttpServletRequest request) {
        return gatewayJoinLogManager.getGatewayJoinSlowList(HttpRequestUtil.getProjectId(request), queryDTO);
    }

    @PostMapping(path = "/error/list")
    @ApiOperation(value = "dsl异常查询列表", notes = "根据指定条件获取dsl异常查询列表")
    public Result<List<GatewayJoinVO>> errorList(@RequestBody GatewayJoinQueryDTO queryDTO,
                                                 HttpServletRequest request) {
        return gatewayJoinLogManager.getGatewayJoinErrorList(HttpRequestUtil.getProjectId(request), queryDTO);
    }
}