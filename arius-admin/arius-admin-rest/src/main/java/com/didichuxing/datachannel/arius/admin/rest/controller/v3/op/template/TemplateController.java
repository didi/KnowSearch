package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.TemplatePipelineManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateRateLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateUpdateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateCapacityVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateClearVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateDeleteVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateRateLimitVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateCyclicalRollInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.AmsRemoteException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
@RequestMapping(V3_OP + "/template")
@Api(tags = "Console-用户侧索引模板接口(REST)")
public class TemplateController extends BaseTemplateController {

    private static final ILog        LOGGER = LogFactory.getLog(TemplateController.class);

    private static final String INDEX_NOT_EXISTS_TIPS = "索引不存在";
 
    public static final int MAX_PERCENT = 10000;
    public static final int MIN_PERCENT = -99;

   

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private IndicesManager          indicesManager;

    @Autowired
    private TemplateLogicManager     templateLogicManager;

    @Autowired
    private ClusterLogicManager      clusterLogicManager;

    @Autowired
    private TemplatePipelineManager templatePipelineManager;

    @GetMapping("/{projectId}")
    @ResponseBody
    @ApiOperation(value = "获取索引列表【三方接口】",tags = "【三方接口】", notes = "包含权限、集群信息、权限信息；")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "projectId", value = "应用ID，不会过滤索引，会影响权限信息", required = true) })
    public Result<List<ConsoleTemplateVO>> getConsoleTemplates(@PathVariable(value = "projectId", required = true) Integer projectId) {
       
        return Result.buildSucc(templateLogicManager.getConsoleTemplatesVOS(projectId));
    }

    @GetMapping("/{logicId}")
    @ResponseBody
    @ApiOperation(value = "获取索引详细信息接口【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    @Deprecated
    public Result<ConsoleTemplateDetailVO> getConsoleTemplateDetail(HttpServletRequest request,
                                                                    @PathVariable("logicId") Integer logicId) {
        IndexTemplateWithCluster indexTemplateLogicWithCluster = indexTemplateService
            .getLogicTemplateWithCluster(logicId);

        if (null == indexTemplateLogicWithCluster || CollectionUtils.isEmpty(indexTemplateLogicWithCluster.getLogicClusters())) {
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
        consoleTemplateDetail.setAppName(getAppName(indexTemplateLogicWithCluster.getProjectId()));
        consoleTemplateDetail.setIndices(getLogicTemplateIndices(logicId));

        Result<Void> checkAuthResult = checkProjectAuth(logicId);
        consoleTemplateDetail.setEditable(checkAuthResult.success());
        // 获取indexRollover功能开启状态
        consoleTemplateDetail.setDisableIndexRollover(Optional.ofNullable(indexTemplateService.getTemplateConfig(logicId))
                .map(IndexTemplateConfig::getDisableIndexRollover)
                .orElse(null)

        );

        return Result.buildSucc(consoleTemplateDetail);
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "用户编辑模板接口【三方接口】",tags = "【三方接口】", notes = "支持修改数据类型、责任人、备注")
    public Result<Void> modifyConsoleTemplate(HttpServletRequest request,
                                        @RequestBody ConsoleTemplateUpdateDTO templateLogicDTO) throws AdminOperateException {
        return templateLogicManager.editTemplate(ConvertUtil.obj2Obj(templateLogicDTO, IndexTemplateDTO.class),
            HttpRequestUtil.getOperator(request),HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/capacity")
    @ResponseBody
    @ApiOperation(value = "获取索引配额信息接口【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    @Deprecated
    public Result<ConsoleTemplateCapacityVO> getLogicTemplateCapacity(@RequestParam("logicId") Integer logicId) {
        return Result.buildSucc();
    }

    @GetMapping("/clear-info")
    @ResponseBody
    @ApiOperation(value = "获取索引清理信息接口【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<ConsoleTemplateClearVO> getLogicTemplateClearInfo(@RequestParam("logicId") Integer logicId) throws AmsRemoteException {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
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
            .setIndices(indicesManager.listIndexNameByTemplatePhyId(templateLogicWithPhysical.getMasterPhyTemplate().getId()));
        consoleTemplateClearVO.setAccessApps(templateLogicManager.getLogicTemplateProjectAccess(logicId));

        return Result.buildSucc(consoleTemplateClearVO);
    }

    @PutMapping("/clear-info")
    @ResponseBody
    @ApiOperation(value = "清理索引信息接口【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = HttpRequestUtil.PROJECT_ID, value = "应用ID", required = true) })
    public Result<Void> clearLogicTemplateIndices(HttpServletRequest request,
                                            @RequestBody ConsoleTemplateClearDTO clearDTO) throws ESOperateException {
        Result<Void> checkAuthResult = checkProjectAuth(clearDTO.getLogicId());
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }

        return clusterLogicManager.clearIndices(clearDTO, HttpRequestUtil.getOperator(request));
    }

    @GetMapping("/delete-info/{logicId}")
    @ResponseBody
    @ApiOperation(value = "获取将要索引下线信息接口【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "logicId", value = "索引ID",
            required = true) })
    public Result<ConsoleTemplateDeleteVO> getLogicTemplateDeleteInfo(@PathVariable("logicId") Integer logicId) throws AmsRemoteException {
        //与上清理索引信息接口实现合并
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
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
           templateLogicManager.getLogicTemplateProjectAccess(logicId));

        return Result.buildSucc(consoleTemplateDeleteVO);
    }

    @DeleteMapping("/delete-info/{logicId}")
    @ResponseBody
    @ApiOperation(value = "下线索引信息接口【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "logicId", value = "索引ID",
            required = true) })
    public Result<Void> deleteTemplate(HttpServletRequest request,
                                 @PathVariable("logicId") Integer logicId) throws AdminOperateException {
        Result<Void> checkAuthResult = checkProjectAuth(logicId);
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }
        return templateLogicManager.delTemplate(logicId, HttpRequestUtil.getOperator(request),  HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/indices/{projectId}")
    @ResponseBody
    @ApiOperation(value = "获取project Id所有模板的索引【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "projectId", value = "应用ID",
            required = true) })
    public Result<List<Tuple<String, String>>> getLogicTemplatesByProjectId(HttpServletRequest request,
                                                                            @PathVariable("projectId") Integer projectId) {

      

        return indexTemplateService.listLogicTemplatesByProjectId(projectId);
    }

    @GetMapping("/cyclical-roll")
    @ResponseBody
    @ApiOperation(value = "获取索引分区信息" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<List<TemplateCyclicalRollInfoVO>> getCyclicalRollInfo(HttpServletRequest request,
                                                                        @RequestParam("logicId") Integer logicId) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);
        if (templateLogicWithPhysical == null) {
            return Result.buildParamIllegal(INDEX_NOT_EXISTS_TIPS);
        }

        List<CatIndexResult> catIndexResults = Lists.newArrayList();

        List<IndexTemplatePhy> physicalMasters = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        for (IndexTemplatePhy physicalMaster : physicalMasters) {
            try {
                catIndexResults.addAll(indicesManager.listIndexCatInfoByTemplatePhyId(physicalMaster.getId()));
            } catch (Exception e) {
                LOGGER.warn("class=ConsoleTemplateController||method=TemplateCyclicalRollInfoVO||logicId={}||errMsg={}", logicId, e.getMessage(), e);
            }
        }

        return Result.buildSucc(ConvertUtil.list2List(catIndexResults, TemplateCyclicalRollInfoVO.class));
    }

    /**
     * 获取模板当前限流值
     */
    @GetMapping(path = "/rate-limit")
    @ResponseBody
    @ApiOperation(value = "获取模板当前限流值接口【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true)})
    public Result<ConsoleTemplateRateLimitVO> getTemplateRateLimit(@RequestParam("logicId") Integer logicId)  {    // 一个逻辑模板可能有master slave两种，限流查看时，默认查看master即可
        List<IndexTemplatePhy> indexTemplatePhysicalInfo = indexTemplatePhyService.getTemplateByLogicId(logicId);
        ConsoleTemplateRateLimitVO consoleTemplateRateLimitVO = new ConsoleTemplateRateLimitVO();
        IndexTemplatePhy indexTemplatePhysicalMasterInfo = new IndexTemplatePhy();
        for (IndexTemplatePhy item : indexTemplatePhysicalInfo) {
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
    @PutMapping(path = "/rate-limit")
    @ResponseBody
    @ApiOperation(value = "更新模板当前限流值接口【三方接口】",tags = "【三方接口】" )
    public Result updateTemplateRateLimit(HttpServletRequest request,@RequestBody ConsoleTemplateRateLimitDTO consoleTemplateRateLimitDTO) {
        // 判断调整比例是否在区间内
        int percent = (int) Math.ceil(100.0 * (consoleTemplateRateLimitDTO.getAdjustRateLimit() - consoleTemplateRateLimitDTO.getCurRateLimit()) / consoleTemplateRateLimitDTO.getCurRateLimit());
        if (percent < MIN_PERCENT || percent > MAX_PERCENT) {
            return Result.buildFail("限流调整值变化太大，一次调整比例在100倍以内");
        }
        try {
            return indexTemplateService.updateTemplateWriteRateLimit(consoleTemplateRateLimitDTO,
                    HttpRequestUtil.getOperator(request),HttpRequestUtil.getProjectId(request));
        } catch (ESOperateException e) {
            LOGGER.info("限流调整失败", e);
            return Result.buildFail("限流调整失败！");
        }
    }

    /**
     * 获取逻辑模板索引列表
     *
     * @param logicId 逻辑ID
     * @return
     */
    private List<String> getLogicTemplateIndices(Integer logicId) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (null != templateLogicWithPhysical && null != templateLogicWithPhysical.getMasterPhyTemplate()) {
            return indexTemplatePhyService.getMatchNoVersionIndexNames(templateLogicWithPhysical.getMasterPhyTemplate().getId());
        }

        return new ArrayList<>();
    }

    /**
     * 获取App名称
     *
     * @param projectId projectId
     * @return
     */
    private String getAppName(Integer projectId) {
        ProjectBriefVO projectBriefVO = projectService.getProjectBriefByProjectId(projectId);
        if (projectBriefVO != null) {
            return projectBriefVO.getProjectName();
        }

        return StringUtils.EMPTY;
    }
}