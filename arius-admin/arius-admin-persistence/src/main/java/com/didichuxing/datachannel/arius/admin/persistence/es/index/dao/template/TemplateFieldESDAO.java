package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateFieldPO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
@NoArgsConstructor
public class TemplateFieldESDAO extends BaseESDAO {

    /**
     * 索引名称
     */
    private String indexName;
    /**
     * type名称
     */
    private String typeName = "type";
    /**
     * 滚动查询大小
     */
    private int scrollSize = 500;

    /**
     * 忽略优化字段集合
     */
    private Set<String> ignoreFieldSet = new HashSet<>();

    @PostConstruct
    public void initJob() {
        this.indexName = dataCentreUtil.getAriusTemplateField();
    }

    /**
     * 批量保存索引模板字段分析结果
     *
     * @return
     */
    public boolean batchInsert(List<TemplateFieldPO> list) {
        return updateClient.batchInsert(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, list);
    }

    /**
     * 删除多余的索引模板统计记录
     *
     * @param idSet
     * @return
     */
    public boolean removeDiffIndexTemplate(Set<Integer> idSet) {

        String queryDsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_TEMPLATE_ID, scrollSize);

        List<String> needDeleteIdList = Lists.newArrayList();

        gatewayClient.queryWithScroll(indexName,
                typeName, queryDsl, scrollSize, null, TemplateFieldPO.class, resultList -> {
                    if (resultList != null) {
                        for (TemplateFieldPO TemplateFieldPO : resultList) {
                            // es中存在，而数据库中不存在，则加入待删除集合
                            if (!idSet.contains(TemplateFieldPO.getId())) {
                                needDeleteIdList.add(TemplateFieldPO.getId().toString());
                            }
                        }
                    }
                } );

        if (needDeleteIdList.isEmpty()) {
            return true;
        }

