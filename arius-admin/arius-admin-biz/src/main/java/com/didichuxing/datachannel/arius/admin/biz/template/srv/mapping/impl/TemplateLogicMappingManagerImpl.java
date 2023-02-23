package com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.DEFAULT_INDEX_MAPPING_TYPE;
import static com.didichuxing.datachannel.arius.admin.common.mapping.AriusTypeProperty.DYNAMIC_TEMPLATES_STR;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplateLogicMappingManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplatePhyMappingManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.MappingOptimizeItem;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateSchemaDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateSchemaOptimizeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template.TemplateMappingOperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.*;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateConfigPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateTypePO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateMappingVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.event.index.ReBuildTomorrowIndexEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.mapping.*;
import com.didichuxing.datachannel.arius.admin.common.util.AriusIndexMappingConfigUtils;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.impl.IndexTemplateServiceImpl;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.common.TypeConfig;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.common.TypeDefine;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.common.TypeProperties;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * @author zhonghua
 * @date 2019-06-13
 */
@Service("templateLogicMappingManagerImpl")
public class TemplateLogicMappingManagerImpl extends BaseTemplateSrvImpl implements TemplateLogicMappingManager {

    private static final ILog         LOGGER                            = LogFactory
        .getLog(IndexTemplateServiceImpl.class);

    private static final String       PHYSICAL_TEMPLATE_NOT_EXISTS_TIPS = "物理模板不存在，ID:%d";
    @Autowired
    private TemplatePhyMappingManager templatePhyMappingManager;
    
    @Autowired
    private IndexTemplateService indexTemplateService;
    
 
   


    private static final String       TEXT_STR                          = "text";
    private static final String       TYPE_STR                          = "type";
    private static final String       KEYWORD_STR                       = "keyword";
    private static final String       IK_SMART_SRT                      = "ik_smart";
    private static final String       ANALYZER_STR                      = "analyzer";
    private static final String       DOC_VALUES_STR                    = "doc_values";
    private static final String       INDEX_STR                         = "index";

