package com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplatePhyMappingManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminESOpRetryConstants;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.common.util.AriusIndexMappingConfigUtils;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameFactory;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.impl.IndexTemplateServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESIndexDAO;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.TypeConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.MultiIndexsConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.TemplateConfig;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhonghua
 */
@Service
@NoArgsConstructor
public class TemplatePhyMappingManagerImpl implements TemplatePhyMappingManager {
    private static final ILog    LOGGER                   = LogFactory.getLog(IndexTemplateServiceImpl.class);

    private static final String  MAPPING_STR              = "mapping";
    private static final String  MAPPINGS_STR             = "mappings";
    private static final Integer MAPPING_FIELD_LIMIT_SIZE = 1000;

    private static final String  JSON_PARSE_ERROR_TIPS    = "json解析失败";

    @Autowired
    private ESTemplateService    templateService;

    @Autowired
    private ESIndexService       indexService;

    @Autowired
    private ESIndexDAO           esIndexDAO;

    @Override
    public Result<Void> updateMapping(String cluster, String template, String mappingStr) {
        return updateMappingCore(cluster, template, mappingStr, null, false);
    }

    @Override
    public Result<Void> updateMappingAndMerge(String cluster, String template, String mappingStr,
                                              Set<String> removeFields) {
        return updateMappingCore(cluster, template, mappingStr, removeFields, true);
    }

    @Override
    public Result<MappingConfig> getMapping(String cluster, String name) {
        TemplateConfig templateConfig = templateService.syncGetTemplateConfig(cluster, name);
        if (templateConfig != null) {
            MappingConfig mappingConfig = templateConfig.getMappings();
            if (mappingConfig != null) {
                mappingConfig.removeDefault();
                return Result.buildSucc(mappingConfig);
            }
        }

        return Result.buildFail("not find template mapping, cluster:" + cluster + ", template:" + name);
    }