        return updateClient.batchDelete(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, needDeleteIdList);
    }

    /**
     * 批量设置可忽略字段
     *
     * @param list
     * @return
     */
    public boolean batchInitIgnoreField(List<TemplateFieldPO> list) {
        for (TemplateFieldPO TemplateFieldPO : list) {
            TemplateFieldPO.setIgnoreFields(ignoreFieldSet);
        }

        return batchInsert(list);
    }

    /**
     * 更新可忽略字段
     *
     * @param id
     * @param fieldName
     * @param isIgnoreImprove
     * @return
     */
    public boolean updateIgnoreFieldById(Integer id, String fieldName, boolean isIgnoreImprove) {
        TemplateFieldPO templateFieldPO = getTemplateFieldById(id);
        if (null == templateFieldPO) {
            return false;
        }

        Set<String> ignoreFields = Sets.newHashSet();
        if (null != templateFieldPO.getIgnoreFields()) {
            ignoreFields.addAll(templateFieldPO.getIgnoreFields());
        }
        if (isIgnoreImprove) {
            ignoreFields.add(fieldName);
        } else {
            ignoreFields.remove(fieldName);
        }
        templateFieldPO.setIgnoreFields(ignoreFields);

        boolean operatorResult = batchInsert(Lists.newArrayList(templateFieldPO));
        // 手动执行刷新操作，为了快速得到操作结果
        updateClient.refreshIndex(EnvUtil.getWriteIndexNameByEnv(this.indexName));

        return operatorResult;
    }

    /**
     * 更新索引模板字段状态信息
     *
     * @param templateName
     * @param state
     * @return
     */
    public boolean updateTemplateFieldState(String templateName, Integer state) {

        String queryDsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_FIELD_BY_NAME, scrollSize, templateName);

        List<TemplateFieldPO> list = getTemplateFieldsByDsl(queryDsl);

        if (CollectionUtils.isEmpty(list)) {
            LOGGER.error("class=TemplateFieldEsDao||method=updateTemplateFieldState||templateName={}||state={}||errMsg=list is empty",
                    templateName, state);
            return false;
        }

        Iterator<TemplateFieldPO> iterator = list.iterator();

        while (iterator.hasNext()) {
            iterator.next().setState(state);
        }

        return updateClient.batchInsert(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, list);
    }

    /**
     * 更新索引模板字段状态信息
     *
     * @param templateNames
     * @param state
     * @return
     */
    public boolean updateTemplateFields(List<String> templateNames, Integer state) {
        StringBuilder sourceSb = new StringBuilder(128);
        boolean isFirst = true;
        for (String field : templateNames) {
            if (isFirst) {
                isFirst = false;
                sourceSb.append(String.format("\"%s\"", field));
            } else {
                sourceSb.append(",").append(String.format("\"%s\"", field));
            }
        }

        String queryDsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_FIELD_BY_NAMES, scrollSize, sourceSb.toString());

        List<TemplateFieldPO> list = getTemplateFieldsByDsl(queryDsl);

        if (CollectionUtils.isEmpty(list)) {
            LOGGER.error("class=TemplateFieldEsDao||method=updateTemplateFieldState||templateNames={}||state={}||errMsg=list is empty",
                    templateNames, state);
            return false;
        }

        Iterator<TemplateFieldPO> iterator = list.iterator();

        while (iterator.hasNext()) {
            iterator.next().setState(state);
        }

        return updateClient.batchInsert(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, list);
    }

    /**
     * 根据id获取索引模板字段信息
     *
     * @param id
     * @return
     */
    public TemplateFieldPO getTemplateFieldById(Integer id) {

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_FIELD_BY_KEY, id);

        return gatewayClient.performRequestAndTakeFirst(indexName, typeName, dsl, TemplateFieldPO.class);
    }

    /**
     * 根据索引模板名称获取索引模板字段信息
     *
     * @param templateName
     * @return
     */
    public TemplateFieldPO getFieldByTemplateNameAndClusterName(String templateName, String clusterName) {

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_FIELD_BY_NAME_CLUSTERNAME, templateName, clusterName);

        return gatewayClient.performRequestAndTakeFirst(indexName, typeName, dsl, TemplateFieldPO.class);
    }

    /**
     * 获取所有索引模板字段信息
     *
     * @return
     */
    public List<TemplateFieldPO> getAllTemplateFields() {

        String queryDsl = String.format("{\"size\":%d}", scrollSize);

        return getTemplateFieldsByDsl(queryDsl);
    }



    /**
     * 获取所有索引模板名称根据状态
     *
     * @param state
     * @return
     */
    public Set<String> getAllTemplateNameByState(Integer state) {
        Set<String> sets = Sets.newHashSet();

        String queryDsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_FIELD_BY_STATE, scrollSize, state);

        List<TemplateFieldPO> list = getTemplateFieldsByDsl(queryDsl);
        for (TemplateFieldPO TemplateFieldPO : list) {
            sets.add(TemplateFieldPO.getName());
        }

        return sets;
    }

    /**
     * 根据状态获取所有索引模板
     *
     * @param state
     * @return
     */
    public List<TemplateFieldPO> getAllTemplateFieldsByState(Integer state) {

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_FIELD_BY_STATE, scrollSize, state);

        return getTemplateFieldsByDsl(dsl);
    }

    /**
     * 比较然后更新
     *
     * @param indexTemplateMappingList
     * @return
     */
    public List<TemplateFieldPO> compareThenUpdate(List<TemplateFieldPO> indexTemplateMappingList) {

        List<TemplateFieldPO> needInsertDocList = Lists.newLinkedList();

        for (TemplateFieldPO indexTemplateMappingItem : indexTemplateMappingList) {

            // 通过查询模板来找到对应的数据，不能通过id获取，由于会存在索引模板跨集群迁移和主备集群双写索引
            TemplateFieldPO templateFieldPO = getFieldByTemplateNameAndClusterName(indexTemplateMappingItem.getName(), indexTemplateMappingItem.getClusterName());
            if (templateFieldPO == null) {
                LOGGER.info("class=TemplateFieldEsDao||method=compareThenUpdate||msg=template [{}] not found in es",
                        indexTemplateMappingItem.getName());
                // 在es中没有找到对应的索引模板数据，则加入队列中
                needInsertDocList.add(indexTemplateMappingItem);
                continue;
            }

            // 如果不相同，则需要覆盖更新，templateFieldMap中已经存在的字段的值以之前的为准，保持之前该字段的创建时间
            if (!templateFieldPO.equals(indexTemplateMappingItem)) {

                indexTemplateMappingItem.updateFromTemplateFieldPO(templateFieldPO);
                needInsertDocList.add(indexTemplateMappingItem);
            }
        }

        return needInsertDocList;
    }


    /**
     * 根据查询语句获取索引模板字段信息
     *
     * @param dsl
     * @return
     */
    private List<TemplateFieldPO> getTemplateFieldsByDsl(String dsl) {
        List<TemplateFieldPO> list = Lists.newArrayList();

        gatewayClient.queryWithScroll(indexName, typeName, dsl, scrollSize, null, TemplateFieldPO.class, resultList -> {
            if (resultList != null) {
                list.addAll(resultList);
            }
        } );

        return list;
    }
}
