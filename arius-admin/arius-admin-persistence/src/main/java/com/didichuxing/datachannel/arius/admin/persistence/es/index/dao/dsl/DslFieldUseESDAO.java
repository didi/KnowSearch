package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl;

import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslFieldUsePO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Component
@NoArgsConstructor
public class DslFieldUseESDAO extends BaseESDAO {

    /**
     * 查询中使用字段的索引名称
     */
    private String indexName;
    /**
     * type名称
     */
    private final String typeName = "type";
    /**
     * 滚动查询大小
     */
    private final int scrollSize = 1000;

    @PostConstruct
    public void init(){
        this.indexName = dataCentreUtil.getAriusDslFieldUse();
    }

    /**
     * 批量保存dsl查询中使用的字段结果到es
     *
     * @param poList
     * @return
     */
    public boolean bathInsert(List<DslFieldUsePO> poList) {

        return updateClient.batchInsert(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, poList);
    }

    public List<DslFieldUsePO> getAllDslFieldUse() {
        List<DslFieldUsePO> list = Lists.newLinkedList();
        String queryDsl = String.format("{\"size\":%d}", scrollSize);

        gatewayClient.queryWithScroll(indexName, typeName, queryDsl, scrollSize, null, DslFieldUsePO.class, resultList -> {
            if (resultList != null) {
                list.addAll(resultList);
            }
        } );

        return combineDslFieldUseList(list);
    }

    /**
     * 合并字段一个月使用情况汇总信息
     *
     * @param templateName
     * @return
     */
    public DslFieldUsePO getFieldUseSummeryInfoByTemplateName(String templateName) {
        // 获取一个月字段使用情况
        List<DslFieldUsePO> list = getFieldUseListByTemplateName(templateName);

        DslFieldUsePO dslFieldUse = null;
        if (CollectionUtils.isNotEmpty(list)) {
            // 取第一个
            dslFieldUse = list.get(0);
            // 合并成一个字段使用情况
            for (int i = 1; i < list.size(); ++i) {
                dslFieldUse.combineFieldUseCount(list.get(i));
            }
        }

        return dslFieldUse;
    }

    /**
     * 根据模板名称获取字段使用信息,最多获取30天
     *
     * @param templateName
     * @return
     */
    public List<DslFieldUsePO> getFieldUseListByTemplateName(String templateName) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_FIELD_USE_BY_TEMPLATE, templateName);

        List<DslFieldUsePO> hits = gatewayClient.performRequest(indexName, typeName, dsl, DslFieldUsePO.class);

        return combineDslFieldUseList(hits);
    }

    /**
     * 根据模板名称获取字段使用信息
     *
     * @param templateName
     * @return
     */
    public DslFieldUsePO getFieldUseInfoByTemplateName(String templateName) {

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_ONE_FIELD_USE_BY_TEMPLATE, templateName);

        List<DslFieldUsePO> hits = gatewayClient.performRequest(indexName, typeName, dsl, DslFieldUsePO.class);
        if (CollectionUtils.isEmpty(hits)) {
            return null;
        }
        hits = combineDslFieldUseList(hits);

        return hits.get(0);
    }



    /**
     * 合并字段使用情况
     *
     * @param hits
     * @return
     */
    private List<DslFieldUsePO> combineDslFieldUseList(List<DslFieldUsePO> hits) {
        List<DslFieldUsePO> result = Lists.newLinkedList();
        // 合并美东和国内统计结果
        if (CollectionUtils.isNotEmpty(hits)) {
            DslFieldUsePO beforeItem = null;
            String key = "";
            Map<String /* id_date */, DslFieldUsePO> map = Maps.newLinkedHashMap();
            for (DslFieldUsePO item : hits) {
                key = String.format("%d_%s", item.getId(), item.getDate());

                if (map.containsKey(key)) {
                    beforeItem = map.get(key);
                    // 合并notUseFields,recentCreateFields,selectFieldsCounter,sortByFieldsCounter,whereFieldsCounter,groupByFieldsCounter
                    item.combineDataWithPo(beforeItem);
                    map.put(key, item);
                } else {
                    map.put(key, item);
                }
            }
            result = Lists.newLinkedList(map.values());
        }

        return result;
    }
}
