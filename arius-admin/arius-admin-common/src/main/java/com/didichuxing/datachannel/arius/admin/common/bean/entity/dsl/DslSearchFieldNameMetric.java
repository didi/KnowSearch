package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayJoinPO;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DslSearchFieldNameMetric {

    /**
     * 索引名称
     */
    private String indexName;
    /**
     * 查询字段统计
     */
    private Map<String, Long> selectFieldsCounter = Maps.newHashMap();
    /**
     * 过滤字段统计
     */
    private Map<String, Long> whereFieldsCounter = Maps.newHashMap();
    /**
     * 聚合字段统计
     */
    private Map<String, Long> groupByFieldsCounter = Maps.newHashMap();
    /**
     * 排序字段统计
     */
    private Map<String, Long> sortByFieldsCounter = Maps.newHashMap();
    /**
     * 使用的字段集合
     */
    private Set<String> useFieldSet;

    public DslSearchFieldNameMetric(String indexName){
        this.indexName  = indexName;
    }

    /**
     * 获取使用的字段
     *
     * @return
     */
    public Set<String> getUseFieldSet() {
        if (useFieldSet == null) {
            useFieldSet = new LinkedHashSet<>();

            if (selectFieldsCounter != null) {
                useFieldSet.addAll(selectFieldsCounter.keySet());
            }
            if (whereFieldsCounter != null) {
                useFieldSet.addAll(whereFieldsCounter.keySet());
            }
            if (groupByFieldsCounter != null) {
                useFieldSet.addAll(groupByFieldsCounter.keySet());
            }
            if (sortByFieldsCounter != null) {
                useFieldSet.addAll(sortByFieldsCounter.keySet());
            }
        }
        return useFieldSet;
    }

    public void mergeSearchFieldNameMetric(DslSearchFieldNameMetric that) {
        if (that == null) {
            return;
        }

        for (Map.Entry<String, Long> entry : that.getSelectFieldsCounter().entrySet()) {
            if (selectFieldsCounter.containsKey(entry.getKey())) {
                Long counter = selectFieldsCounter.get(entry.getKey()) + entry.getValue();
                selectFieldsCounter.put(entry.getKey(), counter);
            } else {
                selectFieldsCounter.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<String, Long> entry : that.getWhereFieldsCounter().entrySet()) {
            if (whereFieldsCounter.containsKey(entry.getKey())) {
                Long counter = whereFieldsCounter.get(entry.getKey()) + entry.getValue();
                whereFieldsCounter.put(entry.getKey(), counter);
            } else {
                whereFieldsCounter.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<String, Long> entry : that.getGroupByFieldsCounter().entrySet()) {
            if (groupByFieldsCounter.containsKey(entry.getKey())) {
                Long counter = groupByFieldsCounter.get(entry.getKey()) + entry.getValue();
                groupByFieldsCounter.put(entry.getKey(), counter);
            } else {
                groupByFieldsCounter.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<String, Long> entry : that.getSortByFieldsCounter().entrySet()) {
            if (sortByFieldsCounter.containsKey(entry.getKey())) {
                Long counter = sortByFieldsCounter.get(entry.getKey()) + entry.getValue();
                sortByFieldsCounter.put(entry.getKey(), counter);
            } else {
                sortByFieldsCounter.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void mergeSearchFieldNameMetric(GatewayJoinPO that) {

        if (that == null) {
            return;
        }

        if (that.getSelectFields() != null) {
            for (Map.Entry<String, Long> entry : that.getSelectFields().entrySet()) {
                if (selectFieldsCounter.containsKey(entry.getKey())) {
                    Long counter = selectFieldsCounter.get(entry.getKey()) + entry.getValue();
                    selectFieldsCounter.put(entry.getKey(), counter);
                } else {
                    selectFieldsCounter.put(entry.getKey(), entry.getValue());
                }
            }
        }

        if (that.getWhereFields() != null) {
            for (Map.Entry<String, Long> entry : that.getWhereFields().entrySet()) {
                if (whereFieldsCounter.containsKey(entry.getKey())) {
                    Long counter = whereFieldsCounter.get(entry.getKey()) + entry.getValue();
                    whereFieldsCounter.put(entry.getKey(), counter);
                } else {
                    whereFieldsCounter.put(entry.getKey(), entry.getValue());
                }
            }
        }

        if (that.getGroupByFields() != null) {
            for (Map.Entry<String, Long> entry : that.getGroupByFields().entrySet()) {
                if (groupByFieldsCounter.containsKey(entry.getKey())) {
                    Long counter = groupByFieldsCounter.get(entry.getKey()) + entry.getValue();
                    groupByFieldsCounter.put(entry.getKey(), counter);
                } else {
                    groupByFieldsCounter.put(entry.getKey(), entry.getValue());
                }
            }
        }

        if (that.getOrderByFields() != null) {
            for (Map.Entry<String, Long> entry : that.getOrderByFields().entrySet()) {
                if (sortByFieldsCounter.containsKey(entry.getKey())) {
                    Long counter = sortByFieldsCounter.get(entry.getKey()) + entry.getValue();
                    sortByFieldsCounter.put(entry.getKey(), counter);
                } else {
                    sortByFieldsCounter.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
