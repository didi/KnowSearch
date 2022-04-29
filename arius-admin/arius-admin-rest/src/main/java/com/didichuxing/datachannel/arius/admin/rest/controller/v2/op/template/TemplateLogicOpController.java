package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplateAction;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.limit.TemplateLimitManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.TemplatePipelineManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateLogicDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.AppTemplateAuthVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplateConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplateLogicAllVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.OpLogicTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.DataTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

@RestController
@RequestMapping(V2_OP + "/template/logic")
@Api(tags = "es集群逻辑模板接口(REST)")
public class TemplateLogicOpController {

    @Autowired
    private TemplateLogicService        templateLogicService;

    @Autowired
    private TemplatePhyService          templatePhyService;

    @Autowired
    private TemplatePipelineManager     templatePipelineManager;

    @Autowired
    private TemplateLimitManager        templateLimitManager;

    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Autowired
    private TemplateAction              templateAction;

    @Autowired
    private TemplateLabelService        templateLabelService;

    @Autowired
    private AppService                  appService;

    @Autowired
    private TemplatePhyManager          templatePhyManager;

    @Autowired
    private TemplateLogicManager        templateLogicManager;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取逻辑模板列表接口", notes = "")
    public Result<List<OpLogicTemplateVO>> list(@RequestBody IndexTemplateLogicDTO param) {
        return getLogicTemplateList(param);
    }

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取逻辑模板列表接口", notes = "")
    public Result<List<OpLogicTemplateVO>> getLogicTemplates(@RequestParam IndexTemplateLogicDTO param) {
        return getLogicTemplateList(param);
    }

