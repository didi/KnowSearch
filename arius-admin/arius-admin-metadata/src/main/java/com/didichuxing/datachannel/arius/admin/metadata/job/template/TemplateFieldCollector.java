package com.didichuxing.datachannel.arius.admin.metadata.job.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateFieldPO;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateFieldESDAO;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.TypeDefine;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.MultiIndexsConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.MultiTemplatesConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.TemplateConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

/**
 * @author: D10865
 * @description: 采集索引模板字段信息
 * @date: Create on 2019/1/24 下午2:47
 * @modified By D10865
 */
@Component
public class TemplateFieldCollector extends AbstractMetaDataJob {

    /**
     * 操作arius.template.field索引
     */
    @Autowired
    private TemplateFieldESDAO templateFieldEsDao;

    @Autowired
    private TemplatePhyService templatePhyService;

    @Autowired
    private ESTemplateService esTemplateService;

    @Autowired
    private ESIndexService esIndexService;

    /**
     * 忽略优化字段集合
     */
    private Set<String> ignoreFieldSet = new HashSet<>();

    private static final FutureUtil futureUtil = FutureUtil.init("TemplateFieldCollector");

    /**
     * 处理采集任务
     *
     * @return
     */
    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=TemplateFieldCollector||method=handleJobTask||params={}", params);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("get index template info");

        // key -> clusterName, value -> indexTemplate po list
        Map<String, List<TemplateFieldPO>> templateMappingByClusterNameMap = getAllClusterIndexTemplate();
        // 获取所有id
        Set<Integer> idSet = Sets.newHashSet();
        int templateTotalCount = 0;
        for (Map.Entry<String, List<TemplateFieldPO>> entry : templateMappingByClusterNameMap.entrySet()) {
            templateTotalCount += entry.getValue().size();
            entry.getValue().forEach(item ->
                idSet.add(item.getId())
            );
        }

        stopWatch.stop().start("collect template field");

        List<TemplateFieldPO> indexTemplateMappingList = runTaskAndGetResult(templateMappingByClusterNameMap);

        boolean operatorResult = templateFieldEsDao.batchInsert(indexTemplateMappingList);

        // 删除es有，而从admin获取没有的索引模板；当索引模板在页面上被删除会出现
        boolean operatorRemoveResult = templateFieldEsDao.removeDiffIndexTemplate(idSet);

        LOGGER.info("class=TemplateFieldCollector||method=handleJobTask||msg=operatorResult [{}] templateTotalCount {} {}, operatorRemoveResult {}, cost {}",
                indexTemplateMappingList.size(), templateTotalCount, operatorResult, operatorRemoveResult, stopWatch.stop().toString());

