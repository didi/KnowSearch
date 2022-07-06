package com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.TemplateLogicAliasesManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.TemplatePhyAliasesManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.ConsoleAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.ConsoleLogicTemplateAliasesDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.ConsoleLogicTemplateDeleteAliasesDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateAlias;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyAlias;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.NewTemplateSrvEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectLogicTemplateAuthService;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zqr
 * @date 2020-09-09
 */
@Service
public class TemplateLogicAliasesManagerImpl extends BaseTemplateSrvImpl implements TemplateLogicAliasesManager {

    private static final String OPERATION_FAILED_TIPS = "操作失败，请重试！";

    private static final String OPERATOR_IS_NULL_TIPS = "操作人为空";

    @Autowired
    private TemplatePhyAliasesManager templatePhyAliasesManager;

    @Autowired
    private ESIndexService              esIndexService;

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;
    
    /**
     * @return
     */
    @Override
    public NewTemplateSrvEnum templateSrv() {
        return NewTemplateSrvEnum.TEMPLATE_ALIASES;
    }
    
    @Override
    public Result<List<IndexTemplatePhyAlias>> getAliases(Integer logicId) {
        return fetchTemplateAliasesByLogicId(logicId);
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
            Map<String, List<IndexTemplatePhyAlias>> map =  templatePhyAliasesManager.fetchAllTemplateAliases(new ArrayList<>(clusters));
            for (IndexTemplateWithPhyTemplates templateLogic : templateLogicList) {
                final List<IndexTemplatePhyAlias> indexTemplatePhyAliases = Optional.ofNullable(templateLogic)
                        .map(IndexTemplate::getName)
                        .filter(map::containsKey)
                        .map(map::get).orElse(Lists.newArrayList());
                for (IndexTemplatePhyAlias physicalAlias :indexTemplatePhyAliases) {
                    aliases.add(fetchAlias(templateLogic.getId(), physicalAlias));
                }
            }

        } catch (ESOperateException e) {
            LOGGER.info("class=TemplateLogicAliasesManagerImpl||method=listAlias||"
                            + "msg=esTemplateNotFound||clusters={}",
                    clusters);
        }

        return aliases;
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
    public Result<Void> createAliases(ConsoleLogicTemplateAliasesDTO aliases, String operator) {
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal(OPERATOR_IS_NULL_TIPS);
        }

        if (aliases == null || CollectionUtils.isEmpty(aliases.getAliases())) {
            return Result.buildParamIllegal("别名信息非法");
        }

        Result<Void> operationResult = createTemplateAliases(aliases.getLogicId(), aliases.getAliases());
        if (operationResult.success()) {
            operateRecordService.save(new OperateRecord.Builder()
                    
                            .operationTypeEnum(OperateTypeEnum.INDEX_MANAGEMENT_ALIAS_MODIFY)
                            .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                            .bizId(aliases.getLogicId())
                            .content("别名创建")
                            .userOperation(operator)
                    
                    .build());
        }