    @Override
    public Result<Void> syncTemplateMapping2Index(String cluster, String index, MappingConfig mappingConfig) throws ESOperateException {
        if (!esIndexDAO.updateIndexMapping(cluster, index, mappingConfig)) {
            return Result.buildFail("update index mapping fail");
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Void> checkMapping(String cluster, String template, String mappingsStr, boolean doMerge) {
        try {
            MappingConfig mappings = new MappingConfig(getMappingObj(JSON.parseObject(mappingsStr)));
            if (mappings.haveDefault()) {
                return Result.build(ResultType.FAIL.getCode(), "mapping have _default_ type");
            }

            return checkMapping(cluster, template, mappings);
        } catch (JSONException e) {
            return Result.build(ResultType.FAIL.getCode(), JSON_PARSE_ERROR_TIPS);
        } catch (Exception t) {
            return Result.build(ResultType.FAIL.getCode(), t.getMessage());
        }
    }

    @Override
    public Result<MappingConfig> syncMappingConfig(String cluster, String template, String expression,
                                                   String dataFormat) {
        try {
            // 拉取模板mapping
            Result<MappingConfig> getTemplateMappingResult = getMapping(cluster, template);
            if (getTemplateMappingResult.failed()) {
                return getTemplateMappingResult;
            }
            // 模板mapping
            MappingConfig templateMappingConfig = getTemplateMappingResult.getData();

            // 拉取索引mapping, 和模板mapping融合，索引名使用昨天和今天的索引名，逗号分隔
            String todayIndexName = IndexNameFactory.getNoVersion(expression, dataFormat, 0);
            String yesterdayIndexName = IndexNameFactory.getNoVersion(expression, dataFormat, -1);
            String indexName = todayIndexName + "*," + yesterdayIndexName + "*";

            Result<List<MappingConfig>> getIndexMappingResult = getIndexMappings(cluster, indexName);

            if (getIndexMappingResult.failed()) {
                return Result.buildFrom(getIndexMappingResult);
            }

            List<MappingConfig> indexMappingConfigs = getIndexMappingResult.getData();
            if (indexMappingConfigs == null || indexMappingConfigs.isEmpty()) {
                return Result.buildSucc(templateMappingConfig);
            }

            // 将索引的mapping合并到模板mapping
            MappingConfig mergeMappingConfig = new MappingConfig(templateMappingConfig.toJson());
            for (MappingConfig imc : indexMappingConfigs) {
                mergeMappingConfig.merge(imc);
            }

            if (checkMappingFieldSize(cluster, template, mergeMappingConfig, MAPPING_FIELD_LIMIT_SIZE)) {
                return Result.buildFail("模版字段个数超过" + MAPPING_FIELD_LIMIT_SIZE);
            }

            // index和template定义冲突字段类型，使用template的定义
            mergeMappingConfig.merge(new MappingConfig(templateMappingConfig.toJson()));

            clearDefaultMapping(templateMappingConfig);
            clearDefaultMapping(mergeMappingConfig);
            // mapping有变化，更新
            if (!mergeMappingConfig.toJson().equals(templateMappingConfig.toJson())) {
                mergeMultiTypePropertiesToUserDefinedType(indexName, mergeMappingConfig);
                MappingConfig toUpdateMapping = new MappingConfig(mergeMappingConfig.toJson());
                Result<Void> result = updateMapping(cluster, template, toUpdateMapping.toJson().toJSONString());
                if (result.failed()) {
                    LOGGER.error("class=TemplatePhyMappingManagerImpl||method=syncMappingConfig||errMsg={} "
                                 + "{} fail to update mapping, error {}",
                        cluster, template, result.getMessage());
                }
            }

            return Result.buildSucc(mergeMappingConfig);
        } catch (Exception t) {
            return Result.buildFail(t.getMessage());
        }
    }

    @Override
    public Result<Void> addIndexMapping(String cluster, String expression, String dataFormat, int updateDays,
                                        MappingConfig mappingConfig) throws ESOperateException {
        for (int i = 1; i <= updateDays; i++) {
            String indexName = IndexNameFactory.getNoVersion(expression, dataFormat, 2 - i);

            if (!esIndexDAO.updateIndexMapping(cluster, indexName, mappingConfig)) {
                return Result.buildFail("update index mapping fail");
            }
        }

        return Result.buildSucc();

    }

    @Override
    public Result<Void> checkMappingForNew(String name, AriusTypeProperty ariusTypeProperty) {
        try {
            MappingConfig mappingConfig = new MappingConfig(ariusTypeProperty.toMappingJSON());
            Map<String, TypeConfig> typeConfigMap = mappingConfig.getMapping();
            if (typeConfigMap != null && typeConfigMap.size() > 1) {
                return Result.build(ResultType.FAIL.getCode(), "mapping具有多个type, 只能配置一个type");
            }
        } catch (JSONException e) {
            return Result.build(ResultType.FAIL.getCode(), JSON_PARSE_ERROR_TIPS);
        } catch (Exception e) {
            return Result.build(ResultType.FAIL.getCode(), e.getMessage());
        }

        return checkMapping(null, name, ariusTypeProperty.toMappingJSON().toJSONString(), false);

    }

    /**************************************** private method ****************************************************/
    private Result<Void> checkMapping(String cluster, String template, MappingConfig mappings) {
        if (isLowVersionCluster(cluster) && !mappings.isEmpty()) {
            String indexName = String.format("indexforcheckmapping_%s_%s", cluster, template);
            return preCreateIndexToCheckTemplateConfig(cluster, indexName, mappings,
                createDefaultSettings(indexName, mappings.getMapping()));
        }

        return Result.buildSucc();
    }

    /**
     * 低版本ES集群
     * @param cluster
     * @return
     */
    private boolean isLowVersionCluster(String cluster) {
        return (StringUtils.isNotBlank(cluster) && AdminConstant.LOW_VERSION_ES_CLUSTER.contains(cluster));
    }

    /**
     * 需要通过创建索引的方式来Check模板Mapping的合法性
     * @param cluster 集群名称
     * @param template 模板名称
     * @param mappings 模板Mapping信息
     * @return
     */
    private Result<Void> preCreateIndexToCheckTemplateConfig(String cluster, String template, MappingConfig mappings,
                                                             Map<String, String> settings) {

        IndexConfig indexConfig = new IndexConfig();
        indexConfig.setMappings(mappings);
        indexConfig.setSettings(settings);

        return tryCreateIndex(cluster, template, indexConfig);
    }

    /**
     * 创建默认模板Setting信息
     * @param indexName 索引名称
     * @param typeConfigMap 模板配置
     * @return
     */
    private Map<String, String> createDefaultSettings(String indexName, Map<String, TypeConfig> typeConfigMap) {
        Map<String, String> settings = Maps.newHashMap();
        settings.put("index.mapping.total_fields.limit", "100000");
        if (typeConfigMap != null) {
            if (typeConfigMap.containsKey("_default_")) {
                if (typeConfigMap.size() == 2) {
                    settings.put(AdminConstant.SINGLE_TYPE_KEY, AdminConstant.DEFAULT_SINGLE_TYPE);
                }

            } else {
                if (typeConfigMap.size() == 1) {
                    settings.put(AdminConstant.SINGLE_TYPE_KEY, AdminConstant.DEFAULT_SINGLE_TYPE);
                }
            }

            String settingValue = settings.get(AdminConstant.SINGLE_TYPE_KEY);
            if (null == settingValue || !AdminConstant.DEFAULT_SINGLE_TYPE.equals(settingValue)) {
                LOGGER.warn("class=TemplatePhyMappingManagerImpl||method=checkMapping||"
                            + "singleTypeSettings={}||indexTemplate={}",
                    settings.keySet(), indexName);
                settings.put(AdminConstant.SINGLE_TYPE_KEY, AdminConstant.DEFAULT_SINGLE_TYPE);
            }
        }

        return settings;
    }

    /**
     * 获取指定集群指定索引的mapping
     * @param cluster 集群名
     * @param indexName 索引名，支持多个，逗号分隔
     * @return 索引mapping
     */
    private Result<List<MappingConfig>> getIndexMappings(String cluster, String indexName) {
        // 获取索引配置
        MultiIndexsConfig multiIndexsConfig = indexService.syncGetIndexConfigs(cluster, indexName);
        if (multiIndexsConfig == null || multiIndexsConfig.getIndexConfigMap() == null) {
            return Result.buildFail("get index config return null");
        }

        // 索引配置map，key-索引名，value-索引配置
        Map<String, IndexConfig> indexConfigMap = multiIndexsConfig.getIndexConfigMap();

        List<MappingConfig> ret = new ArrayList<>();
        // 遍历索引名，获取索引的mapping配置

        for (Map.Entry<String, IndexConfig> entry : indexConfigMap.entrySet()) {
            String name = entry.getKey();
            IndexConfig indexConfig = indexConfigMap.get(name);
            if (indexConfig == null) {
                return Result.buildFail("get null index config, indexName:" + name);
            }

            if (indexConfig.getMappings() == null) {
                return Result.buildFail("get null mapping config, indexName:" + name);
            }

            ret.add(indexConfig.getMappings());
        }

        return Result.buildSucc(ret);
    }

    /**
     * 合并多个type属性到默认type
     * @param indexTemplate 索引名称
     * @param templateMappingConfig 模板配置
     */
    private void mergeMultiTypePropertiesToUserDefinedType(String indexTemplate, MappingConfig templateMappingConfig) {
        if (templateMappingConfig != null && templateMappingConfig.getMapping() != null) {
            templateMappingConfig.removeDefault();
            Map<String, TypeConfig> typeConfigs = templateMappingConfig.getMapping();
            if (!typeConfigs.isEmpty()) {
                if (typeConfigs.size() == 1) {
                    LOGGER.info(
                        "class=TemplatePhysicalMappingServiceImpl||method=mergeMultiTypePropertiesToDefaultType||msg=singleType"
                                + "||typeName={}||indexTemplate={}",
                        typeConfigs.keySet(), indexTemplate);
                } else if (typeConfigs.size() == 2
                           && typeConfigs.containsKey(AdminConstant.DEFAULT_INDEX_MAPPING_TYPE)) {
                    String userDefinedTypeName = fetchNonDefaultKey(typeConfigs,
                        AdminConstant.DEFAULT_INDEX_MAPPING_TYPE);
                    LOGGER
                        .info("class=TemplatePhysicalMappingServiceImpl||method=mergeMultiTypePropertiesToDefaultType||"
                              + "msg=multi type||userDefinedType={}||indexTemplate={}",
                            userDefinedTypeName, indexTemplate);
                    if (StringUtils.isNotBlank(userDefinedTypeName)) {
                        typeConfigs.get(userDefinedTypeName)
                            .merge(typeConfigs.get(AdminConstant.DEFAULT_INDEX_MAPPING_TYPE));
                        typeConfigs.remove(AdminConstant.DEFAULT_INDEX_MAPPING_TYPE);
                    }
                } else {
                    LOGGER.warn(
                        "class=TemplatePhysicalMappingServiceImpl||method=mergeMultiTypePropertiesToDefaultTypee||"
                                + "msg=multi user defined types||userDefinedTypes={}||indexTemplate={}",
                        typeConfigs.keySet(), indexTemplate);
                }
            }
        }
    }

    /**
     * 获取第一个非默认type名称
     * @param typeConfigs type configs
     * @param defaultType 默认type名称
     * @return
     */
    private String fetchNonDefaultKey(Map<String, TypeConfig> typeConfigs, String defaultType) {
        if (typeConfigs == null || StringUtils.isBlank(defaultType)) {
            return null;
        }

        for (String typeName : typeConfigs.keySet()) {
            if (!defaultType.equals(typeName)) {
                return typeName;
            }
        }

        return null;
    }

    /**
     * 清楚Default Mapping信息
     *
     * @param mappingConfig 模板Mapping Config.
     */
    private void clearDefaultMapping(MappingConfig mappingConfig) {
        mappingConfig.removeDefault();
    }

    private JSONObject getMappingObj(JSONObject obj) {
        if (obj.containsKey(MAPPING_STR)) {
            return obj.getJSONObject(MAPPING_STR);
        }

        if (obj.containsKey(MAPPINGS_STR)) {
            return obj.getJSONObject(MAPPINGS_STR);
        }

        return obj;
    }

    private Result<Void> tryCreateIndex(String clusterName, String indexName, IndexConfig indexConfig) {
        try {
            esIndexDAO.deleteIndex(clusterName, indexName);

            if (!esIndexDAO.createIndexWithConfig(clusterName, indexName, indexConfig,3)) {
                return Result.buildFail("create index get false");
            }

            return Result.buildSucc();
        } catch (Exception t) {
            LOGGER.warn(
                "class=TemplatePhyMappingManagerImpl||method=tryCreateIndex||msg=check mapping error, cluster:{}, tmp_index:{}, mapping:{}",
                clusterName, indexName, indexConfig.getMappings().toJson(), t);

            StringBuilder sb = new StringBuilder();
            while (t != null) {
                sb.append(t.getMessage()).append("\n");
                t = (Exception) t.getCause();
            }

            String message = sb.toString();
            int i = message.indexOf("{\"error\"");
            if (i > 0) {
                message = message.substring(i);
            }

            i = message.indexOf("400}");
            if (i > 0) {
                message = message.substring(0, i + 4);
            }

            return Result.buildFail("mapping不能创建索引，异常信息:" + message);

        } finally {
            if (!esIndexDAO.deleteIndex(clusterName, indexName)) {
                LOGGER.warn(
                    "class=TemplatePhyMappingManagerImpl||method=tryCreateIndex||msg=delete index error, indexName:{}",
                    indexName);
            }
        }
    }

    private Result<Void> updateMappingCore(String cluster, String template, String mappingStr, Set<String> removeFields,
                                           boolean doMerge) {
        try {

            Result<MappingConfig> result = AriusIndexMappingConfigUtils.parseMappingConfig(mappingStr);
            if (result.failed()) {
                return Result.buildFrom(result);
            }

            MappingConfig mappings = result.getData();

            if (mappings.haveDefault()) {
                return Result.build(ResultType.FAIL.getCode(), "mapping have _default_ type");
            }

            return updateMapping(cluster, template, mappings, removeFields, doMerge);
        } catch (JSONException e) {
            return Result.build(ResultType.FAIL.getCode(), JSON_PARSE_ERROR_TIPS);
        } catch (Exception t) {
            return Result.build(ResultType.FAIL.getCode(), t.getMessage());
        }
    }

    private Result<Void> updateMapping(String cluster, String name, MappingConfig mappings, Set<String> removeFields,
                                       boolean doMerge) {
        Result<Void> result = checkMapping(cluster, name, mappings);
        if (result.failed()) {
            return result;
        }

        TemplateConfig templateConfig = templateService.syncGetTemplateConfig(cluster, name);
        if (templateConfig == null) {
            return Result.buildFail("模版不存在，tamplate:" + name);
        }

        if (doMerge) {
            templateConfig.getMappings().merge(mappings);
        } else {
            mappings.mergeDefault(templateConfig.getMappings());
            templateConfig.setMappings(mappings);
        }

        if (CollectionUtils.isNotEmpty(removeFields)) {
            for (String removeField : removeFields) {
                templateConfig.getMappings().delFields(Arrays.asList(removeField.split("\\.")));
            }
        }

        try {
            if (!templateService.syncUpdateTemplateConfig(cluster, name, templateConfig,
                AdminESOpRetryConstants.DEFAULT_RETRY_COUNT)) {
                return Result.buildFail("更新模板mapping失败，请稍后重试");
            }
        } catch (ESOperateException e) {
            return Result.buildFail("更新模板mapping失败，errMsg: " + e.getMessage());
        }

        return Result.buildSucc();
    }

    /**
     * 检测模版字段数是否超标 超过则记录日志
     * @param cluster 物理集群名称
     * @param template 模板名称
     * @param templateMappingConfig 模板mapping
     * @param fieldLimitSize 长度限制
     * @return true/false 是否超标
     */
    private boolean checkMappingFieldSize(String cluster, String template, MappingConfig templateMappingConfig,
                                          Integer fieldLimitSize) {
        Map<String, TypeConfig> typeConfigMap = templateMappingConfig.getMapping();

        for (TypeConfig typeConfig : typeConfigMap.values()) {
            if (typeConfig.getProperties() != null && typeConfig.getProperties().getJsonMap().size() > fieldLimitSize) {
                LOGGER.warn(
                    "class=TemplatePhyMappingManagerImpl||method=checkMappingFieldSize||cluster={}||template {} mapping size is {}",
                    cluster, template, typeConfig.getProperties().getJsonMap().size());
                return true;
            }
        }
        return false;
    }
}