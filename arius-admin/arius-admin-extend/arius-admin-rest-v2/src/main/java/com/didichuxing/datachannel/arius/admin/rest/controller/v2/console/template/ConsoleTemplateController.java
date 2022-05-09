package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_CONSOLE;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.common.bean.common.QuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateRateLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateUpdateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.*;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.TemplatePipelineManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.ESTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.LogicTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @author d06679
 * @date 2019/5/15
 */
@RestController
@RequestMapping(V2_CONSOLE + "/template")
@Api(tags = "Console-用户侧索引模板接口(REST)")
public class ConsoleTemplateController extends BaseConsoleTemplateController {

    private static final ILog        LOGGER = LogFactory.getLog(ConsoleTemplateController.class);

    private static final String INDEX_NOT_EXISTS_TIPS = "索引不存在";
    private static final String APP_IS_NOT_EXIST = "应用不存在";
    public static final int MAX_PERCENT = 10000;
    public static final int MIN_PERCENT = -99;

    @Autowired
    private AppService               appService;

    @Autowired
    private TemplateQuotaManager     templateQuotaManager;

    @Autowired
    private TemplatePhyService       templatePhyService;

    @Autowired
    private ESIndexService           esIndexService;

    @Autowired
    private TemplateLogicManager     templateLogicManager;

    @Autowired
    private ClusterLogicManager      clusterLogicManager;

