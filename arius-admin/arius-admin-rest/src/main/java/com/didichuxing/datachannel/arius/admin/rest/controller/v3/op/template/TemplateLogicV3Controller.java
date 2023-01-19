package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplateLogicSettingsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.*;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateIncrementalSettingsDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateSettingVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * Created by linyunan on 2021-07-30
 */
@RestController
@RequestMapping({ V3 + "/template/logic" })
@Api(tags = "逻辑模板接口(REST)")
public class TemplateLogicV3Controller {

    @Autowired
    private TemplateLogicManager         templateLogicManager;

    @Autowired
    private TemplateLogicSettingsManager templateLogicSettingManager;

    @GetMapping("/data-type")
    @ResponseBody
    @ApiOperation(value = "获取逻辑模版创建的类型")
    public Result<Map<Integer, String>> templateLogicDataType(HttpServletRequest request) {
        return Result.buildSucc(templateLogicManager.getDataTypeCode2DescMap());
    }

    @GetMapping("/names")
    @ResponseBody
    @ApiOperation(value = "获取逻辑模板名称列表接口")
    public Result<List<String>> listTemplateLogicNames(HttpServletRequest request) {
        return Result.buildSucc(templateLogicManager.getTemplateLogicNames(HttpRequestUtil.getProjectId(request)));
    }

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "模糊查询模板列表")
    public PaginationResult<ConsoleTemplateVO> pageGetConsoleTemplateVOS(HttpServletRequest request,
                                                                         @RequestBody TemplateConditionDTO condition) throws NotFindSubclassException {
        return templateLogicManager.pageGetConsoleTemplateVOS(condition, HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/{templateName}/name-check")
    @ResponseBody
    @ApiOperation(value = "校验模板名称是否合法")
    public Result<Void> checkTemplateValidForCreate(@PathVariable("templateName") String templateName) {
        return templateLogicManager.checkTemplateValidForCreate(templateName);
    }

    @GetMapping("/{templateId}/check-edit-mapping")
    @ResponseBody
    @ApiOperation(value = "校验可否编辑模板mapping")
    public Result<Boolean> checkTemplateEditMapping(@PathVariable Integer templateId) {
        return templateLogicManager.checkTemplateEditMapping(templateId);
    }

    @GetMapping("/{templateId}/{templateSrvId}/check-edit-template-srv/")
    @ResponseBody
    @ApiOperation(value = "校验模板是否可以使用指定的索引模板服务，例如是否可以编辑mapping,setting等")
    @ApiImplicitParams({ @ApiImplicitParam(name = "templateId", dataType = "Integer", value = "逻辑模板id", required = true),
                         @ApiImplicitParam(name = "templateSrvId", dataType = "Integer", value = "索引模板服务的id，例如mapping设置", required = true) })
    public Result<Boolean> checkTemplateEditService(@PathVariable("templateId") Integer templateId,
                                                    @PathVariable("templateSrvId") Integer templateSrvId) {
        return templateLogicManager.checkTemplateEditService(templateId, templateSrvId);
    }

    @PutMapping("/rollover/switch/{templateLogicId}/{status}")
    @ResponseBody
    @ApiOperation(value = "更改逻辑模版的rollover能力")
    @ApiImplicitParams({ @ApiImplicitParam(name = "templateLogicId", dataType = "Integer", value = "逻辑模版id", required = true),
                         @ApiImplicitParam(name = "status", dataType = "Integer", value = "停启rollover能力（1 启用，0 禁用）", required = true) })
    public Result<Void> switchRolloverStatus(@PathVariable Integer templateLogicId, @PathVariable Integer status,
                                             HttpServletRequest request) {
        String operator = HttpRequestUtil.getOperator(request);
        return templateLogicManager.switchRolloverStatus(templateLogicId, status, operator,
            HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("/setting")
    @ResponseBody
    @ApiOperation(value = "更新索引Setting接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-APP-ID", value = "应用ID", required = true) })
    public Result<Void> customizeSetting(HttpServletRequest request,
                                         @RequestBody TemplateSettingDTO settingDTO) throws AdminOperateException {
        Result<Void> checkAuthResult = templateLogicManager.checkProjectAuthOnLogicTemplate(settingDTO.getLogicId(),
            HttpRequestUtil.getProjectId(request));
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }

        return templateLogicSettingManager.customizeSetting(settingDTO, HttpRequestUtil.getOperator(request));
    }

    @GetMapping("/setting")
    @ResponseBody
    @ApiOperation(value = "获取索引Setting接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    @Deprecated
    public Result<TemplateSettingVO> getTemplateSettings(@RequestParam("logicId") Integer logicId) throws AdminOperateException {
        return templateLogicSettingManager.buildTemplateSettingVO(logicId);
    }

    @GetMapping("/templates")
    @ResponseBody
    @ApiOperation(value = "根据物理集群名称获取对应全量逻辑模板列表", notes = "")
    public Result<List<ConsoleTemplateVO>> getLogicTemplatesByCluster(HttpServletRequest request,
                                                                      @RequestParam("cluster") String cluster) {
        return templateLogicManager.getTemplateVOByPhyCluster(cluster);
    }
   

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "创建逻辑模板")
    public Result<Void> createTemplate(HttpServletRequest request,
                                       @RequestBody IndexTemplateWithCreateInfoDTO param) {
        return templateLogicManager.create(param, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @PutMapping()
    @ResponseBody
    @ApiOperation(value = "用户编辑模板")
    public Result<Void> editTemplate(HttpServletRequest request, @RequestBody IndexTemplateDTO param) {
        return templateLogicManager.editTemplate(param, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @DeleteMapping("/indices")
    @ResponseBody
    @ApiOperation(value = "清理索引")
    public Result<Void> clearIndices(HttpServletRequest request, @RequestBody TemplateClearDTO clearDTO) {
        return templateLogicManager.clearIndices(clearDTO, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("/{templateId}/{shardNum}/adjust-shard")
    @ResponseBody
    @ApiOperation(value = "扩缩容")
    public Result<Void> adjustShard(HttpServletRequest request, @PathVariable("templateId") Integer templateId,
                                    @PathVariable("shardNum") Integer shardNum) throws AdminOperateException {
        return templateLogicManager.adjustShard(templateId, shardNum, HttpRequestUtil.getProjectId(request),
            HttpRequestUtil.getOperator(request));
    }

    @PutMapping("/{templateId}/upgrade")
    @ResponseBody
    @ApiOperation(value = "升版本")
    public Result<Void> upgrade(HttpServletRequest request,
                                @PathVariable Integer templateId) throws AdminOperateException {
        return templateLogicManager.upgrade(templateId, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/{clusterPhyName}/phy/templates")
    @ResponseBody
    @ApiOperation(value = "根据物理集群名称获取对应全量逻辑模板列表", notes = "")
    public Result<List<ConsoleTemplateVO>> getLogicTemplatesByPhyCluster(HttpServletRequest request,
                                                                         @PathVariable String clusterPhyName) {
        return templateLogicManager.getTemplateVOByPhyCluster(clusterPhyName);
    }

    @GetMapping("/{clusterLogicName}/logic/templates")
    @ResponseBody
    @ApiOperation(value = "根据逻辑集群名称获取对应全量逻辑模板列表", notes = "")
    public Result<List<ConsoleTemplateVO>> listTemplateVOByLogicCluster(HttpServletRequest request,
                                                                        @PathVariable String clusterLogicName) {
        return templateLogicManager.listTemplateVOByLogicCluster(clusterLogicName,
            HttpRequestUtil.getProjectId(request));
    }
    
    @PutMapping("/{templateId}/block-write")
    @ResponseBody
    @ApiOperation(value = "写变更")
    public Result<Void> write(HttpServletRequest request, @PathVariable Integer templateId,
                              @RequestParam("status") Boolean status)  {
        return templateLogicManager.blockWrite(templateId, status, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }
    
    @PutMapping("/{templateId}/block-read")
    @ResponseBody
    @ApiOperation(value = "读变更")
    public Result<Void> read(HttpServletRequest request, @PathVariable Integer templateId,
                             @RequestParam("status") Boolean status) {
        return templateLogicManager.blockRead(templateId, status, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("/template-index-setting")
    @ResponseBody
    @ApiOperation(value = "更新模版settings和非分区模版索引的settings")
    public Result<Void> updateTemplateAndIndexSettings(HttpServletRequest request, @RequestBody TemplateIncrementalSettingsDTO param) throws AdminOperateException {
        return templateLogicManager.updateTemplateAndIndexSettings(param, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }
}