package com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.impl;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.TemplateLogicAliasManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.TemplatePhyAliasManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.ConsoleAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.ConsoleLogicTemplateAliasesDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.*;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectLogicTemplateAuthService;
import com.google.common.collect.Lists;

/**
 * @author zqr
 * @date 2020-09-09
 */
@Service
public class TemplateLogicAliasManagerImpl extends BaseTemplateSrvImpl implements TemplateLogicAliasManager {

    private static final String             OPERATION_FAILED_TIPS = "操作失败，请重试！";

    private static final String             OPERATOR_IS_NULL_TIPS = "操作人为空";

    @Autowired
    private TemplatePhyAliasManager         templatePhyAliasManager;

    @Autowired
    private ESIndexService                  esIndexService;

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;

    /**
     * @return
     */
    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.TEMPLATE_ALIASES;
    }

    /**
     * 获取别名
     * 注意：模板量大, 耗时较久, 会一直占用线程无法释放, 持续请求进来会耗干资源
     * @return list
     */
    @Override
    public List<IndexTemplateAlias> listAlias() {
        return listAlias(indexTemplateService.listAllLogicTemplateWithPhysicals());
    }

    /**
     * 获取别名
     *
     * @return list
     */
    @Override
    public List<IndexTemplateAlias> listAlias(List<IndexTemplateWithPhyTemplates> templateLogicList) {
        List<IndexTemplateAlias> aliases = new ArrayList<>();
        Set<String> clusters = new HashSet<>();
        for (IndexTemplateWithPhyTemplates templateLogicWithPhyTemplates : templateLogicList) {
            if (null != templateLogicWithPhyTemplates && null != templateLogicWithPhyTemplates.getMasterPhyTemplate()
                && StringUtils.isNotBlank(templateLogicWithPhyTemplates.getMasterPhyTemplate().getCluster())) {
                clusters.add(templateLogicWithPhyTemplates.getMasterPhyTemplate().getCluster());
            }
        }

        try {
            Map<String, List<IndexTemplatePhyAlias>> map = templatePhyAliasManager
                .fetchAllTemplateAliases(new ArrayList<>(clusters));
            for (IndexTemplateWithPhyTemplates templateLogic : templateLogicList) {
                final List<IndexTemplatePhyAlias> indexTemplatePhyAliases = Optional.ofNullable(templateLogic)
                    .map(IndexTemplate::getName).filter(map::containsKey).map(map::get).orElse(Lists.newArrayList());
                for (IndexTemplatePhyAlias physicalAlias : indexTemplatePhyAliases) {
                    aliases.add(fetchAlias(templateLogic.getId(), physicalAlias));
                }
            }

        } catch (ESOperateException e) {
            LOGGER.error(
                "class=TemplateLogicAliasesManagerImpl||method=listAlias||msg=esTemplateNotFound||clusters={}",
                clusters);
        }

        return aliases;
    }

    @Override
    public Result<List<IndexTemplatePhyAlias>> fetchTemplateAliasesByLogicId(Integer logicId) {
        Result<IndexTemplatePhy> result = fetchAnyOneLogicTemplateMasterPhysicalTemplate(logicId);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        IndexTemplatePhy indexTemplatePhy = result.getData();

        try {
            return Result.buildSucc(templatePhyAliasManager.fetchTemplateAliases(indexTemplatePhy.getCluster(),
                indexTemplatePhy.getName()));
        } catch (ESOperateException e) {
            LOGGER.warn("class=TemplateLogicAliasesManagerImpl||method=fetchTemplateAliasesByLogicId||"
                        + "msg=failedFetchTemplateAliases||cluster={}||templateName={}",
                indexTemplatePhy.getCluster(), indexTemplatePhy.getName(), e);

            return Result.buildFail("操作失败，请稍后重试：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> createTemplateAliases(Integer logicId, List<ConsoleAliasDTO> aliases) {
        Result<IndexTemplatePhy> result = fetchAnyOneLogicTemplateMasterPhysicalTemplate(logicId);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        IndexTemplatePhy indexTemplatePhy = result.getData();
        if (!isTemplateSrvOpen(indexTemplatePhy.getLogicId())) {
            return Result.buildFail(indexTemplatePhy.getName() + "没有开启" + templateServiceName());
        }

        try {
            if (templatePhyAliasManager.batchCreateTemplateAliases(indexTemplatePhy.getCluster(),
                indexTemplatePhy.getName(), convertAliases(aliases))) {
                return Result.buildSucc();
            }
        } catch (ESOperateException e) {
            return Result.buildFail(e.getMessage());
        }

        return Result.buildFail(OPERATION_FAILED_TIPS);
    }

    @Override
    public Result<Void> modifyTemplateAliases(Integer logicId, List<ConsoleAliasDTO> aliases) {
        Result<IndexTemplatePhy> result = fetchAnyOneLogicTemplateMasterPhysicalTemplate(logicId);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        IndexTemplatePhy indexTemplatePhy = result.getData();
        if (!isTemplateSrvOpen(indexTemplatePhy.getLogicId())) {
            return Result.buildFail(indexTemplatePhy.getName() + "没有开启" + templateServiceName());
        }

        try {
            if (templatePhyAliasManager.modifyTemplateAliases(indexTemplatePhy.getCluster(), indexTemplatePhy.getName(),
                convertAliases(aliases))) {
                return Result.buildSucc();
            }
        } catch (ESOperateException e) {
            return Result.buildFail(e.getMessage());
        }

        return Result.buildFail(OPERATION_FAILED_TIPS);

    }

    /**
     * 删除模板别名列表
     * @param logicId 逻辑模板ID
     * @param aliases 别名列表
     * @return
     */
    @Override
    public Result<Void> deleteTemplateAliases(Integer logicId, List<String> aliases) {
        Result<IndexTemplatePhy> result = fetchAnyOneLogicTemplateMasterPhysicalTemplate(logicId);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        IndexTemplatePhy indexTemplatePhy = result.getData();

        if (!isTemplateSrvOpen(indexTemplatePhy.getLogicId())) {
            return Result.buildFail(indexTemplatePhy.getName() + "没有开启" + templateServiceName());
        }

        try {
            if (templatePhyAliasManager.deleteTemplateAliases(indexTemplatePhy.getCluster(), indexTemplatePhy.getName(),
                aliases)) {
                return Result.buildSucc();
            }
        } catch (ESOperateException e) {
            return Result.buildFail(e.getMessage());
        }

        return Result.buildFail(OPERATION_FAILED_TIPS);
    }

    @Override
    public List<IndexTemplateAlias> getAliasesById(Integer logicId) {
        List<IndexTemplateAlias> templateAliases = new ArrayList<>();

        Result<List<IndexTemplatePhyAlias>> result = fetchTemplateAliasesByLogicId(logicId);
        if (result.success()) {
            List<IndexTemplatePhyAlias> aliases = result.getData();
            for (IndexTemplatePhyAlias physicalAlias : aliases) {
                templateAliases.add(fetchAlias(logicId, physicalAlias));
            }
        }

        return templateAliases;

    }

    @Override
    public Result<List<IndexTemplatePhyAlias>> getAliases(Integer logicId) {
        return fetchTemplateAliasesByLogicId(logicId);
    }

    @Override
    public Result<Void> createAliases(ConsoleLogicTemplateAliasesDTO aliases, String operator) {
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal(OPERATOR_IS_NULL_TIPS);
        }

        if (aliases == null || CollectionUtils.isEmpty(aliases.getAliases())) {
            return Result.buildParamIllegal("别名信息非法");
        }

        Result<Void> operationResult = createTemplateAliases(aliases.getLogicId(), aliases.getAliases());
        if (operationResult.success()) {
            IndexTemplate logicTemplateById = indexTemplateService.getLogicTemplateById(aliases.getLogicId());
            operateRecordService.saveOperateRecordWithManualTrigger(String.format("index:【%s】创建别名：【%s】", Optional.ofNullable(logicTemplateById.getName()).orElse(""), aliases.getAliases()), operator, AuthConstant.SUPER_PROJECT_ID,
                    aliases.getLogicId(), OperateTypeEnum.INDEX_MANAGEMENT_ALIAS_MODIFY, logicTemplateById.getProjectId());
        }

        return operationResult;

    }

    @Override
    public Result<Void> modifyAliases(ConsoleLogicTemplateAliasesDTO aliases, String operator) {
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal(OPERATOR_IS_NULL_TIPS);
        }

        if (aliases == null || CollectionUtils.isEmpty(aliases.getAliases())) {
            return Result.buildParamIllegal("别名信息非法");
        }

        Result<Void> operationResult = modifyTemplateAliases(aliases.getLogicId(), aliases.getAliases());
        if (operationResult.success()) {
            IndexTemplate logicTemplateById = indexTemplateService.getLogicTemplateById(aliases.getLogicId());
            operateRecordService
                .save(new OperateRecord.Builder().operationTypeEnum(OperateTypeEnum.INDEX_MANAGEMENT_ALIAS_MODIFY)
                    .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).bizId(aliases.getLogicId()).content(String.format("index:【%s】设置别名：【%s】", Optional.ofNullable(logicTemplateById.getName()).orElse(""), aliases.getAliases()))
                    .userOperation(operator).operateProject(projectService.getProjectBriefByProjectId(logicTemplateById.getProjectId())).build());
        }

        return operationResult;
    }

    @Override
    public Result<List<Tuple<String, String>>> getAllTemplateAliasesByProjectId(Integer projectId) {
        List<Tuple<String, String>> aliases = new ArrayList<>();

        List<ProjectTemplateAuth> projectTemplateAuths = projectLogicTemplateAuthService
            .getTemplateAuthsByProjectId(projectId);
        if (CollectionUtils.isEmpty(projectTemplateAuths)) {
            return Result.build(true);
        }

        projectTemplateAuths.parallelStream().forEach(appTemplateAuth -> {
            IndexTemplateWithPhyTemplates logicWithPhysical = this.indexTemplateService
                .getLogicTemplateWithPhysicalsById(appTemplateAuth.getTemplateId());

            if (null != logicWithPhysical && logicWithPhysical.hasPhysicals()) {
                IndexTemplatePhy indexTemplatePhysicalInfo = logicWithPhysical.getPhysicals().get(0);

                if (!isTemplateSrvOpen(indexTemplatePhysicalInfo.getLogicId())) {
                    return;
                }

                aliases.addAll(esIndexService.syncGetIndexAliasesByExpression(indexTemplatePhysicalInfo.getCluster(),
                    indexTemplatePhysicalInfo.getExpression()));
            }
        });

        return Result.buildSucc(aliases);
    }

    /**************************************** private method ****************************************************/
    /**
     * 获取逻辑模板Master角色物理模板
     * @param logicId 逻辑模板ID
     * @return
     */
    private Result<IndexTemplatePhy> fetchAnyOneLogicTemplateMasterPhysicalTemplate(Integer logicId) {
        if (logicId == null) {
            return Result.buildNotExist("非法的逻辑ID： " + logicId);
        }

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist("逻辑模板不存在, ID:" + logicId);
        }

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildNotExist("物理模板不存在，ID:" + logicId);
        }

        IndexTemplatePhy indexTemplatePhy = templateLogicWithPhysical.getMasterPhyTemplate();
        if (indexTemplatePhy != null) {
            return Result.buildSucc(indexTemplatePhy);
        }

        return Result.buildNotExist("逻辑模板不存在Master角色物理模板，ID:" + logicId);
    }

    /**
     * 解析生成别名
     * @param logicId 逻辑模板ID
     * @param alias 别名名称
     * @return
     */
    private IndexTemplateAlias fetchAlias(Integer logicId, IndexTemplatePhyAlias alias) {
        if (alias != null) {
            IndexTemplateAlias templateAlias = new IndexTemplateAlias();
            templateAlias.setName(alias.getAlias());
            templateAlias.setLogicId(logicId);
            return templateAlias;
        }

        return null;
    }

    /**
     * 转换别名列表
     * @param aliasList 别名DTO列表
     * @return
     */
    private List<IndexTemplatePhyAlias> convertAliases(List<ConsoleAliasDTO> aliasList) {
        List<IndexTemplatePhyAlias> aliases = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(aliasList)) {
            for (ConsoleAliasDTO aliasDTO : aliasList) {
                aliases.add(convertAlias(aliasDTO));
            }
        }
        return aliases;
    }

    /**
     * 转换别名
     * @param aliasDTO 别名DTO
     * @return
     */
    private IndexTemplatePhyAlias convertAlias(ConsoleAliasDTO aliasDTO) {
        if (aliasDTO != null) {
            IndexTemplatePhyAlias alias = new IndexTemplatePhyAlias();
            alias.setAlias(aliasDTO.getAlias());
            alias.setFilter(aliasDTO.getFilter());
            return alias;
        }
        return null;
    }
}