    /**
     * @return
     */
    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.TEMPLATE_MAPPING;
    }

    /**
     * 查询指定的逻辑模板的Field信息
     *
     * @param logicId 模板id
     * @return 模板信息  不存在返回null
     */
    @Override
    public Result<IndexTemplateWithMapping> getTemplateWithMapping(Integer logicId) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            LOGGER.warn("method=getTemplateWithFieldById||msg=not exit||logicId={}", logicId);
            return Result.buildNotExist("模板不存在");
        }

        IndexTemplateWithMapping templateLogicWithMapping = ConvertUtil.obj2Obj(templateLogicWithPhysical,
            IndexTemplateWithMapping.class);

        if (templateLogicWithPhysical.hasPhysicals()) {
            MappingConfig mergeMappingConfig = null;
            List<IndexTemplatePhy> masterPhysicalTemplates = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
            for (IndexTemplatePhy templatePhysical : masterPhysicalTemplates) {

                Result<MappingConfig> result = templatePhyMappingManager.getMapping(templatePhysical.getCluster(),
                    templatePhysical.getName());
                if (result.failed()) {
                    return Result.buildFrom(result);
                }
                MappingConfig mappingConfig = result.getData();
                if (mergeMappingConfig == null) {
                    mergeMappingConfig = mappingConfig;
                } else {
                    mergeMappingConfig.merge(mappingConfig);
                }
            }

            if (null != mergeMappingConfig) {
                templateLogicWithMapping.setTypeProperties(
                    genAriusTypePropertyList(templateLogicWithMapping, mergeMappingConfig.getMapping()));

                List<Field> fields = null;
                try {
                    fields = convert2Fields(mergeMappingConfig);
                } catch (Exception t) {
                    LOGGER.warn("method=getTemplateWithFieldById||msg=mapping to field error||logicId={}", logicId, t);
                }
                if (fields == null) {
                    fields = new ArrayList<>();
                }
                templateLogicWithMapping.setFields(fields);
            }
        } else {
            LOGGER.warn("method=getTemplateWithFieldById||msg=not deploy||logicId={}", logicId);
        }

        return Result.buildSucc(templateLogicWithMapping);
    }

    @Override
    public Result checkFields(Integer logicId, List<Field> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return Result.buildSucc();
        }

        Result<Void> result = checkFieldInternal(fields);
        if (result.failed()) {
            return result;
        }

        List<IndexTemplatePhy> templatePhysicals = getMasterTemplatePhysicalByLogicId(logicId);

        if (CollectionUtils.isEmpty(templatePhysicals)) {
            return Result.buildFail("can not find template physical, logicId:" + logicId);
        }

        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            Result<MappingConfig> getDiffMappingResult = getDiffMapping(templatePhysical.getCluster(),
                templatePhysical.getName(), fields);
            if (getDiffMappingResult.failed()) {
                return getDiffMappingResult;
            }

            MappingConfig mappingConfig = getDiffMappingResult.getData();

            String cluster = templatePhysical.getCluster();
            String template = templatePhysical.getName();
            String mapping = mappingConfig.toJson().toJSONString();
            Result<Void> checkMappingResult = templatePhyMappingManager.checkMapping(cluster, template, mapping, true);
            if (checkMappingResult.failed()) {
                return checkMappingResult;
            }
        }

        return Result.buildSucc();
    }

    @Override
    public Result<Void> updateFields(Integer logicId, List<Field> fields, Set<String> removeFields, Integer projectId,
                                     String operator) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist(String.format(PHYSICAL_TEMPLATE_NOT_EXISTS_TIPS, logicId));
        }

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildNotExist(String.format(PHYSICAL_TEMPLATE_NOT_EXISTS_TIPS, logicId));
        }

        Result<Void> checkFieldResult = checkFieldInternal(fields);
        if (checkFieldResult.failed()) {
            return checkFieldResult;
        }

        List<IndexTemplatePhy> masterPhysicalTemplates = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        for (IndexTemplatePhy masterTemplatePhysical : masterPhysicalTemplates) {
            if (masterTemplatePhysical == null) {
                return Result.buildFail("can not find template physical, logicId:" + logicId);
            }
            final Result<MappingConfig> beforeResult = templatePhyMappingManager
                .getMapping(masterTemplatePhysical.getCluster(), masterTemplatePhysical.getName());
            JSONObject beforeData = beforeResult.getData().toJson();
            Result<MappingConfig> getDiffResult = getDiffMapping(masterTemplatePhysical.getCluster(),
                masterTemplatePhysical.getName(), fields);
            if (getDiffResult.failed()) {
                return Result.buildFrom(getDiffResult);
            }

            String diffMapping = getDiffResult.getData().toJson().toJSONString();

            Result<Void> editResult = templatePhyMappingManager.updateMappingAndMerge(
                masterTemplatePhysical.getCluster(), masterTemplatePhysical.getName(), diffMapping, removeFields);
            if (editResult.failed()) {
                return Result.buildFrom(editResult);
            }
            final Result<MappingConfig> afterResult = templatePhyMappingManager
                .getMapping(masterTemplatePhysical.getCluster(), masterTemplatePhysical.getName());
            operateRecordService.saveOperateRecordWithManualTrigger(
                    new TemplateMappingOperateRecord(beforeData, afterResult.getData()).toString(),
                    operator, projectId, logicId, OperateTypeEnum.TEMPLATE_MANAGEMENT_EDIT_MAPPING,
                    templateLogicWithPhysical.getProjectId());
        }

        return Result.buildSucc();
    }

    @Override
    public AriusTypeProperty fields2Mapping(List<Field> fields) {
        if (fields == null) {
            fields = Lists.newArrayList();
        }

        MappingConfig mappingConfig = convert2Mapping(AdminConstant.DEFAULT_INDEX_MAPPING_TYPE, fields);
        AriusTypeProperty ariusTypeProperty = new AriusTypeProperty();
        ariusTypeProperty.setTypeName(AdminConstant.DEFAULT_INDEX_MAPPING_TYPE);
        if (CollectionUtils.isNotEmpty(fields)) {
            ariusTypeProperty.setProperties(
                mappingConfig.getMapping().get(AdminConstant.DEFAULT_INDEX_MAPPING_TYPE).getProperties().toJson());
        } else {
            ariusTypeProperty.setProperties(new JSONObject());
        }
        return ariusTypeProperty;
    }



    /**
     * mapping优化
     *
     * @param optimizeDTO dto
     * @param operator    操作人
     * @return result
     */
    @Override
    public Result<Void> modifySchemaOptimize(ConsoleTemplateSchemaOptimizeDTO optimizeDTO, String operator) {

        if (CollectionUtils.isEmpty(optimizeDTO.getItems())) {
            return Result.buildParamIllegal("未选择优化字段");
        }

        List<AriusTypeProperty> typeProperties = buildAriusTypeProperty(optimizeDTO.getItems());

        return updateProperties(optimizeDTO.getLogicId(), typeProperties, operator);
    }

    @Override
    public Result updateMappingForNew(Integer logicId, AriusTypeProperty ariusTypeProperty) throws
                                                                                            AdminOperateException {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist("逻辑模板不存在, ID:" + logicId);
        }

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildNotExist(String.format(PHYSICAL_TEMPLATE_NOT_EXISTS_TIPS, logicId));
        }
        TemplateConfigPO config = indexTemplateService.getTemplateConfigByLogicId(logicId);

        List<IndexTemplatePhy> templatePhysicals = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {

            Result<MappingConfig> mappingConfigResult = AriusIndexMappingConfigUtils
                .parseMappingConfig(ariusTypeProperty.toMappingJSON().toJSONString());
            if (mappingConfigResult.failed()) {
                return mappingConfigResult;
            }

            MappingConfig mappingConfig = mappingConfigResult.getData();

            if (config != null && config.getDisableSourceFlags() != null
                && config.getDisableSourceFlags().booleanValue()) {
                LOGGER.info("method=updateMappingForNew||msg=disableSource||logicId={}", logicId);
                mappingConfig.disableSource();
            } else {
                mappingConfig.enableSource();
            }

            Result<Void> result = templatePhyMappingManager.updateMapping(templatePhysical.getCluster(),
                templatePhysical.getName(), mappingConfig.toJson().toJSONString());

            if (result.failed()) {
                return result;
            }
        }

        return Result.buildSucc();
    }

    @Override
    public Result<Void> updateProperties(Integer logicId, List<AriusTypeProperty> ariusTypePropertyList,
                                         String operator) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist("逻辑模板不存在, ID:" + logicId);
        }

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildNotExist(String.format(PHYSICAL_TEMPLATE_NOT_EXISTS_TIPS, logicId));
        }

        TemplateConfigPO config = indexTemplateService.getTemplateConfigByLogicId(logicId);

        List<IndexTemplatePhy> templatePhysicals = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        boolean isSingleIndex = isSingleIndex(logicId);

        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            Result<MappingConfig> result = templatePhyMappingManager.getMapping(templatePhysical.getCluster(),
                templatePhysical.getName());
            if (result.failed()) {
                return Result.buildFrom(result);
            }

            MappingConfig templateMappingConfig = (result.getData());
            Map<String, TypeConfig> typeConfigMap = templateMappingConfig.getMapping();

            if (typeConfigMap.size() == 0) {
                TypeConfig typeConfig = new TypeConfig();
                typeConfigMap.put(AdminConstant.DEFAULT_INDEX_MAPPING_TYPE, typeConfig);
            }

            // 将对应的type替换
            for (AriusTypeProperty ariusTypePropertyTemp : ariusTypePropertyList) {
                //todo 后续0.3.2下线
                AriusTypeProperty ariusTypeProperty = AriusTypeProperty.buildPropertiesAndDynamicTemplates(ariusTypePropertyTemp);
                //高版本es集群只有一个type
                TypeProperties typeProperties = new TypeProperties(ariusTypeProperty.getProperties());

                for (Map.Entry<String, TypeConfig> entry : typeConfigMap.entrySet()) {
                    if (isSingleIndex && (entry.getValue().getProperties() == null || (isExistMappingChanged(entry.getValue().getProperties().getJsonMap(),
                        typeProperties.getJsonMap())))){
                        return Result.buildFail("非分区模板禁止编辑mapping");
                    }
                    entry.getValue().setProperties(typeProperties);
                    entry.getValue().getNotUsedMap().put(AdminConstant.DEFAULT_DYNAMIC_TEMPLATES_KEY,
                        ariusTypeProperty.getDynamicTemplates());
                }
            }

            if (config != null && config.getDisableSourceFlags()) {
                templateMappingConfig.disableSource();
            } else {
                templateMappingConfig.enableSource();
            }
            final Result<MappingConfig> beforeResult = templatePhyMappingManager
                .getMapping(templatePhysical.getCluster(), templatePhysical.getName());
            JSONObject beforeData = beforeResult.getData().toJson();
            Result<Void> updateMappingResult = templatePhyMappingManager.updateMapping(templatePhysical.getCluster(),
                templatePhysical.getName(), templateMappingConfig.toJson().toJSONString());
            
            if (updateMappingResult.failed()) {
                return Result.buildFrom(updateMappingResult);
            }
            final Result<MappingConfig> afterResult = templatePhyMappingManager
                .getMapping(templatePhysical.getCluster(), templatePhysical.getName());
            operateRecordService.saveOperateRecordWithManualTrigger(
                    new TemplateMappingOperateRecord(beforeData, afterResult.getData()).toString(),
                    operator, templateLogicWithPhysical.getProjectId(), logicId,
                    OperateTypeEnum.TEMPLATE_MANAGEMENT_EDIT_MAPPING, templateLogicWithPhysical.getProjectId());
        }

        return Result.buildSucc();
    }
    
    /**
     * 修改模板schema
     *
     * @param schemaDTO       schema
     * @param operator        操作人
     * @param projectId
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> editMapping(ConsoleTemplateSchemaDTO schemaDTO, String operator,
                                    Integer projectId) throws AdminOperateException {
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人为空");
        }

        if (schemaDTO.getFields() == null && schemaDTO.getTypeProperties() == null) {
            return Result.buildParamIllegal("fields或mapping信息非法");
        }

        if (schemaDTO.getFields() != null && schemaDTO.getTypeProperties() != null) {
            return Result.buildParamIllegal("fields或mapping信息非法");
        }

        if (schemaDTO.getFields() != null && !clusterIsHighVersion(schemaDTO.getLogicId())) {
            return Result.buildParamIllegal("该功能只支持高版本(6.5.1以上)es， 请使用JSON格式");
        }

        Result<Void> saveSpecialFieldResult = saveSpecialField(schemaDTO, operator, projectId);
        if (saveSpecialFieldResult.failed()) {
            return saveSpecialFieldResult;
        }

        // 修改mapping信息.
        if (schemaDTO.getTypeProperties() != null) {
            Result<Void> result = updateProperties(schemaDTO.getLogicId(), schemaDTO.getTypeProperties(), operator);
            if (result.success()) {
                // 根据是否滚动生成索引 进行index mapping update
                if (isSingleIndex(schemaDTO.getLogicId())) {
                    syncTemplateMapping2Index(schemaDTO.getLogicId());
                } else {
                    SpringTool.publish(new ReBuildTomorrowIndexEvent(this,schemaDTO.getLogicId()));
                }
            }
            return result;
        }

        Result<Void> result = updateFields(schemaDTO.getLogicId(), schemaDTO.getFields(),
            schemaDTO.getRemoveFieldNames(), projectId, operator

        );
        if (result.success()) {
            //JSonu
            // 重建明天索引 通过发布事件提升模板编辑的速度
            
             SpringTool.publish(new ReBuildTomorrowIndexEvent(this,schemaDTO.getLogicId()));
        }

        return result;
    }

    @Override
    public Result<TemplateMappingVO> getSchema(Integer logicId) {
        Result<IndexTemplateWithMapping> result = getTemplateWithMapping(logicId);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        IndexTemplateWithMapping templateLogicWithMapping = result.getData();
        if (templateLogicWithMapping == null) {
            return Result.buildParamIllegal("索引不存在");
        }

        fillSpecialField(templateLogicWithMapping);

        TemplateMappingVO schemaVO = ConvertUtil.obj2Obj(templateLogicWithMapping, TemplateMappingVO.class);
        return Result.buildSucc(schemaVO);
    }

    /**************************************** private method ****************************************************/
    private List<AriusTypeProperty> genAriusTypePropertyList(IndexTemplate templateLogic,
                                                             Map<String, TypeConfig> typeConfigMap) {

        List<IndexTemplateType> templateTypes = indexTemplateService.listLogicTemplateTypes(templateLogic.getId());
        Map<String, IndexTemplateType> typeName2IndexTemplateTypeMap = ConvertUtil.list2Map(templateTypes,
            IndexTemplateType::getName);

        List<AriusTypeProperty> ariusTypePropertyList = Lists.newArrayList();

        if (typeConfigMap == null || typeConfigMap.size() == 0) {
            ariusTypePropertyList.add(buildDefaultType(templateLogic));
        } else if (typeConfigMap.size() == 1) {
            ariusTypePropertyList.add(buildOneType(templateLogic, typeConfigMap, typeName2IndexTemplateTypeMap));
        } else {
            List<AriusTypeProperty> typeProperties = buildMultiType(templateLogic, typeConfigMap,
                typeName2IndexTemplateTypeMap);
            ariusTypePropertyList.addAll(typeProperties);
        }

        return ariusTypePropertyList;
    }

    private List<IndexTemplatePhy> getMasterTemplatePhysicalByLogicId(Integer logicId) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            LOGGER.warn("method=getIndexTemplatePhysicalByLogicId||msg=not exit||logicId={}", logicId);
            return Collections.emptyList();
        }

        if (templateLogicWithPhysical.hasPhysicals()) {
            return templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        } else {
            LOGGER.warn("method=getIndexTemplatePhysicalByLogicId||msg=not deploy||logicId={}", logicId);
            return Collections.emptyList();
        }
    }

    private Result<Void> checkFieldInternal(List<Field> fields) {
        Set<String> existName = Sets.newHashSet();
        for (Field field : fields) {
            Result<Void> result = checkField(existName, field);
            if (result.failed()) {
                return result;
            }
        }
        return Result.buildSucc();
    }

    private Result<Void> checkField(Set<String> existName, Field field) {
        Result<Void> result = checkFieldIsNull(field);
        if (result.failed()) {
            return result;
        }

        if (existName.contains(field.getName())) {
            return Result.buildParamIllegal("字段名称重复");
        }

        existName.add(field.getName());
        TypeEnum typeEnum = TypeEnum.valueFrom(field.getType());
        if (typeEnum.equals(TypeEnum.UNKNOWN)) {
            return Result.buildParamIllegal("字段类型非法");
        }

        IndexEnum indexEnum = IndexEnum.valueFrom(field.getIndexType());
        if (indexEnum.equals(IndexEnum.UNKNOWN)) {
            return Result.buildParamIllegal("检索类型非法");
        }

        if (field.getAnalyzerType() != null) {
            Result<Void> checkFieldAnalyzerTypeResult = checkFieldAnalyzerType(field);
            if (checkFieldAnalyzerTypeResult.failed()) {
                return checkFieldAnalyzerTypeResult;
            }
        }

        if (field.getSortType() != null) {
            Result<Void> checkFieldSortTypeResult = checkFieldSortType(field);
            if (checkFieldSortTypeResult.failed()) {
                return checkFieldSortTypeResult;
            }
        }

        Result<Void> checkFieldTypeResult = checkfieldType(field);
        if (checkFieldTypeResult.failed()) {
            return checkFieldTypeResult;
        }

        return Result.buildSucc();
    }

    private Result<Void> checkFieldAnalyzerType(Field field) {
        AnalyzerEnum analyzerEnum = AnalyzerEnum.valueFrom(field.getAnalyzerType());
        if (analyzerEnum.equals(AnalyzerEnum.UNKNOWN)) {
            return Result.buildParamIllegal("分词器非法");
        }
        return Result.buildSucc();
    }

    private Result<Void> checkFieldSortType(Field field) {
        SortEnum sortEnum = SortEnum.valueFrom(field.getSortType());
        if (sortEnum.equals(SortEnum.UNKNOWN)) {
            return Result.buildParamIllegal("是否排序非法非法");
        }
        return Result.buildSucc();
    }

    private Result<Void> checkfieldType(Field field) {
        if (field.getType().equals(TypeEnum.STRING.getCode())) {
            Result<Void> checkFieldStringResult = handleCheckFieldString(field);
            if (checkFieldStringResult.failed()) {
                return checkFieldStringResult;
            }
        } else {
            if (field.getIndexType().equals(IndexEnum.FUZZY.getCode())) {
                return Result.buildParamIllegal("非string类型的字段不能配置全文检索");
            }
            if (field.getAnalyzerType() != null) {
                return Result.buildParamIllegal("非string类型的字段不能配置分词器");
            }
        }
        return Result.buildSucc();
    }

    private Result<Void> handleCheckFieldString(Field field) {
        if (field.getIndexType().equals(IndexEnum.FUZZY.getCode())) {
            if (field.getSortType() != null && field.getSortType().equals(SortEnum.YES.getCode())) {
                return Result.buildParamIllegal("全文检索的string类型不能排序");
            }
            if (field.getAnalyzerType() == null) {
                return Result.buildParamIllegal("全文检索的string类型必须选择分词器");
            }
        }
        if (field.getIndexType().equals(IndexEnum.FORBID.getCode()) && field.getAnalyzerType() != null) {
            return Result.buildParamIllegal("不检索的string类型不能配置分词器");
        }
        if (field.getIndexType().equals(IndexEnum.EXACT.getCode()) && field.getAnalyzerType() != null) {
            return Result.buildParamIllegal("精确检索的string类型不能配置分词器");
        }
        return Result.buildSucc();
    }

    private Result<Void> checkFieldIsNull(Field field) {
        if (AriusObjUtils.isNull(field)) {
            return Result.buildParamIllegal("字段信息不能为空");
        }
        if (AriusObjUtils.isNull(field.getName())) {
            return Result.buildParamIllegal("字段名称不能为空");
        }
        if (AriusObjUtils.isNull(field.getType())) {
            return Result.buildParamIllegal("字段类型不能为空");
        }
        if (AriusObjUtils.isNull(field.getIndexType())) {
            return Result.buildParamIllegal("检索类型不能为空");
        }
        return Result.buildSucc();
    }

    /**
     * 根据现有配置 和 fields对比，得到修改的mapping
     */
    private Result<MappingConfig> getDiffMapping(String cluster, String template, List<Field> fields) {
        try {
            Result<MappingConfig> result = templatePhyMappingManager.getMapping(cluster, template);
            if (result.failed()) {
                return result;
            }

            MappingConfig mappingConfig = result.getData();
            Set<String> typeNames = mappingConfig.getMapping().keySet();
            if (typeNames.size() > 1) {
                return Result.buildFrom(Result
                    .buildFail("模板中有多个type，请使用mapping json的方式修改, " + "cluster:" + cluster + ", template:" + template));
            }

            String type = "type";
            if (typeNames.size() == 1) {
                for (String typeName : typeNames) {
                    type = typeName;
                }
            }

            List<Field> srcFields = convert2Fields(mappingConfig);
            List<Field> diffFields = diffField(srcFields, fields);

            mappingConfig = convert2Mapping(type, diffFields);

            return Result.buildSucc(mappingConfig);

        } catch (Exception e) {
            return Result.buildFail(e.getMessage());
        }
    }

    private List<Field> convert2Fields(MappingConfig mapping) {
        Map<String/*typeName*/, Map<String/*field*/, TypeDefine>> typeFieldTypeMap = mapping.getTypeDefines();
        List<Field> ret = new ArrayList<>();

        Set<String> fieldNames = new HashSet<>();

        for (Map.Entry<String/*typeName*/, Map<String/*field*/, TypeDefine>> entry : typeFieldTypeMap.entrySet()) {
            for (String fieldName : entry.getValue().keySet()) {
                if (fieldNames.contains(fieldName)) {
                    continue;
                } else {
                    fieldNames.add(fieldName);
                }

                TypeDefine td = entry.getValue().get(fieldName);

                JSONObject typeObj = td.getDefine();
                if (typeObj.isEmpty()) {
                    continue;
                }

                Field field = getField(fieldName, typeObj);

                ret.add(field);
            }
        }

        LOGGER.debug("method=convert2Fields||ret={}||mapping={}", ret, mapping);


        return ret;
    }

    private Field getField(String fieldName, JSONObject typeObj) {
        Field field = new Field();
        field.setName(fieldName);

        if (TEXT_STR.equals(typeObj.getString(TYPE_STR))) {
            handleTypeText(typeObj, field);
        } else if (KEYWORD_STR.equals(typeObj.get(TYPE_STR))) {
            handleTypeKeyword(typeObj, field);
        } else if (TypeEnum.INT.getCode().equalsIgnoreCase(typeObj.getString(TYPE_STR))
                   || TypeEnum.LONG.getCode().equalsIgnoreCase(typeObj.getString(TYPE_STR))
                   || TypeEnum.BOOLEAN.getCode().equalsIgnoreCase(typeObj.getString(TYPE_STR))
                   || TypeEnum.DOUBLE.getCode().equalsIgnoreCase(typeObj.getString(TYPE_STR))
                   || TypeEnum.DATE.getCode().equalsIgnoreCase(typeObj.getString(TYPE_STR))
                   || TypeEnum.OBJECT.getCode().equalsIgnoreCase(typeObj.getString(TYPE_STR))
                   || TypeEnum.FLOAT.getCode().equalsIgnoreCase(typeObj.getString(TYPE_STR))) {
            handleTypeNonString(typeObj, field);
        } else {
            handleTypeUnknown(field);
        }
        return field;
    }

    private void handleTypeUnknown(Field field) {
        field.setType(TypeEnum.UNKNOWN.getCode());
        field.setAnalyzerType(AnalyzerEnum.UNKNOWN.getCode());
        field.setIndexType(IndexEnum.UNKNOWN.getCode());
        field.setSortType(SortEnum.UNKNOWN.getCode());
    }

    private void handleTypeNonString(JSONObject typeObj, Field field) {
        field.setType(typeObj.getString(TYPE_STR));

        if (typeObj.containsKey(INDEX_STR) && !typeObj.getBoolean(INDEX_STR).booleanValue()) {
            field.setIndexType(IndexEnum.FORBID.getCode());
        } else {
            field.setIndexType(IndexEnum.EXACT.getCode());
        }

        if (typeObj.containsKey(DOC_VALUES_STR)) {
            if (typeObj.getBoolean(DOC_VALUES_STR).booleanValue()) {
                field.setSortType(SortEnum.YES.getCode());
            } else {
                field.setSortType(SortEnum.NO.getCode());
            }
        }
    }

    private void handleTypeKeyword(JSONObject typeObj, Field field) {
        field.setType(TypeEnum.STRING.getCode());

        if (typeObj.containsKey(INDEX_STR) && !typeObj.getBoolean(INDEX_STR).booleanValue()) {
            field.setIndexType(IndexEnum.FORBID.getCode());
        } else {
            field.setIndexType(IndexEnum.EXACT.getCode());
        }

        if (typeObj.containsKey(DOC_VALUES_STR)) {
            if (typeObj.getBoolean(DOC_VALUES_STR).booleanValue()) {
                field.setSortType(SortEnum.YES.getCode());
            } else {
                field.setSortType(SortEnum.NO.getCode());
            }
        }
    }

    private void handleTypeText(JSONObject typeObj, Field field) {
        field.setType(TypeEnum.STRING.getCode());

        // 分词配置
        if (typeObj.containsKey(ANALYZER_STR) && IK_SMART_SRT.equals(typeObj.getString(ANALYZER_STR))) {
            field.setAnalyzerType(AnalyzerEnum.IK.getCode());
        } else {
            field.setAnalyzerType(AnalyzerEnum.DEFAULT.getCode());
        }

        if (typeObj.containsKey(INDEX_STR) && !typeObj.getBoolean(INDEX_STR).booleanValue()) {
            field.setIndexType(IndexEnum.FORBID.getCode());
            field.setAnalyzerType(null);
        } else {
            field.setIndexType(IndexEnum.FUZZY.getCode());
        }

        if (typeObj.containsKey(DOC_VALUES_STR)) {
            if (typeObj.getBoolean(DOC_VALUES_STR).booleanValue()) {
                field.setSortType(SortEnum.YES.getCode());
            } else {
                field.setSortType(SortEnum.NO.getCode());
            }
        }
    }

    private MappingConfig convert2Mapping(String type, List<Field> fields) {
        Map<String, TypeDefine> mappingTypes = Maps.newHashMap();

        for (Field field : fields) {
            // fields -> type
            TypeEnum typeEnum = TypeEnum.valueFrom(field.getType());
            IndexEnum indexEnum = IndexEnum.valueFrom(field.getIndexType());
            SortEnum sortEnum = SortEnum.valueFrom(field.getSortType());
            AnalyzerEnum analyzerEnum = AnalyzerEnum.valueFrom(field.getAnalyzerType());

            JSONObject typeObj = new JSONObject();
            if (typeEnum == TypeEnum.STRING) {
                handleTypeString(indexEnum, sortEnum, analyzerEnum, typeObj);
            } else {
                handleTypeNonString(typeEnum, indexEnum, sortEnum, typeObj);
            }

            mappingTypes.put(field.getName(), new TypeDefine(typeObj));
        }

        MappingConfig mappingConfig = new MappingConfig();

        for (Map.Entry<String, TypeDefine> entry : mappingTypes.entrySet()) {
            String field = entry.getKey();
            List<String> fieldList = new ArrayList<>();
            fieldList.addAll(Arrays.asList(field.split("\\.")));
            mappingConfig.addFields(type, fieldList, entry.getValue());
        }

        return mappingConfig;
    }

    private void handleTypeNonString(TypeEnum typeEnum, IndexEnum indexEnum, SortEnum sortEnum, JSONObject typeObj) {
        typeObj.put(TYPE_STR, typeEnum.getCode());

        if (typeEnum != TypeEnum.OBJECT && typeEnum != TypeEnum.ARRAY) {
            if (indexEnum == IndexEnum.FORBID) {
                typeObj.put(INDEX_STR, false);
            } else if (indexEnum == IndexEnum.EXACT) {
                typeObj.put(INDEX_STR, true);
            }

            if (sortEnum == SortEnum.NO) {
                typeObj.put(DOC_VALUES_STR, false);
            } else if (sortEnum == SortEnum.YES) {
                typeObj.put(DOC_VALUES_STR, true);
            }

            // 如果新增，或者改为Date类型，则自动增加format
            if (typeEnum == TypeEnum.DATE) {
                typeObj.put(
                    "format",
                    "yyyy-MM-dd HH:mm:ss Z||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm:ss.SSS Z||yyyy-MM-dd HH:mm:ss.SSS"
                              + "||yyyy-MM-dd HH:mm:ss,SSS||yyyy/MM/dd HH:mm:ss||yyyy-MM-dd HH:mm:ss,SSS Z||yyyy/MM/dd HH:mm:ss,SSS Z||epoch_millis");
            }
        }
    }

    private void handleTypeString(IndexEnum indexEnum, SortEnum sortEnum, AnalyzerEnum analyzerEnum,
                                  JSONObject typeObj) {
        if (indexEnum == IndexEnum.FUZZY) {
            typeObj.put(TYPE_STR, TEXT_STR);
        } else {
            typeObj.put(TYPE_STR, KEYWORD_STR);
        }

        if (indexEnum == IndexEnum.FORBID) {
            typeObj.put(INDEX_STR, false);
        } else if (indexEnum == IndexEnum.EXACT) {
            typeObj.put(INDEX_STR, true);
        }

        if (sortEnum == SortEnum.NO) {
            typeObj.put(DOC_VALUES_STR, false);
        } else if (sortEnum == SortEnum.YES) {
            typeObj.put(DOC_VALUES_STR, true);
        }

        if (analyzerEnum == AnalyzerEnum.IK) {
            typeObj.put(ANALYZER_STR, IK_SMART_SRT);
        }
    }

    /**
     * 返回dst中和src中不同的field
     *
     * @param src
     * @param dst
     * @return
     */
    private List<Field> diffField(List<Field> src, List<Field> dst) {
        Map<String, Field> srcMap = Maps.newHashMap();

        for (Field field : src) {
            srcMap.put(field.getName(), field);
        }

        List<Field> ret = new ArrayList<>();
        for (Field field : dst) {
            if (!srcMap.containsKey(field.getName())) {
                ret.add(field);
                continue;
            }

            if (!field.esTypeEquals(srcMap.get(field.getName()))) {
                ret.add(field);
            }
        }

        return ret;
    }

    private AriusTypeProperty buildDefaultType(IndexTemplate templateLogic) {
        // 优先取模板中的id和routing
        String idField = templateLogic.getIdField();
        String routingField = templateLogic.getRoutingField();

        AriusTypeProperty ariusTypeProperty = new AriusTypeProperty();
        ariusTypeProperty.setTypeName(AdminConstant.DEFAULT_INDEX_MAPPING_TYPE);

        ariusTypeProperty.setProperties(new JSONObject());

        ariusTypeProperty.setIdField(idField);
        ariusTypeProperty.setRoutingField(routingField);
        ariusTypeProperty.setDateField(templateLogic.getDateField());
        ariusTypeProperty.setDateFieldFormat(templateLogic.getDateFieldFormat());

        return ariusTypeProperty;
    }

    private AriusTypeProperty buildOneType(IndexTemplate templateLogic, Map<String, TypeConfig> typeConfigMap,
                                           Map<String, IndexTemplateType> typeName2IndexTemplateTypeMap) {
        Map.Entry<String, TypeConfig> entry = typeConfigMap.entrySet().iterator().next();
        String typeName = entry.getKey();

        // 优先取模板中的id和routing
        String idField = templateLogic.getIdField();
        String routingField = templateLogic.getRoutingField();

        if (typeName2IndexTemplateTypeMap.containsKey(typeName)) {
            IndexTemplateType typeFromMysql = typeName2IndexTemplateTypeMap.get(typeName);

            if (StringUtils.isBlank(idField) && typeFromMysql != null) {
                idField = typeFromMysql.getIdField();
            }

            if (StringUtils.isBlank(routingField) && typeFromMysql != null) {
                routingField = typeFromMysql.getRouting();
            }
        }

        TypeConfig typeConfig = entry.getValue();

        AriusTypeProperty ariusTypeProperty = new AriusTypeProperty();
        ariusTypeProperty.setTypeName(typeName);

        if (typeConfig.getProperties() == null) {
            ariusTypeProperty.setProperties(new JSONObject());
        } else {
            ariusTypeProperty.setProperties(typeConfig.getProperties().toJson());
        }

        ariusTypeProperty.setIdField(idField);
        ariusTypeProperty.setRoutingField(routingField);
        ariusTypeProperty.setDateField(templateLogic.getDateField());
        ariusTypeProperty.setDateFieldFormat(templateLogic.getDateFieldFormat());

        // 获取并且设置对应的dynamic_templates
        Map<String, Object> notUsedMap = typeConfig.getNotUsedMap();
        if (!MapUtils.isEmpty(notUsedMap) && notUsedMap.containsKey(DYNAMIC_TEMPLATES_STR)) {
            JSONArray dynamicArrays = (JSONArray) notUsedMap.get(DYNAMIC_TEMPLATES_STR);
            ariusTypeProperty.setDynamicTemplates(dynamicArrays);
        }

        return ariusTypeProperty;
    }

    private List<AriusTypeProperty> buildMultiType(IndexTemplate templateLogic, Map<String, TypeConfig> typeConfigMap,
                                                   Map<String, IndexTemplateType> typeName2IndexTemplateTypeMap) {
        List<AriusTypeProperty> typeProperties = Lists.newArrayList();
        for (Map.Entry<String, TypeConfig> entry : typeConfigMap.entrySet()) {
            String typeName = entry.getKey();
            TypeConfig typeConfig = entry.getValue();

            AriusTypeProperty ariusTypeProperty = new AriusTypeProperty();
            ariusTypeProperty.setTypeName(typeName);

            if (typeConfig.getProperties() == null) {
                ariusTypeProperty.setProperties(new JSONObject());
            } else {
                ariusTypeProperty.setProperties(typeConfig.getProperties().toJson());
            }

            if (typeName2IndexTemplateTypeMap.containsKey(typeName)) {
                IndexTemplateType typeFromMysql = typeName2IndexTemplateTypeMap.get(typeName);
                if (typeFromMysql != null) {
                    ariusTypeProperty.setIdField(typeFromMysql.getIdField());
                    ariusTypeProperty.setRoutingField(typeFromMysql.getRouting());
                    ariusTypeProperty.setDateField(templateLogic.getDateField());
                    ariusTypeProperty.setDateFieldFormat(templateLogic.getDateFieldFormat());
                }
            }

            typeProperties.add(ariusTypeProperty);
        }

        return typeProperties;
    }

    private List<AriusTypeProperty> buildAriusTypeProperty(List<MappingOptimizeItem> items) {
        Multimap<String, MappingOptimizeItem> typeName2JSONObjectMultiMap = ConvertUtil.list2MulMap(items,
            MappingOptimizeItem::getTypeName);

        List<AriusTypeProperty> typeProperties = Lists.newArrayList();
        for (String typeName : typeName2JSONObjectMultiMap.keySet()) {
            AriusTypeProperty typeProperty = new AriusTypeProperty();
            typeProperty.setTypeName(typeName);

            Collection<MappingOptimizeItem> typeOptimizeItems = typeName2JSONObjectMultiMap.get(typeName);
            JSONObject properties = new JSONObject();
            for (MappingOptimizeItem item : typeOptimizeItems) {
                properties.put(item.getFieldName(), item.getOptimize());
            }

            typeProperty.setProperties(properties);

            typeProperties.add(typeProperty);
        }

        return typeProperties;
    }

    private Result<Void> saveSpecialField(ConsoleTemplateSchemaDTO schemaDTO, String operator,
                                          Integer projectId) throws AdminOperateException {
        if (CollectionUtils.isNotEmpty(schemaDTO.getFields())) {
            return saveSpecialFieldByField(schemaDTO, operator, projectId);
        } else {
            return saveSpecialFieldByJSON(schemaDTO, operator, projectId);
        }

    }

    private Result<Void> saveSpecialFieldByJSON(ConsoleTemplateSchemaDTO schemaDTO, String operator,
                                                Integer projectId) throws AdminOperateException {
        List<AriusTypeProperty> typeProperties = schemaDTO.getTypeProperties();
        if (typeProperties.size() == 1 && (StringUtils.isBlank(typeProperties.get(0).getTypeName())
                                           || typeProperties.get(0).getTypeName().equals(DEFAULT_INDEX_MAPPING_TYPE))) {
            // 就一个type，修改模板的id和routing字段
            IndexTemplateDTO templateLogicDTO = new IndexTemplateDTO();
            templateLogicDTO.setId(schemaDTO.getLogicId());

            templateLogicDTO.setDateField(typeProperties.get(0).getDateField());
            templateLogicDTO.setDateFieldFormat(typeProperties.get(0).getDateFieldFormat());
            Result<Void> editDateFieldResult = indexTemplateService.editTemplate(templateLogicDTO, operator, projectId);
            if (editDateFieldResult.failed()) {
                return editDateFieldResult;
            }
        } else {
            Result<Void> result = handleUpdateType(schemaDTO, operator, typeProperties, projectId);
            if (result.failed()) {
                return result;
            }
        }

        return Result.buildSucc();
    }

    private Result<Void> handleUpdateType(ConsoleTemplateSchemaDTO schemaDTO, String operator,
                                          List<AriusTypeProperty> typeProperties,
                                          Integer projectId) throws AdminOperateException {
        // 修改type表
        List<IndexTemplateType> templateTypes = indexTemplateService.listLogicTemplateTypes(schemaDTO.getLogicId());
        Map<String, IndexTemplateType> typeName2IndexTemplateTypeMap = ConvertUtil.list2Map(templateTypes,
            IndexTemplateType::getName);

        String dateField = typeProperties.get(0).getDateField();
        String dateFieldFormat = typeProperties.get(0).getDateFieldFormat();

        Result<Void> result = handleTypeProperties(typeProperties, typeName2IndexTemplateTypeMap, dateField);
        if (result.failed()) {
            return result;
        }

        // 修改模板的时间字段
        if (dateField != null || dateFieldFormat != null) {
            IndexTemplateDTO templateLogicDTO = new IndexTemplateDTO();
            templateLogicDTO.setId(schemaDTO.getLogicId());
            templateLogicDTO.setDateField(dateField);
            templateLogicDTO.setDateFieldFormat(dateFieldFormat);
            Result<Void> editDateFieldResult = indexTemplateService.editTemplate(templateLogicDTO, operator, projectId);
            if (editDateFieldResult.failed()) {
                return editDateFieldResult;
            }
        }
        return Result.buildSucc();
    }

    private Result<Void> handleTypeProperties(List<AriusTypeProperty> typeProperties,
                                              Map<String, IndexTemplateType> typeName2IndexTemplateTypeMap,
                                              String dateField) {
        for (AriusTypeProperty typeProperty : typeProperties) {

            if (!Objects.equals(dateField, typeProperty.getDateField())) {
                return Result.buildFail("多个type的分区字段必须一致");
            }

            if (typeName2IndexTemplateTypeMap.containsKey(typeProperty.getTypeName())) {
                // 更新
                IndexTemplateType indexTemplateType = typeName2IndexTemplateTypeMap.get(typeProperty.getTypeName());
                TemplateTypePO param = new TemplateTypePO();
                param.setId(indexTemplateType.getId());
                param.setIdField(typeProperty.getIdField());
                param.setRouting(typeProperty.getRoutingField());
                if (!indexTemplateService.updateTemplateType(param)) {
                    return Result.buildFail("保存特征字段失败");
                } else {
                    LOGGER.info("method=saveSpecialFieldByJSON||msg=update db succ||typeId={}", param.getId());
                }
            } else {
                if (StringUtils.isNotBlank(typeProperty.getIdField())
                    || StringUtils.isNotBlank(typeProperty.getRoutingField())) {
                    return Result.buildFail(
                        "平台升级es到高版本(7.6.1)，高版本es索引仅支持单type，不再支持多个type；平台弱化了用户侧索引type的概念，索引多type需求用户可以通过创建多个索引来实现；\n"
                                            + "如需指定索引type的主键字段或者路由字段，请确认您的索引mapping中只有一个名为\""
                                            + DEFAULT_INDEX_MAPPING_TYPE + "\"的type；");
                }
            }
        }
        return Result.buildSucc();
    }

    private Result<Void> saveSpecialFieldByField(ConsoleTemplateSchemaDTO schemaDTO, String operator,
                                                 Integer projectId) throws AdminOperateException {
        SpecialField specialField = SpecialField.analyzeFromFields(schemaDTO.getFields(),
            schemaDTO.getRemoveFieldNames());
        IndexTemplateDTO templateLogicDTO = new IndexTemplateDTO();
        templateLogicDTO.setId(schemaDTO.getLogicId());
        templateLogicDTO.setDateField(specialField.getDateField());
        templateLogicDTO.setDateFieldFormat(specialField.getDateFieldFormat());
        return indexTemplateService.editTemplate(templateLogicDTO, operator, projectId);
    }

    private boolean clusterIsHighVersion(Integer logicId) {
        IndexTemplateWithPhyTemplates logicWithPhysical = this.indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (!logicWithPhysical.hasPhysicals()) {
            return false;
        }

        Result<ClusterPhy> clusterPhyResult = clusterPhyManager
            .getClusterByName(logicWithPhysical.getMasterPhyTemplate().getCluster());
        if (null == clusterPhyResult.getData()) {
            return false;
        }

        return ESVersionUtil.isHigher(clusterPhyResult.getData().getEsVersion(), "6.5.1");
    }

    protected void fillSpecialField(IndexTemplateWithMapping templateLogicWithMapping) {
        if (CollectionUtils.isEmpty(templateLogicWithMapping.getFields())) {
            return;
        }

        Map<String, Field> name2FieldMap = ConvertUtil.list2Map(templateLogicWithMapping.getFields(), Field::getName);

        if (StringUtils.isNotBlank(templateLogicWithMapping.getDateField())) {
            handleDateField(templateLogicWithMapping, name2FieldMap);
        }

        if (StringUtils.isNotBlank(templateLogicWithMapping.getIdField())) {
            handleIdField(templateLogicWithMapping, name2FieldMap);
        }

        if (StringUtils.isNotBlank(templateLogicWithMapping.getRoutingField())) {
            handleRoutingField(templateLogicWithMapping, name2FieldMap);
        }
    }

    private void handleRoutingField(IndexTemplateWithMapping templateLogicWithMapping,
                                    Map<String, Field> name2FieldMap) {
        for (String routingField : templateLogicWithMapping.getRoutingField().split(",")) {
            if (name2FieldMap.containsKey(routingField)) {
                name2FieldMap.get(routingField).setRoutingField(true);
            }
        }
    }

    private void handleIdField(IndexTemplateWithMapping templateLogicWithMapping, Map<String, Field> name2FieldMap) {
        for (String idField : templateLogicWithMapping.getIdField().split(",")) {
            if (name2FieldMap.containsKey(idField)) {
                name2FieldMap.get(idField).setIdField(true);
            }
        }
    }

    private void handleDateField(IndexTemplateWithMapping templateLogicWithMapping, Map<String, Field> name2FieldMap) {
        if (name2FieldMap.containsKey(templateLogicWithMapping.getDateField())) {
            name2FieldMap.get(templateLogicWithMapping.getDateField()).setDateField(true);
            name2FieldMap.get(templateLogicWithMapping.getDateField())
                .setDateFieldFormat(templateLogicWithMapping.getDateFieldFormat());
        }
    }

    /**
     * 判断模板是否生成滚动索引
     * @param logicId
     * @return
     */
    private boolean isSingleIndex(Integer logicId) {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);
        //滚动索引的expression 以* 结尾
        return !templateLogicWithPhysical.getExpression().endsWith("*");
    }

    /**
     * 将模板mapping 更新到非滚动index上
     * @param logicId
     * @return
     */
    private void syncTemplateMapping2Index(Integer logicId) throws ESOperateException {
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
            .getLogicTemplateWithPhysicalsById(logicId);

        List<IndexTemplatePhy> templatePhysicals = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        for (IndexTemplatePhy indexTemplatePhy : templatePhysicals) {
            Result<MappingConfig> result = templatePhyMappingManager.getMapping(indexTemplatePhy.getCluster(),
                indexTemplatePhy.getName());
            if (result.failed()) {
                LOGGER.warn("class=TemplateLogicMappingManagerImpl||method=syncTemplateMapping2Index|||logicId={}",
                    logicId);
            }
            MappingConfig templateMappingConfig = result.getData();
            Result<Void> updateResult = templatePhyMappingManager.syncTemplateMapping2Index(
                indexTemplatePhy.getCluster(), indexTemplatePhy.getExpression(), templateMappingConfig);
            if (updateResult.failed()) {
                LOGGER.warn("class=TemplateLogicMappingManagerImpl||method=syncTemplateMapping2Index||mapping={}",
                    templateMappingConfig);
            }
        }
    }

    /**
     * 判断修改前后两个mapping 对象是否改变，这里只判断「原mapping」已有字段是否被修改，「新增字段」不在判断范围之内
     * @param src
     * @param dest
     * @return
     */
    private boolean isExistMappingChanged(Map<String, TypeDefine> src, Map<String, TypeDefine> dest) {
        if (src == null || dest == null) {
            return false;
        }
        try {
            for (Map.Entry<String, TypeDefine> entry : src.entrySet()) {
                if (!entry.getValue().equals(dest.get(entry.getKey()))) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.error("class=TemplateLogicMappingManagerImpl||method=isExistMappingChanged||errMsg={}",
                e.getMessage(), e);
        }
        return false;
    }

}