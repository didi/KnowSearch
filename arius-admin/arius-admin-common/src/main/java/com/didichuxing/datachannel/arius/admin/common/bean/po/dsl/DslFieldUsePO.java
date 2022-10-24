package com.didichuxing.datachannel.arius.admin.common.bean.po.dsl;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslSearchFieldNameMetric;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DslFieldUsePO extends BaseESPO {

    private Long              id;

    private String            clusterName;

    private String            name;

    private String            date;

    private Long              fieldCount;
    /**
     * 数据中心
     */
    private String            dataCenter;

    private List<String>      notUseFields;

    /**
     * 最近创建的字段名称
     */
    private List<String>      recentCreateFields;

    private Map<String, Long> selectFieldsCounter;

    private Map<String, Long> whereFieldsCounter;

    private Map<String, Long> groupByFieldsCounter;

    private Map<String, Long> sortByFieldsCounter;

    /**
     * 更新统计字段值
     *
     * @param searchFieldNameMetric
     */
    public void updateFromSearchFieldMetric(DslSearchFieldNameMetric searchFieldNameMetric) {
        this.setSelectFieldsCounter(searchFieldNameMetric.getSelectFieldsCounter());
        this.setSortByFieldsCounter(searchFieldNameMetric.getSortByFieldsCounter());
        this.setGroupByFieldsCounter(searchFieldNameMetric.getGroupByFieldsCounter());
        this.setWhereFieldsCounter(searchFieldNameMetric.getWhereFieldsCounter());
    }

    /**
     * 使用空map
     */
    public void fillEmptyMap() {
        this.selectFieldsCounter = Maps.newHashMap();
        this.whereFieldsCounter = Maps.newHashMap();
        this.groupByFieldsCounter = Maps.newHashMap();
        this.sortByFieldsCounter = Maps.newHashMap();
    }

    /**
     * 合并notUseFields,recentCreateFields,selectFieldsCounter,sortByFieldsCounter,whereFieldsCounter,groupByFieldsCounter
     */
    public void combineDataWithPo(DslFieldUsePO other) {
        // 没有使用的字段，国内统计结果和国外统计结果求交集
        Set<String> notUseFieldSetsCenter1 = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(notUseFields)) {
            notUseFieldSetsCenter1.addAll(notUseFields);
        }
        Set<String> notUseFieldSetsCenter2 = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(other.getNotUseFields())) {
            notUseFieldSetsCenter2.addAll(other.getNotUseFields());
        }
        notUseFields = Lists.newArrayList(Sets.intersection(notUseFieldSetsCenter1, notUseFieldSetsCenter2));

        Set<String> recentCreateFieldsSets = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(recentCreateFields)) {
            recentCreateFieldsSets.addAll(recentCreateFields);
        }
        if (CollectionUtils.isNotEmpty(other.getRecentCreateFields())) {
            recentCreateFieldsSets.addAll(other.getRecentCreateFields());
        }
        recentCreateFields = Lists.newArrayList(recentCreateFieldsSets);

        // 合并字段使用次数
        combineFieldUseCount(other);
    }

    /**
     * 合并字段使用次数
     *
     * @param other
     */
    public void combineFieldUseCount(DslFieldUsePO other) {
        countFieldUse(other.getSelectFieldsCounter(), selectFieldsCounter);
        countFieldUse(other.getWhereFieldsCounter(), whereFieldsCounter);
        countFieldUse(other.getGroupByFieldsCounter(), groupByFieldsCounter);
        countFieldUse(other.getSortByFieldsCounter(), sortByFieldsCounter);
    }

    private void countFieldUse(Map<String, Long> otherFieldsCounter, Map<String, Long> fieldsCounter) {
        if (otherFieldsCounter != null) {
            for (Map.Entry<String, Long> entry : otherFieldsCounter.entrySet()) {
                if (fieldsCounter == null) {
                    fieldsCounter = new HashMap<>();
                }

                if (fieldsCounter.containsKey(entry.getKey())) {
                    Long counter = fieldsCounter.get(entry.getKey()) + entry.getValue();
                    fieldsCounter.put(entry.getKey(), counter);
                } else {
                    fieldsCounter.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public void removeBlankField() {
        selectFieldsCounter.remove("");
        whereFieldsCounter.remove("");
        groupByFieldsCounter.remove("");
        sortByFieldsCounter.remove("");
    }

    @JSONField(serialize = false)
    @Override
    public String getKey() {
        return String.format("%d_%s_%s", id, date, dataCenter);
    }

    @Override
    public String getRoutingValue() {
        return null;
    }
}
