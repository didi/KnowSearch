package com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping;

import java.util.Set;

import com.didichuxing.datachannel.arius.admin.client.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;

/**
 * 物理模板的mapping服务
 * @author zhonghua
 */
public interface TemplatePhyMappingManager {

    /**
     * 更新模板mapping
     * @param cluster 集群
     * @param template 模板名字
     * @param mappings mappings
     * @return result
     */
    Result<Void> updateMapping(String cluster, String template, String mappings);

    Result<Void> updateMappingAndMerge(String cluster, String template, String mappings, Set<String> removeFields);

    /**
     * 校验模板mapping 模板还不存在
     *
     * @param template 模板名字
     * @param ariusTypeProperty 属性列表
     * @return Result
     */
    Result<Void> checkMappingForNew(String template, AriusTypeProperty ariusTypeProperty);

    /**
     * 校验模板mapping 模板已经存在
     * @param cluster 集群
     * @param template 模板名字
     * @param mappings mapping
     * @return result
     */
    Result<Void> checkMapping(String cluster, String template, String mappings, boolean doMerge);

    /**
     * 获取模板mapping
     * @param cluster 集群
     * @param template 模板
     * @return result
     */
    Result<MappingConfig> getMapping(String cluster, String template);

    /**
     * 更新索引mapping
     * @param cluster 集群
     * @param expression 模板表达式
     * @param dataFormat 模板名中的时间格式
     * @return result
     */
    Result<Void> addIndexMapping(String cluster, String expression, String dataFormat, int updateDays,
                           MappingConfig mappingConfig);

    /**
     * 将模板mapping 更新到非滚动index上
     * @param cluster
     * @param index
     * @param mappingConfig
     * @return
     */
    Result<Void> syncTemplateMapping2Index(String cluster, String index, MappingConfig mappingConfig);

    /**
     * 将index的mapping同步到template上
     * @param cluster 集群
     * @param template 模板
     * @param expression 模板表达式
     * @param dataFormat 模板名中的时间格式
     * @return result
     */
    Result<MappingConfig> syncMappingConfig(String cluster, String template, String expression, String dataFormat);
}