        return JOB_SUCCESS;
    }

    /**************************************************** private method ****************************************************/

    /**
     * 获取所有集群索引模板
     *
     * @return
     */
    private Map<String, List<TemplateFieldPO>> getAllClusterIndexTemplate() {
        Map<String, List<TemplateFieldPO>> templateMappingByClusterNameMap = Maps.newHashMap();

        List<IndexTemplatePhy> templatePhies = templatePhyService.listTemplate();
        if(CollectionUtils.isEmpty(templatePhies)){return templateMappingByClusterNameMap;}

        for (IndexTemplatePhy indexTemplate : templatePhies) {
            templateMappingByClusterNameMap.computeIfAbsent(indexTemplate.getCluster(),
                    key -> Lists.newLinkedList()).add(
                            new TemplateFieldPO(indexTemplate.getId().intValue(),
                                                indexTemplate.getName(),
                                                indexTemplate.getCluster())
                    );
        }

        return templateMappingByClusterNameMap;
    }

    /**
     * 运行任务并获取结果
     *
     * @param templateMappingByClusterNameMap
     * @return
     */
    private List<TemplateFieldPO> runTaskAndGetResult(Map<String/*clusterName*/, List<TemplateFieldPO>> templateMappingByClusterNameMap) {
        // 每一个线程任务是获取一个集群的索引模板数据
        for (Map.Entry<String, List<TemplateFieldPO>> entry : templateMappingByClusterNameMap.entrySet()) {
            futureUtil.callableTask(() -> {
                List<TemplateFieldPO> indexTemplateMappingList = getIndexTemplateFieldNamesByClusterName(entry.getKey(), entry.getValue());
                return templateFieldEsDao.compareThenUpdate(indexTemplateMappingList);
            });
        }

        return futureUtil.waitResult();
    }

    /**
     * 根据集群名称获取该集群上的索引模板
     *
     * @param clusterName
     */
    private List<TemplateFieldPO> getIndexTemplateFieldNamesByClusterName(String clusterName, List<TemplateFieldPO> templateFieldPOS) {

        String markTime = DateTimeUtil.getCurrentFormatDateTime();
        TemplateFieldPO templateFieldPO;

        Iterator<TemplateFieldPO> templateFieldPOIterator = templateFieldPOS.iterator();

        while (templateFieldPOIterator.hasNext()) {
            templateFieldPO = templateFieldPOIterator.next();
            // 收集索引模板字段信息
            templateFieldPO.setTemplateFieldMap(collectTemplateFieldsInfo(clusterName, templateFieldPO, markTime));
            // 设置忽略字段信息
            templateFieldPO.setIgnoreFields(ignoreFieldSet);
        }

        return templateFieldPOS;
    }

    /**
     * 收集索引模板字段信息
     *
     * @param clusterName
     * @param templateFieldPO
     * @param markTime
     */
    private Map<String, String> collectTemplateFieldsInfo(String clusterName, TemplateFieldPO templateFieldPO, String markTime) {
        Map<String, String> templateFieldMap = Maps.newLinkedHashMap();
        Set<String> fieldNameSets = Sets.newLinkedHashSet();

        // 从索引模板中获取字段信息
        fieldNameSets.addAll(getFieldsFromIndexTemplate(clusterName, templateFieldPO.getName()));
        // 从索引mapping中获取字段信息
        fieldNameSets.addAll(getFieldsFromIndexMapping(clusterName, String.format("%s*", templateFieldPO.getName())));

        for (String fieldName : fieldNameSets) {
            // 字段名为空则移除
            if (StringUtils.isBlank(fieldName)) {
                continue;
            }

            templateFieldMap.put(fieldName, markTime);
        }

        if (templateFieldMap.isEmpty()) {
            LOGGER.info("class=TemplateFieldEsDao||method=collectTemplateFieldsInfo||clusterName={}||msg=template [{}] can't find field",
                    clusterName, templateFieldPO.getName());
        }

        return templateFieldMap;
    }

    /**
     * 从索引mapping中获取字段信息
     *
     * @param clusterName
     * @param indexName
     * @return
     */
    private Set<String> getFieldsFromIndexMapping(String clusterName, String indexName) {
        Set<String> fieldNameSets = Sets.newLinkedHashSet();

        MultiIndexsConfig multiIndexsConfig = esIndexService.syncGetIndexConfigs(clusterName, indexName);

        if (null == multiIndexsConfig) {
            return fieldNameSets;
        }

        for (Map.Entry<String, IndexConfig> entry : multiIndexsConfig.getIndexConfigMap().entrySet()) {
            fieldNameSets.addAll(getFieldNames(entry.getValue().getMappings()));
        }
        return fieldNameSets;
    }

    /**
     * 获取mapping中所有字段名 fieldName,不包含type这层，由于不同type的类型必须一致
     * @return
     */
    private Set<String> getFieldNames(MappingConfig mappingConfig) {
        Set<String> fieldNameSet = Sets.newLinkedHashSet();
        Map<String/*typeName*/, Map<String/*field*/, TypeDefine>> typeFieldMap = mappingConfig.getTypeDefines();
        for (Map.Entry<String, Map<String, TypeDefine>> entry : typeFieldMap.entrySet()) {
            for (Map.Entry<String, TypeDefine> typeDefineEntry : entry.getValue().entrySet()) {
                fieldNameSet.add(typeDefineEntry.getKey());
            }
        }

        return fieldNameSet;
    }

    /**
     * 从索引模板中获取字段信息
     *
     * @param clusterName
     * @param templateName
     * @return
     */
    private Set<String> getFieldsFromIndexTemplate(String clusterName, String templateName) {
        Set<String> fieldNameSets = Sets.newLinkedHashSet();

        MultiTemplatesConfig templatesConfig = esTemplateService.syncGetTemplates(clusterName, templateName);

        if (templatesConfig == null) {
            return fieldNameSets;
        }

        for (Map.Entry<String, TemplateConfig> entry : templatesConfig.getTemplateConfigMap().entrySet()) {
            fieldNameSets.addAll(entry.getValue().getFieldNames());
        }

        return fieldNameSets;
    }
}