    @GetMapping("/listByHasAuthCluster")
    @ResponseBody
    @ApiOperation(value = "获取APP有权限的逻辑集群下的的所有逻辑模板列表接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true) })
    public Result<List<OpLogicTemplateVO>> listByHasAuthCluster(@RequestParam("appId") Integer appId) {

        List<IndexTemplateLogic> hasClusterAuthTemplates = templateLogicService.getTemplatesByHasAuthCluster(appId);
        return Result.buildSucc(ConvertUtil.list2List(hasClusterAuthTemplates, OpLogicTemplateVO.class));
    }

    @GetMapping("/listHasAuthInLogicCluster")
    @ResponseBody
    @ApiOperation(value = "获取APP有权限的逻辑集群下的的所有逻辑模板列表接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "Long", name = "logicClusterId", value = "逻辑集群ID", required = true) })
    public Result<List<OpLogicTemplateVO>> listHasAuthInLogicCluster(@RequestParam("appId") Integer appId,
                                                                     @RequestParam("logicClusterId") Long logicClusterId) {
        List<IndexTemplateLogic> hasAuthTemplatesInCluster = templateLogicService
            .getHasAuthTemplatesInLogicCluster(appId, logicClusterId);
        return Result.buildSucc(ConvertUtil.list2List(hasAuthTemplatesInCluster, OpLogicTemplateVO.class));
    }

    @PostMapping("/query")
    @ResponseBody
    @ApiOperation(value = "获取逻辑模板列表接口", notes = "模糊查询接口")
    public Result<List<OpLogicTemplateVO>> query(@RequestBody IndexTemplateLogicDTO param) {
        return Result.buildSucc(ConvertUtil.list2List(templateLogicService.fuzzyLogicTemplatesByCondition(param),
            OpLogicTemplateVO.class));
    }

    @GetMapping("/listByLabelIds")
    @ResponseBody
    @ApiOperation(value = "根据标签获取逻辑模板接口")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "includeLabelIds", value = "包含的标签id"),
                         @ApiImplicitParam(paramType = "query", dataType = "String", name = "excludeLabelIds", value = "排除的标签id") })
    public Result<List<OpLogicTemplateVO>> listByLabelExpression(@RequestParam(value = "includeLabelIds", required = false) String includeLabelIds,
                                                                 @RequestParam(value = "excludeLabelIds", required = false) String excludeLabelIds) {
        return Result.buildSucc(ConvertUtil
            .list2List(templateLogicManager.getByLabelIds(includeLabelIds, excludeLabelIds), OpLogicTemplateVO.class));
    }

    @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取指定逻辑模板接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "逻辑模板ID", required = true) })
    public Result<OpLogicTemplateVO> getById(@RequestParam("logicId") Integer logicId) {
        return Result.buildSucc(
            ConvertUtil.obj2Obj(templateLogicService.getLogicTemplateById(logicId), OpLogicTemplateVO.class));
    }

    @GetMapping("/getTemplateByName")
    @ResponseBody
    @ApiOperation(value = "根据模板名称获取模板详情列表", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "templateName", value = "模板名称", required = true) })
    public Result<List<OpLogicTemplateVO>> getByName(@RequestParam("templateName") String templateName) {
        return Result.buildSucc(
            ConvertUtil.list2List(templateLogicService.getLogicTemplateByName(templateName), OpLogicTemplateVO.class));
    }

    @GetMapping("/getAll")
    @ResponseBody
    @ApiOperation(value = "获取指定逻辑模板详细信息接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "逻辑模板ID", required = true) })
    public Result<IndexTemplateLogicAllVO> getAll(@RequestParam("logicId") Integer logicId) {
        IndexTemplateLogic templateLogic = templateLogicService.getLogicTemplateById(logicId);
        if (templateLogic == null) {
            return Result.buildNotExist("模板不存在");
        }

        IndexTemplateLogicAllVO logicAllVO = ConvertUtil.obj2Obj(templateLogic, IndexTemplateLogicAllVO.class);

        logicAllVO.setDataTypeStr(DataTypeEnum.valueOf(templateLogic.getDataType()).getDesc());

        // 获取物理模板信息
        logicAllVO.setPhysicalVOS(
            ConvertUtil.list2List(templatePhyService.getTemplateByLogicId(logicId), IndexTemplatePhysicalVO.class));

        // 获取访问APP信息
        logicAllVO.setTemplateAuthVOS(ConvertUtil.list2List(
            appLogicTemplateAuthService.getTemplateAuthsByLogicTemplateId(logicId), AppTemplateAuthVO.class));

        // 获取模板标签信息
        logicAllVO.setLabels(templateLabelService.listTemplateLabel(logicId));

        // 所属应用名称
        logicAllVO.setAppName(appService.getAppName(logicAllVO.getAppId()));

        return Result.buildSucc(logicAllVO);
    }

    @DeleteMapping("/del")
    @ResponseBody
    @ApiOperation(value = "删除逻辑模板接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "逻辑模板ID", required = true) })
    public Result<Void> delete(HttpServletRequest request,
                         @RequestParam(value = "logicId") Integer logicId) throws AdminOperateException {
        return templateLogicManager.delTemplate(logicId, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新建逻辑模板接口", notes = "")
    public Result<Integer> add(HttpServletRequest request,
                               @RequestBody IndexTemplateLogicDTO param) throws AdminOperateException {
        return templateAction.createWithAutoDistributeResource(param, HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/edit")
    @ResponseBody
    @ApiOperation(value = "编辑逻辑模板接口", notes = "")
    public Result<Void> edit(HttpServletRequest request,
                       @RequestBody IndexTemplateLogicDTO param) throws AdminOperateException {
        // 不允许修改表达式
        param.setExpression(null);
        return templateLogicManager.editTemplate(param, HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/switchMasterSlave")
    @ResponseBody
    @ApiOperation(value = "主从切换接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "逻辑模板ID", required = true) })
    public Result<Void> switchMasterSlave(HttpServletRequest request, @RequestParam(value = "logicId") Integer logicId,
                                    @RequestParam(value = "expectMasterPhysicalId") Long expectMasterPhysicalId) {
        return templatePhyManager.switchMasterSlave(logicId, expectMasterPhysicalId,
            HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/config/get")
    @ResponseBody
    @ApiOperation(value = "获取逻辑模板配置接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "逻辑模板ID", required = true) })
    public Result<IndexTemplateConfigVO> getConfig(@RequestParam("logicId") Integer logicId) {
        return Result.buildSucc(
            ConvertUtil.obj2Obj(templateLogicService.getTemplateConfig(logicId), IndexTemplateConfigVO.class));
    }

    @PutMapping("/config/update")
    @ResponseBody
    @ApiOperation(value = "编辑逻辑模板配置接口", notes = "")
    public Result<Void> updateConfig(HttpServletRequest request, @RequestBody IndexTemplateConfigDTO configDTO) {
        return templateLogicService.updateTemplateConfig(configDTO, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/checkMeta")
    @ResponseBody
    @ApiOperation(value = "元数据校验接口", notes = "")
    public Result<Void> checkMeta() {
        return Result.build(templateLogicManager.checkAllLogicTemplatesMeta());
    }

    @PutMapping("/repair/pipeline")
    @ResponseBody
    @ApiOperation(value = "修复模板pipeline", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "逻辑模板ID", required = true) })
    public Result<Void> repairPipeline(HttpServletRequest request,
                                 @RequestParam(value = "logicId") Integer logicId) throws ESOperateException {
        return templatePipelineManager.repairPipeline(logicId);
    }

    @PostMapping("/editName")
    @ResponseBody
    @ApiOperation(value = "编辑逻辑模板名称接口", notes = "")
    public Result<Void> editName(HttpServletRequest request,
                           @RequestBody IndexTemplateLogicDTO param) throws AdminOperateException {
        return templateLogicService.editTemplateName(param, HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/adjustPipelineRateLimit")
    @ResponseBody
    @ApiOperation(value = "调整Pipeline限流值", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "逻辑模板ID", required = true) })
    public Result<Void> adjustPipelineRateLimit(@RequestParam(value = "logicId") Integer logicId) {
        return Result.build(templateLimitManager.adjustPipelineRateLimit(logicId));
    }

    @RequestMapping(path = "/blockRead", method = RequestMethod.PUT)
    @ResponseBody
    @ApiOperation(value = "是否禁读", notes = "")
    public Result updateBlockReadState(HttpServletRequest request, @RequestBody IndexTemplateLogicDTO param) {
        return templateLogicService.updateBlockReadState(param.getId(), param.getBlockRead(), HttpRequestUtils.getOperator(request));
    }

    @RequestMapping(path = "/blockWrite", method = RequestMethod.PUT)
    @ResponseBody
    @ApiOperation(value = "是否禁写", notes = "")
    public Result updateBlockWriteState(HttpServletRequest request, @RequestBody IndexTemplateLogicDTO param) {
        return templateLogicService.updateBlockWriteState(param.getId(), param.getBlockWrite(), HttpRequestUtils.getOperator(request));
    }

    private Result<List<OpLogicTemplateVO>> getLogicTemplateList(IndexTemplateLogicDTO param){
        return Result
                .buildSucc(ConvertUtil.list2List(templateLogicService.getLogicTemplates(param), OpLogicTemplateVO.class));
    }
}
