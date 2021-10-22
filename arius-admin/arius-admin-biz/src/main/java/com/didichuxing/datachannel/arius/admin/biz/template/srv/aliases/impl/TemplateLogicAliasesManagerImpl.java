package com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.TemplateLogicAliasesManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.TemplatePhyAliasesManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias.ConsoleAliasDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias.ConsoleLogicTemplateAliasesDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias.ConsoleLogicTemplateDeleteAliasesDTO;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.*;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateDTOConvertUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.EDIT_TEMPLATE_ALIASES;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_ALIASES;

/**
 * @author zqr
 * @date 2020-09-09
 */
@Service
public class TemplateLogicAliasesManagerImpl extends BaseTemplateSrv implements TemplateLogicAliasesManager {

    @Autowired
    private TemplatePhyAliasesManager templatePhyAliasesManager;

    @Autowired
    private ESIndexService              esIndexService;

    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_ALIASES;
    }

    @Override
    public Result getAliases(Integer logicId) {
        return fetchTemplateAliasesByLogicId(logicId);
    }

    /**
     * 获取别名
     *
     * @return list
     */
    @Override
    public List<IndexTemplateAlias> listAlias() {
        List<IndexTemplateAlias> aliases = new ArrayList<>();
        List<IndexTemplateLogic> templateLogicList = templateLogicService.getAllLogicTemplates();
        //TODO:lyn 获取别名优化
        for (IndexTemplateLogic templateLogic : templateLogicList) {
            aliases.addAll(getAliasesById(templateLogic.getId()));
        }

        return aliases;
    }

    @Override
    public List<IndexTemplateAlias> getAliasesById(Integer logicId) {
        List<IndexTemplateAlias> templateAliases = new ArrayList<>();

        Result result = fetchTemplateAliasesByLogicId(logicId);
        if (result.success()) {
            List<IndexTemplatePhyAlias> aliases = (List<IndexTemplatePhyAlias>) result.getData();
            for (IndexTemplatePhyAlias physicalAlias : aliases) {
                templateAliases.add(fetchAlias(logicId, physicalAlias));
            }
        }

        return templateAliases;
    }

    @Override
    public Result createAliases(ConsoleLogicTemplateAliasesDTO aliases, String operator) {
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人为空");
        }

        if (aliases == null || CollectionUtils.isEmpty(aliases.getAliases())) {
            return Result.buildParamIllegal("别名信息非法");
        }

        Result operationResult = createTemplateAliases(aliases.getLogicId(), aliases.getAliases());
        if (operationResult.success()) {
            operateRecordService.save(TEMPLATE, EDIT_TEMPLATE_ALIASES, aliases.getLogicId(), "-", operator);
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
    public Result modifyAliases(ConsoleLogicTemplateAliasesDTO aliases, String operator) {
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人为空");
        }

        if (aliases == null || CollectionUtils.isEmpty(aliases.getAliases())) {
            return Result.buildParamIllegal("别名信息非法");
        }

        Result operationResult = modifyTemplateAliases(aliases.getLogicId(), aliases.getAliases());
        if (operationResult.success()) {
            operateRecordService.save(TEMPLATE, EDIT_TEMPLATE_ALIASES, aliases.getLogicId(), "-", operator);
        }

        return operationResult;
    }

    @Override
    public Result deleteTemplateAliases(ConsoleLogicTemplateDeleteAliasesDTO deleteAliasesDTO, String operator) {
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人为空");
        }

        if (deleteAliasesDTO == null || CollectionUtils.isEmpty(deleteAliasesDTO.getAliases())) {
            return Result.buildParamIllegal("待删除索引别名列表");
        }

        Result operationResult = deleteTemplateAliases(deleteAliasesDTO.getLogicId(), deleteAliasesDTO.getAliases());

        if (operationResult.success()) {
            operateRecordService.save(TEMPLATE, EDIT_TEMPLATE_ALIASES, deleteAliasesDTO.getLogicId(), "-", operator);
        }

        return operationResult;
    }

    @Override
    public Result fetchTemplateAliasesByLogicId(Integer logicId) {
        Result result = fetchAnyOneLogicTemplateMasterPhysicalTemplate(logicId);
        if (result.failed()) {
            return result;
        }

        IndexTemplatePhy indexTemplatePhy = (IndexTemplatePhy) result.getData();

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
    public Result createTemplateAliases(Integer logicId, List<ConsoleAliasDTO> aliases) {
        Result result = fetchAnyOneLogicTemplateMasterPhysicalTemplate(logicId);
        if (result.failed()) {
            return result;
        }

        IndexTemplatePhy indexTemplatePhy = (IndexTemplatePhy) result.getData();
        if (!isTemplateSrvOpen(indexTemplatePhy.getCluster())) {
            return Result.buildFail(indexTemplatePhy.getCluster() + "没有开启" + templateServiceName());
        }

        try {
            if (templatePhyAliasesManager.batchCreateTemplateAliases(indexTemplatePhy.getCluster(),
                indexTemplatePhy.getName(), TemplateDTOConvertUtils.convertAliases(aliases))) {
                return Result.buildSucc();
            }
        } catch (ESOperateException e) {
            return Result.buildFail(e.getMessage());
        }

        return Result.buildFail("操作失败，请重试！");
    }

    @Override
    public Result modifyTemplateAliases(Integer logicId, List<ConsoleAliasDTO> aliases) {
        Result result = fetchAnyOneLogicTemplateMasterPhysicalTemplate(logicId);
        if (result.failed()) {
            return result;
        }

        IndexTemplatePhy indexTemplatePhy = (IndexTemplatePhy) result.getData();
        if (!isTemplateSrvOpen(indexTemplatePhy.getCluster())) {
            return Result.buildFail(indexTemplatePhy.getCluster() + "没有开启" + templateServiceName());
        }

        try {
            if (templatePhyAliasesManager.modifyTemplateAliases(indexTemplatePhy.getCluster(),
                indexTemplatePhy.getName(), TemplateDTOConvertUtils.convertAliases(aliases))) {
                return Result.buildSucc();
            }
        } catch (ESOperateException e) {
            return Result.buildFail(e.getMessage());
        }

        return Result.buildFail("操作失败，请重试！");

    }

    /**
     * 删除模板别名列表
     * @param logicId 逻辑模板ID
     * @param aliases 别名列表
     * @return
     */
    @Override
    public Result deleteTemplateAliases(Integer logicId, List<String> aliases) {
        Result result = fetchAnyOneLogicTemplateMasterPhysicalTemplate(logicId);
        if (result.failed()) {
            return result;
        }

        IndexTemplatePhy indexTemplatePhy = (IndexTemplatePhy) result.getData();

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

        return Result.buildFail("操作失败，请重试！");
    }

    /**
     * 获取模板具体的别名
     *
     * @param appId appId
     */
    @Override
    public Result<List<Tuple<String/*index*/, String/*aliases*/>>> getAllTemplateAliasesByAppid(Integer appId) {
        List<Tuple<String, String>> aliases = new ArrayList<>();

        List<AppTemplateAuth> appTemplateAuths = appLogicTemplateAuthService.getTemplateAuthsByAppId(appId);
        if (CollectionUtils.isEmpty(appTemplateAuths)) {
            return Result.build(true);
        }

        appTemplateAuths.parallelStream().forEach(appTemplateAuth -> {
            IndexTemplateLogicWithPhyTemplates logicWithPhysical = this.templateLogicService
                .getLogicTemplateWithPhysicalsById(Integer.valueOf(appTemplateAuth.getTemplateId()));

            if (null != logicWithPhysical && logicWithPhysical.hasPhysicals()) {
                IndexTemplatePhy indexTemplatePhysical = logicWithPhysical.getPhysicals().get(0);

                if (!isTemplateSrvOpen(indexTemplatePhysical.getCluster())) {
                    return;
                }

                aliases.addAll(esIndexService.syncGetIndexAliasesByExpression(indexTemplatePhysical.getCluster(),
                    indexTemplatePhysical.getExpression()));
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
    private Result fetchAnyOneLogicTemplateMasterPhysicalTemplate(Integer logicId) {
        if (logicId == null) {
            return Result.buildNotExist("非法的逻辑ID： " + logicId);
        }

        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
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
}
