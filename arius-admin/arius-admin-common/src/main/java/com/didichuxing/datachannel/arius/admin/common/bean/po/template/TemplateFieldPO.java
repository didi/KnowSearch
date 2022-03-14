package com.didichuxing.datachannel.arius.admin.common.bean.po.template;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/1/15 下午7:40
 * @modified By D10865
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateFieldPO extends BaseESPO {

    private Integer id;

    private String name;

    private String clusterName;

    private Map<String, String> templateFieldMap;

    private Integer fieldCount;

    @JSONField(serialize = false)
    private Set<String> keySet;
    /**
     * 更新状态，1为启用mapping优化 @UpdateMappingState
     */
    private Integer state;
    /**
     * 是否索引存储分离，1为分离，0不分离
     */
    private Integer sourceSeparated;
    /**
     * 可忽略mapping优化字段集合
     */
    private Set<String> ignoreFields;

    public TemplateFieldPO(int id, String name, String clusterName) {
        this.id = id;
        this.name = name;
        this.clusterName = clusterName;
    }

    public TemplateFieldPO(int id, String name, String clusterName, Map<String, String> templateFieldMap) {
        this.id = id;
        this.name = name;
        this.clusterName = clusterName;
        this.templateFieldMap = templateFieldMap;
    }

    public TemplateFieldPO(Integer id, String name, String clusterName, Map<String, String> templateFieldMap, Integer state) {
        this.id = id;
        this.name = name;
        this.clusterName = clusterName;
        this.templateFieldMap = templateFieldMap;
        this.state = state;
    }

    public void setTemplateFieldMap(Map<String, String> templateFieldMap) {
        this.templateFieldMap = templateFieldMap;
        if (templateFieldMap != null) {
            this.fieldCount = templateFieldMap.size();
        } else {
            this.fieldCount = 0;
        }
    }

    @JSONField(serialize = false)
    public Set<String> getKeySet() {
        if (null == keySet) {
            keySet = new HashSet<>();
            if (templateFieldMap != null) {
                keySet.addAll(templateFieldMap.keySet());
            }
        }
        return keySet;
    }

    @JSONField(serialize = false)
    public void setKeySet(Set<String> keySet) {
        this.keySet = keySet;
    }

    /**
     * 从其他map进行更新
     * that为从ES中获取到的对象(原来的值)，this目前收集到的字段信息(现有的值)
     *
     * @param that
     */
    @JSONField(serialize = false)
    public void updateFromTemplateFieldPO(TemplateFieldPO that) {
        Map<String, String> thatTemplateFieldMap = that.getTemplateFieldMap();
        state = that.state;

        if (templateFieldMap != null) {
            Iterator<Map.Entry<String, String>> entryIterator = templateFieldMap.entrySet().iterator();

            while (entryIterator.hasNext()) {
                Map.Entry<String, String> updateEntry = entryIterator.next();

                // 字段名为空则移除
                if (StringUtils.isBlank(updateEntry.getKey())) {
                    entryIterator.remove();
                    continue;
                }

                // 如果原来就有，则使用原来的值
                if (thatTemplateFieldMap != null && thatTemplateFieldMap.containsKey(updateEntry.getKey())) {
                    updateEntry.setValue(thatTemplateFieldMap.get(updateEntry.getKey()));
                }
            }
            fieldCount = templateFieldMap.size();
        } else {
            fieldCount = 0;
        }

        // 可忽略字段信息以ES中的为准
        ignoreFields = that.ignoreFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TemplateFieldPO that = (TemplateFieldPO) o;

        if (!id.equals(that.id)) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (clusterName != null ? !clusterName.equals(that.clusterName) : that.clusterName != null) {
            return false;
        }

        if (fieldCount != null ? !fieldCount.equals(that.fieldCount) : that.fieldCount != null) {
            return false;
        }

        if (state != null ? !state.equals(that.state) : that.state != null) {
            return false;
        }

        if (sourceSeparated != null ? !sourceSeparated.equals(that.sourceSeparated) : that.sourceSeparated != null) {
            return false;
        }

        if (!Objects.equals(ignoreFields, that.ignoreFields)) {
            return false;
        }

        if (templateFieldMap == null && that.templateFieldMap == null) {
            return true;
        }
        if (templateFieldMap != null || that.templateFieldMap != null) {
            return false;
        }

        // 仅比较map的个数和key与key之间比较
        if (templateFieldMap.size() != that.templateFieldMap.size()) {
            return false;
        }

        Set<String> key1 = new TreeSet<>();
        key1.addAll(templateFieldMap.keySet());

        Set<String> key2 = new TreeSet<>();
        key2.addAll(that.templateFieldMap.keySet());

        if (!StringUtils.join(key1, AdminConstant.COMMA).equals(StringUtils.join(key2, AdminConstant.COMMA))) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    @JSONField(serialize = false)
    @Override
    public String getKey() {
        return "" + id;
    }

    @Override
    public String getRoutingValue() {
        return null;
    }
}