    @Autowired
    private TemplatePipelineManager templatePipelineManager;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取索引列表", notes = "包含权限、集群信息、权限信息；")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID，不会过滤索引，会影响权限信息", required = true) })
    public Result<List<ConsoleTemplateVO>> getConsoleTemplates(@RequestParam(value = "appId", required = false) Integer appId,
                                                               @RequestParam(value = "dataCenter", required = false, defaultValue = "") String dataCenter) {
        App app = appService.getAppById(appId);
        if (null == app) {
            return Result.buildNotExist(APP_IS_NOT_EXIST);
        }
        return Result.buildSucc(templateLogicManager.getConsoleTemplatesVOS(appId));
    }

    @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取索引详细信息接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<ConsoleTemplateDetailVO> getConsoleTemplateDetail(HttpServletRequest request,
                                                                    @RequestParam("logicId") Integer logicId) {
        IndexTemplateInfoWithCluster indexTemplateLogicWithCluster = indexTemplateInfoService
            .getLogicTemplateWithCluster(logicId);

        if (null == indexTemplateLogicWithCluster) {
            return Result.buildFail("模板对应资源不存在!");
        }

        ConsoleTemplateDetailVO consoleTemplateDetail = ConvertUtil.obj2Obj(indexTemplateLogicWithCluster,
            ConsoleTemplateDetailVO.class);

        consoleTemplateDetail.setCyclicalRoll(indexTemplateLogicWithCluster.getExpression().endsWith("*"));
        consoleTemplateDetail
            .setCluster(templateLogicManager.jointCluster(indexTemplateLogicWithCluster.getLogicClusters()));

        // 仅对有一个逻辑集群的情况设置集群类型与等级
        if (indexTemplateLogicWithCluster.getLogicClusters().size() == 1) {
            consoleTemplateDetail.setClusterType(indexTemplateLogicWithCluster.getLogicClusters().get(0).getType());
            consoleTemplateDetail.setClusterLevel(indexTemplateLogicWithCluster.getLogicClusters().get(0).getLevel());
        }
        consoleTemplateDetail.setAppName(getAppName(indexTemplateLogicWithCluster.getAppId()));
        consoleTemplateDetail.setIndices(getLogicTemplateIndices(logicId));

        Result<Void> checkAuthResult = checkAppAuth(logicId, HttpRequestUtils.getAppId(request));
        consoleTemplateDetail.setEditable(checkAuthResult.success());
        // 获取indexRollover功能开启状态
        consoleTemplateDetail.setDisableIndexRollover(indexTemplateInfoService.getTemplateConfig(logicId).getDisableIndexRollover());

        return Result.buildSucc(consoleTemplateDetail);
    }

    @PutMapping("/update")
    @ResponseBody
    @ApiOperation(value = "用户编辑模板接口", notes = "支持修改数据类型、责任人、备注")
    public Result<Void> modifyConsoleTemplate(HttpServletRequest request,
                                        @RequestBody ConsoleTemplateUpdateDTO templateLogicDTO) throws AdminOperateException {
        return templateLogicManager.editTemplate(ConvertUtil.obj2Obj(templateLogicDTO, IndexTemplateInfoDTO.class),
            HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/capacity")
    @ResponseBody
    @ApiOperation(value = "获取索引配额信息接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<ConsoleTemplateCapacityVO> getLogicTemplateCapacity(@RequestParam("logicId") Integer logicId) {
        IndexTemplateInfo templateLogic = indexTemplateInfoService.getLogicTemplateById(logicId);
        if (templateLogic == null) {
            return Result.buildParamIllegal(INDEX_NOT_EXISTS_TIPS);
        }

        ConsoleTemplateCapacityVO templateCapacityVO = ConvertUtil.obj2Obj(templateLogic,
            ConsoleTemplateCapacityVO.class);
        templateCapacityVO.setCyclicalRoll(templateLogic.getExpression().endsWith("*"));
        templateCapacityVO.setTopUsage(getQuotaUsage(templateLogic.getId()));
        templateCapacityVO.setCurrentUsage(fetchLogicTemplateCurrentUsage(logicId));
        return Result.buildSucc(templateCapacityVO);
    }

    @GetMapping("/clearInfo")
    @ResponseBody
    @ApiOperation(value = "获取索引清理信息接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<ConsoleTemplateClearVO> getLogicTemplateClearInfo(@RequestParam("logicId") Integer logicId) {
        IndexTemplateInfoWithPhyTemplates templateLogicWithPhysical = indexTemplateInfoService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            return Result.buildParamIllegal(INDEX_NOT_EXISTS_TIPS);
        }

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildParamIllegal("索引没有部署");
        }

        ConsoleTemplateClearVO consoleTemplateClearVO = new ConsoleTemplateClearVO();
        consoleTemplateClearVO.setLogicId(templateLogicWithPhysical.getId());
        consoleTemplateClearVO.setName(templateLogicWithPhysical.getName());
        consoleTemplateClearVO
            .setIndices(templatePhyService.getMatchNoVersionIndexNames(templateLogicWithPhysical.getAnyOne().getId()));
        consoleTemplateClearVO.setAccessApps(
            ConvertUtil.list2List(templateLogicManager.getLogicTemplateAppAccess(logicId), ConsoleAppVO.class));

        return Result.buildSucc(consoleTemplateClearVO);
    }

    @PutMapping("/clearInfo")
    @ResponseBody
    @ApiOperation(value = "清理索引信息接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-APP-ID", value = "应用ID", required = true) })
    public Result<Void> clearLogicTemplateIndices(HttpServletRequest request,
                                            @RequestBody ConsoleTemplateClearDTO clearDTO) throws ESOperateException {
        Result<Void> checkAuthResult = checkAppAuth(clearDTO.getLogicId(), HttpRequestUtils.getAppId(request));
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }

        return clusterLogicManager.clearIndices(clearDTO, HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/deleteInfo")
    @ResponseBody
    @ApiOperation(value = "获取将要索引下线信息接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<ConsoleTemplateDeleteVO> getLogicTemplateDeleteInfo(@RequestParam("logicId") Integer logicId) {
        //与上清理索引信息接口实现合并
        IndexTemplateInfoWithPhyTemplates templateLogicWithPhysical = indexTemplateInfoService
            .getLogicTemplateWithPhysicalsById(logicId);
        if (templateLogicWithPhysical == null) {
            return Result.buildParamIllegal(INDEX_NOT_EXISTS_TIPS);
        }

        ConsoleTemplateDeleteVO consoleTemplateDeleteVO = new ConsoleTemplateDeleteVO();
        consoleTemplateDeleteVO.setLogicId(templateLogicWithPhysical.getId());
        consoleTemplateDeleteVO.setName(templateLogicWithPhysical.getName());

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildParamIllegal("索引没有部署");
        }

        consoleTemplateDeleteVO.setAccessApps(
            ConvertUtil.list2List(templateLogicManager.getLogicTemplateAppAccess(logicId), ConsoleAppVO.class));

        return Result.buildSucc(consoleTemplateDeleteVO);
    }

    @DeleteMapping("/deleteInfo")
    @ResponseBody
    @ApiOperation(value = "下线索引信息接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-APP-ID", value = "应用ID", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<Void> deleteTemplate(HttpServletRequest request,
                                 @RequestParam("logicId") Integer logicId) throws AdminOperateException {
        Result<Void> checkAuthResult = checkAppAuth(logicId, HttpRequestUtils.getAppId(request));
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }
        return templateLogicManager.delTemplate(logicId, HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/indices/list")
    @ResponseBody
    @ApiOperation(value = "获取App Id所有模板的索引" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "应用ID", required = true) })
    public Result<List<Tuple<String, String>>> getLogicTemplatesByAppId(HttpServletRequest request,
                                                                        @RequestParam("appId") Integer appId) {

        App app = appService.getAppById(appId);
        if (null == app) {
            return Result.buildNotExist("应用不存在");
        }

        return indexTemplateInfoService.getLogicTemplatesByAppId(appId);
    }

    @GetMapping("/cyclicalRoll")
    @ResponseBody
    @ApiOperation(value = "获取索引分区信息" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<List<TemplateCyclicalRollInfoVO>> getCyclicalRollInfo(HttpServletRequest request,
                                                                        @RequestParam("logicId") Integer logicId) {
        IndexTemplateInfoWithPhyTemplates templateLogicWithPhysical = indexTemplateInfoService
            .getLogicTemplateWithPhysicalsById(logicId);
        if (templateLogicWithPhysical == null) {
            return Result.buildParamIllegal(INDEX_NOT_EXISTS_TIPS);
        }

        List<CatIndexResult> catIndexResults = Lists.newArrayList();

        List<IndexTemplatePhyInfo> physicalMasters = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        for (IndexTemplatePhyInfo physicalMaster : physicalMasters) {
            try {
                catIndexResults.addAll(esIndexService.syncCatIndexByExpression(physicalMaster.getCluster(),
                    physicalMaster.getExpression()));
            } catch (Exception e) {
                LOGGER.warn("class=ConsoleTemplateController||method=TemplateCyclicalRollInfoVO||logicId={}||errMsg={}", logicId, e.getMessage(), e);
            }
        }

        return Result.buildSucc(ConvertUtil.list2List(catIndexResults, TemplateCyclicalRollInfoVO.class));
    }

    /**
     * 获取模板当前限流值
     */
    @GetMapping(path = "/rateLimit")
    @ResponseBody
    @ApiOperation(value = "获取模板当前限流值接口" )
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true)})
    public Result<ConsoleTemplateRateLimitVO> getTemplateRateLimit(@RequestParam("logicId") Integer logicId) throws Exception {    // 一个逻辑模板可能有master slave两种，限流查看时，默认查看master即可
        List<IndexTemplatePhyInfo> indexTemplatePhysicalInfo = templatePhyService.getTemplateByLogicId(logicId);
        ConsoleTemplateRateLimitVO consoleTemplateRateLimitVO = new ConsoleTemplateRateLimitVO();
        IndexTemplatePhyInfo indexTemplatePhysicalMasterInfo = new IndexTemplatePhyInfo();
        for (IndexTemplatePhyInfo item : indexTemplatePhysicalInfo) {
            if (TemplateDeployRoleEnum.MASTER.getCode().equals(item.getRole())) {
                indexTemplatePhysicalMasterInfo = item;
            }
        }
        consoleTemplateRateLimitVO.setRateLimit(templatePipelineManager.getRateLimit(indexTemplatePhysicalMasterInfo));
        return Result.buildSucc(consoleTemplateRateLimitVO);
    }

    /**
     * 更新模板当前限流值，更新时复用存量逻辑，调整时以百分比变更[-99,1000]
     *  @param consoleTemplateRateLimitDTO 索引模板限流调整表单信息
     *  @return
     *  @throws AdminOperateException
     */
    @PutMapping(path = "/rateLimit")
    @ResponseBody
    @ApiOperation(value = "更新模板当前限流值接口" )
    public Result updateTemplateRateLimit(@RequestBody ConsoleTemplateRateLimitDTO consoleTemplateRateLimitDTO) {
        // 判断调整比例是否在区间内
        int percent = (int) Math.ceil(100.0 * (consoleTemplateRateLimitDTO.getAdjustRateLimit() - consoleTemplateRateLimitDTO.getCurRateLimit()) / consoleTemplateRateLimitDTO.getCurRateLimit());
        if (percent < MIN_PERCENT || percent > MAX_PERCENT) {
            return Result.buildFail("限流调整值变化太大，一次调整比例在100倍以内");
        }
        try {
            return indexTemplateInfoService.updateTemplateWriteRateLimit(consoleTemplateRateLimitDTO);
        } catch (ESOperateException e) {
            LOGGER.info("限流调整失败", e);
            return Result.buildFail("限流调整失败！");
        }
    }

    /**
     * 获取逻辑模板使用率
     *
     * @param logicId 逻辑模板ID
     * @return
     */
    private QuotaUsage getQuotaUsage(Integer logicId) {
        LogicTemplateQuotaUsage templateQuotaUsage = templateQuotaManager.getLogicTemplateQuotaUsage(logicId,
                7 * AdminConstant.MILLIS_PER_DAY);
        QuotaUsage topUsage = ConvertUtil.obj2Obj(templateQuotaUsage, QuotaUsage.class);
        topUsage.setQuotaDiskUsage(templateQuotaUsage.getActualDiskG() / templateQuotaUsage.getQuotaDiskG());
        topUsage.setQuotaCpuUsage(templateQuotaUsage.getActualCpuCount() / templateQuotaUsage.getQuotaCpuCount());

        return topUsage;
    }

    /**
     * 获取逻辑模板索引列表
     *
     * @param logicId 逻辑ID
     * @return
     */
    private List<String> getLogicTemplateIndices(Integer logicId) {
        IndexTemplateInfoWithPhyTemplates templateLogicWithPhysical = indexTemplateInfoService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (null != templateLogicWithPhysical && null != templateLogicWithPhysical.getAnyOne()) {
            return templatePhyService.getMatchNoVersionIndexNames(templateLogicWithPhysical.getAnyOne().getId());
        }

        return new ArrayList<>();
    }

    /**
     * 获取App名称
     *
     * @param appId App Id
     * @return
     */
    private String getAppName(Integer appId) {
        App app = appService.getAppById(appId);
        if (app != null) {
            return app.getName();
        }

        return StringUtils.EMPTY;
    }

    /**
     * 获取逻辑模板当前使用率
     *
     * @param logicId 逻辑模板ID
     * @return
     */
    private QuotaUsage fetchLogicTemplateCurrentUsage(Integer logicId) {
        ESTemplateQuotaUsage esTemplateQuotaUsage = templateQuotaManager.getByLogicId(logicId);

        QuotaUsage currentUsage = ConvertUtil.obj2Obj(esTemplateQuotaUsage, QuotaUsage.class);
        if (esTemplateQuotaUsage != null) {
            currentUsage
                .setQuotaDiskUsage(esTemplateQuotaUsage.getActualDiskG() / esTemplateQuotaUsage.getQuotaDiskG());
            currentUsage
                .setQuotaCpuUsage(esTemplateQuotaUsage.getActualCpuCount() / esTemplateQuotaUsage.getQuotaCpuCount());
        }

        return currentUsage;
    }
}
