package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateRateLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateUpdateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateClearVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateDeleteVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateRateLimitVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateCyclicalRollInfoVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.AmsRemoteException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author d06679
 * @date 2019/5/15
 */
@RestController
@RequestMapping(V3 + "/template")
@Api(tags = "Console-用户侧索引模板接口(REST)")
public class TemplateController extends BaseTemplateController {

    private static final ILog    LOGGER = LogFactory.getLog(TemplateController.class);

    @Autowired
    private TemplateLogicManager templateLogicManager;

    @Autowired
    private ClusterLogicManager  clusterLogicManager;

    @GetMapping("/{projectId}")
    @ResponseBody
    @ApiOperation(value = "获取索引列表【三方接口】", tags = "【三方接口】", notes = "包含权限、集群信息、权限信息；")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "projectId", value = "应用ID，不会过滤索引，会影响权限信息", required = true) })
    public Result<List<ConsoleTemplateVO>> getConsoleTemplates(@PathVariable(value = "projectId", required = true) Integer projectId) {

        return Result.buildSucc(templateLogicManager.getConsoleTemplatesVOS(projectId));
    }

    @GetMapping("/detail/{logicId}")
    @ResponseBody
    @ApiOperation(value = "获取模板详细信息接口【三方接口】", tags = "【三方接口】")
    @ApiImplicitParam(type = "Integer", name = "logicId", value = "逻辑模板ID", required = true)
    public Result<ConsoleTemplateDetailVO> detail(HttpServletRequest request,
                                                  @PathVariable("logicId") Integer logicId) {

        return templateLogicManager.getDetailVoByLogicId(logicId, HttpRequestUtil.getProjectId(request));

    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "用户编辑模板接口【三方接口】", tags = "【三方接口】", notes = "支持修改数据类型、责任人、备注")
    public Result<Void> modifyConsoleTemplate(HttpServletRequest request,
                                              @RequestBody ConsoleTemplateUpdateDTO templateLogicDTO) throws AdminOperateException {
        return templateLogicManager.editTemplate(ConvertUtil.obj2Obj(templateLogicDTO, IndexTemplateDTO.class),
            HttpRequestUtil.getOperator(request), HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/logic/indices")
    @ResponseBody
    @ApiOperation(value = "获取索引清理信息接口【三方接口】", tags = "【三方接口】")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<ConsoleTemplateClearVO> getLogicTemplateClearInfo(@RequestParam("logicId") Integer logicId) throws AmsRemoteException {

        return templateLogicManager.getLogicTemplateClearInfo(logicId);
    }

    @PutMapping("/clear-info")
    @ResponseBody
    @ApiOperation(value = "清理索引信息接口【三方接口】", tags = "【三方接口】")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = HttpRequestUtil.PROJECT_ID, value = "应用ID", required = true) })
    @Deprecated
    public Result<Void> clearLogicTemplateIndices(HttpServletRequest request,
                                                  @RequestBody TemplateClearDTO clearDTO) throws ESOperateException {
        Result<Void> checkAuthResult = checkProjectAuth(clearDTO.getLogicId());
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }

        return clusterLogicManager.clearIndices(clearDTO, HttpRequestUtil.getOperator(request));
    }

    @GetMapping("/delete-info/{logicId}")
    @ResponseBody
    @ApiOperation(value = "获取将要索引下线信息接口【三方接口】", tags = "【三方接口】")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<ConsoleTemplateDeleteVO> getLogicTemplateDeleteInfo(@PathVariable("logicId") Integer logicId) throws AmsRemoteException {

        return templateLogicManager.getLogicTemplateDeleteInfo(logicId);
    }

    @DeleteMapping("/delete-info/{logicId}")
    @ResponseBody
    @ApiOperation(value = "下线索引信息接口【三方接口】", tags = "【三方接口】")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<Void> deleteTemplate(HttpServletRequest request,
                                       @PathVariable("logicId") Integer logicId) throws AdminOperateException {
        Result<Void> checkAuthResult = checkProjectAuth(logicId);
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }
        return templateLogicManager.delTemplate(logicId, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/indices/{projectId}")
    @ResponseBody
    @ApiOperation(value = "获取project Id所有模板的索引【三方接口】", tags = "【三方接口】")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "projectId", value = "应用ID", required = true) })
    public Result<List<Tuple<String, String>>> getLogicTemplatesByProjectId(HttpServletRequest request,
                                                                            @PathVariable("projectId") Integer projectId) {
        return templateLogicManager.listLogicTemplatesByProjectId(projectId);
    }

    @GetMapping("/cyclical-roll")
    @ResponseBody
    @ApiOperation(value = "获取索引分区信息")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<List<TemplateCyclicalRollInfoVO>> getCyclicalRollInfo(HttpServletRequest request,
                                                                        @RequestParam("logicId") Integer logicId) {

        return templateLogicManager.getCyclicalRollInfo(logicId);
    }

    /**
     * 获取模板当前限流值
     */
    @GetMapping(path = "/rate-limit")
    @ResponseBody
    @ApiOperation(value = "获取模板当前限流值接口【三方接口】", tags = "【三方接口】")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<ConsoleTemplateRateLimitVO> getTemplateRateLimit(@RequestParam("logicId") Integer logicId) { // 一个逻辑模板可能有master slave两种，限流查看时，默认查看master即可

        return templateLogicManager.getTemplateRateLimit(logicId);

    }

    /**
     * 更新模板当前限流值，更新时复用存量逻辑，调整时以百分比变更[-99,1000]
     *  @param consoleTemplateRateLimitDTO 索引模板限流调整表单信息
     *  @return
     *  @throws AdminOperateException
     */
    @PutMapping(path = "/rate-limit")
    @ResponseBody
    @ApiOperation(value = "更新模板当前限流值接口【三方接口】", tags = "【三方接口】")
    public Result updateTemplateRateLimit(HttpServletRequest request,
                                          @RequestBody ConsoleTemplateRateLimitDTO consoleTemplateRateLimitDTO) {

        return templateLogicManager.updateTemplateWriteRateLimit(consoleTemplateRateLimitDTO,
            HttpRequestUtil.getOperator(request), HttpRequestUtil.getProjectId(request));
    }
}