        return operationResult;
    }

    /**
     * 更新索引别名
     * @param aliases 别名列表详情
     * @param operator 操作者
     * @return
     */
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
             operateRecordService.save(new OperateRecord.Builder()
                            .operationTypeEnum(OperateTypeEnum.INDEX_MANAGEMENT_ALIAS_MODIFY)
                            .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                            .bizId(aliases.getLogicId())
                             .content("别名修改")
                            .userOperation(operator)
                    .build());
        }

        return operationResult;
    }

    @Override
    public Result<Void> deleteTemplateAliases(ConsoleLogicTemplateDeleteAliasesDTO deleteAliasesDTO, String operator) {
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal(OPERATOR_IS_NULL_TIPS);
        }

        if (deleteAliasesDTO == null || CollectionUtils.isEmpty(deleteAliasesDTO.getAliases())) {
            return Result.buildParamIllegal("待删除索引别名列表");
        }

        Result<Void> operationResult = deleteTemplateAliases(deleteAliasesDTO.getLogicId(), deleteAliasesDTO.getAliases());

        if (operationResult.success()) {
                  operateRecordService.save(new OperateRecord.Builder()
                    
                            .operationTypeEnum(OperateTypeEnum.INDEX_MANAGEMENT_ALIAS_MODIFY)
                          .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).bizId(deleteAliasesDTO.getLogicId())
                          .content("删除索引别名").userOperation(operator)
                    
                    .build());
        }

        return operationResult;
    }

    @Override
    public Result<List<IndexTemplatePhyAlias>> fetchTemplateAliasesByLogicId(Integer logicId) {
        Result<IndexTemplatePhy> result = fetchAnyOneLogicTemplateMasterPhysicalTemplate(logicId);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        IndexTemplatePhy indexTemplatePhy = result.getData();

        try {
            return Result.buildSucc( templatePhyAliasesManager.fetchTemplateAliases(indexTemplatePhy.getCluster(),
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
        if (!isTemplateSrvOpen(indexTemplatePhy.getCluster())) {
            return Result.buildFail(indexTemplatePhy.getCluster() + "没有开启" + templateServiceName());
        }

        try {
            if (templatePhyAliasesManager.batchCreateTemplateAliases(indexTemplatePhy.getCluster(),
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
        if (!isTemplateSrvOpen(indexTemplatePhy.getCluster())) {
            return Result.buildFail(indexTemplatePhy.getCluster() + "没有开启" + templateServiceName());
        }

        try {
            if (templatePhyAliasesManager.modifyTemplateAliases(indexTemplatePhy.getCluster(),
                indexTemplatePhy.getName(), convertAliases(aliases))) {
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

        if (!isTemplateSrvOpen(indexTemplatePhy.getCluster())) {
            return Result.buildFail(indexTemplatePhy.getCluster() + "没有开启" + templateServiceName());
        }

        try {
            if (templatePhyAliasesManager.deleteTemplateAliases(indexTemplatePhy.getCluster(),
                indexTemplatePhy.getName(), aliases)) {
                return Result.buildSucc();
            }
        } catch (ESOperateException e) {
            return Result.buildFail(e.getMessage());
        }

        return Result.buildFail(OPERATION_FAILED_TIPS);
    }

    /**
     * 获取模板具体的别名
     *
     * @param projectId projectId
     */
    @Override
    public Result<List<Tuple<String/*index*/, String/*aliases*/>>> getAllTemplateAliasesByProjectId(Integer projectId) {
        List<Tuple<String, String>> aliases = new ArrayList<>();

        List<ProjectTemplateAuth> projectTemplateAuths = projectLogicTemplateAuthService.getTemplateAuthsByProjectId(
                projectId);
        if (CollectionUtils.isEmpty(projectTemplateAuths)) {
            return Result.build(true);
        }

        projectTemplateAuths.parallelStream().forEach(appTemplateAuth -> {
            IndexTemplateWithPhyTemplates logicWithPhysical = this.indexTemplateService
                .getLogicTemplateWithPhysicalsById(appTemplateAuth.getTemplateId());

            if (null != logicWithPhysical && logicWithPhysical.hasPhysicals()) {
                IndexTemplatePhy indexTemplatePhysicalInfo = logicWithPhysical.getPhysicals().get(0);

                if (!isTemplateSrvOpen(indexTemplatePhysicalInfo.getCluster())) {
                    return;
                }

                aliases.addAll(esIndexService.syncGetIndexAliasesByExpression(indexTemplatePhysicalInfo.getCluster(),
                    indexTemplatePhysicalInfo.getExpression()));
            }
        });

        return Result.buildSucc(aliases.stream().collect(Collectors.toList()));
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
     * @param aliasDTOS 别名DTO列表
     * @return
     */
    private List<IndexTemplatePhyAlias> convertAliases(List<ConsoleAliasDTO> aliasDTOS) {
        List<IndexTemplatePhyAlias> aliases = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(aliasDTOS)) {
            for (ConsoleAliasDTO aliasDTO: aliasDTOS) {
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