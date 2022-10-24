package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.dsl;

import com.didichuxing.datachannel.arius.admin.biz.dsl.DslTemplateManager;
import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayJoinLogManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.DslQueryLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.UserConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayJoinVO;
import com.didichuxing.datachannel.arius.admin.common.constant.QueryDiagnosisTabNameEnum;
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
    @ApiImplicitParams({@ApiImplicitParam(name = "dslTemplateMd5", value = "查询模板MD5", required = true),
                        @ApiImplicitParam(name = "projectId", value = "查询模板对应的projectId", required = false)})
    public Result<DslTemplateVO> getDetailTemplate(@PathVariable(value = "dslTemplateMd5") String dslTemplateMd5,
                                                   @RequestParam(value = "projectId") Integer projectId,
                                                   HttpServletRequest request) {
        return dslTemplateManager.getDslTemplateDetail(null!=projectId?projectId:HttpRequestUtil.getProjectId(request), dslTemplateMd5);
    }

    @PutMapping(path = "/status/{dslTemplateMd5}")
    @ApiOperation(value = "根据dslTemplateMd5修改DSL模版状态（启用或停用）", notes = "调用该接口，直接对状态取反")
    @ApiImplicitParams({@ApiImplicitParam(name = "dslTemplateMd5", value = "查询模板MD5List", required = true),
                        @ApiImplicitParam(name = "projectId", value = "查询模板对应的projectId", required = false)})
    public Result<Boolean> changeStatus(@PathVariable(value = "dslTemplateMd5") String dslTemplateMd5,
                                        @RequestParam(value = "projectId",required = false) Integer projectId, HttpServletRequest request) {

        return dslTemplateManager.changeDslTemplateStatus(null != projectId ? projectId : HttpRequestUtil.getProjectId(request),HttpRequestUtil.getOperator(request), dslTemplateMd5);
    }

    @PutMapping(path = "/query-limit")
    @ApiOperation(value = "根据dslTemplateMd5修改查询模版限流值", notes = "可批量修改")
    public Result<Boolean> updateQueryLimit(@RequestBody List<DslQueryLimitDTO> dslTemplateList, HttpServletRequest request) {
        return dslTemplateManager.updateDslTemplateQueryLimit(HttpRequestUtil.getProjectId(request),HttpRequestUtil.getOperator(),dslTemplateList);
    }

    @PostMapping("/slow/page")
    @ApiOperation(value = "dsl慢查询分页", notes = "根据指定页获取dsl慢查询列表")
    public PaginationResult<GatewayJoinVO> slowPage(@RequestBody GatewayJoinQueryDTO queryDTO, HttpServletRequest request) throws NotFindSubclassException {
        queryDTO.setTabName(QueryDiagnosisTabNameEnum.SLOW_QUERY.getTabName());
        return gatewayJoinLogManager.getGatewayJoinPage(HttpRequestUtil.getProjectId(request), queryDTO);
    }

    @PostMapping("/error/page")
    @ApiOperation(value = "dsl慢查询分页", notes = "根据指定页获取dsl慢查询列表")
    public PaginationResult<GatewayJoinVO> errorPage(@RequestBody GatewayJoinQueryDTO queryDTO, HttpServletRequest request) throws NotFindSubclassException {
        queryDTO.setTabName(QueryDiagnosisTabNameEnum.ERROR_QUERY.getTabName());
        return gatewayJoinLogManager.getGatewayJoinPage(HttpRequestUtil.getProjectId(request), queryDTO);
    }

    @GetMapping("/{indexName}")
    public Result<String> getDSLByProjectIdAndIndexName(@PathVariable(value = "indexName") String indexName,
                                                        HttpServletRequest request) {
        return gatewayJoinLogManager.getDSLByProjectIdAndIndexName(HttpRequestUtil.getProjectId(request), indexName);
    }
    @PostMapping("/dsl-template-config")
    @ResponseBody
    @ApiOperation(value = "获取账号下已配置字段类型")
    public Result<List<String>> listDslTemplateFields(@RequestBody UserConfigInfoDTO param,
                                                       HttpServletRequest request) {
        return Result
                .buildSucc(dslTemplateManager.listConfigDslTemplateFields(param, HttpRequestUtil.getOperator(request),HttpRequestUtil.getProjectId(request)));
    }

    @PutMapping("/dsl-template-config")
    @ResponseBody
    @ApiOperation(value = "更新账号下已配置指标类型")
    public Result<Integer> updateConfigDslTemplateFields(@RequestBody UserConfigInfoDTO param,
                                                        HttpServletRequest request) {
        return dslTemplateManager.updateConfigDslTemplateFields(param, HttpRequestUtil.getOperator(request),HttpRequestUtil.getProjectId(request));
    }
}