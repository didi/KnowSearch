package com.didichuxing.datachannel.arius.admin.common.bean.po.gateway;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2018/9/19 下午5:36
 * @modified By
 *
 * join 后的gateway日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayJoinPO extends BaseESPO {

    /**
     * 请求path
     */
    private String uri;
    /**
     * 请求id
     */
    private String requestId;
    /**
     * appid
     */
    private Integer appid;
    /**
     * 索引名称
     */
    private String indices;
    /**
     * type名称
     */
    private String typeName;
    /**
     * 查询命中索引信息json
     */
    private String index;
    /**
     * 查询语句
     */
    private String dsl;
    /**
     * 查询模板
     */
    private String dslTemplate;
    /**
     * 查询模板MD5
     */
    private String dslTemplateMd5;
    /**
     * 是否超时，"true"/"false"
     */
    private String isTimedOut;
    /**
     * 查询语句类型
     */
    private String dslType;
    /**
     * 查询方式,dsl/sql
     */
    private String searchType;
    /**
     * 查询es耗时
     */
    private Long esCost;
    /**
     * 查询总耗时
     */
    private Long totalCost;
    /**
     * 查询shard个数
     */
    private Long totalShards;
    /**
     * 查询总命中数
     */
    private Long totalHits;
    /**
     * 查询响应长度
     */
    private Long responseLen;
    /**
     * 错误名称
     */
    private String exceptionName;
    /**
     * 创建时间
     */
    private String ariusCreateTime;
    /**
     * timeStamp
     */
    private long timeStamp;
    /**
     * indiceSample
     */
    private String indiceSample;
    /**
     * 查询字段
     */
    private Map<String, Long> selectFields;
    /**
     * 过滤字段
     */
    private Map<String, Long> whereFields;
    /**
     * 聚合字段
     */
    private Map<String, Long> groupByFields;
    /**
     *排序字段
     */
    private Map<String, Long> orderByFields;
    /**
     * 多type索引查询映射后的索引名称
     */
    private String destIndexName;
    /**
     * 请求源ip
     */
    private String remoteAddr;

    @JSONField(name="selectFields")
    public void setSelectFields(String selectFields) {
        this.selectFields = Maps.newHashMap();
        splitFieldsThenAddMetric(selectFields, this.selectFields);
    }

    @JSONField(name="whereFields")
    public void setWhereFields(String whereFields) {
        this.whereFields = Maps.newHashMap();
        splitFieldsThenAddMetric(whereFields, this.whereFields);
    }

    @JSONField(name="groupByFields")
    public void setGroupByFields(String groupByFields) {
        this.groupByFields = Maps.newHashMap();
        splitFieldsThenAddMetric(groupByFields, this.groupByFields);
    }

    @JSONField(name="orderByFields")
    public void setOrderByFields(String orderByFields) {
        this.orderByFields = Maps.newHashMap();
        splitFieldsThenAddMetric(orderByFields, this.orderByFields);
    }

    public Map<String, Long> getGroupByFields() {
        return groupByFields;
    }

    public Map<String, Long> getOrderByFields() {
        return orderByFields;
    }

    public Map<String, Long> getWhereFields() {
        return whereFields;
    }

    public Map<String, Long> getSelectFields() {
        return selectFields;
    }

    /**
     * 重置字段使用次数
     * @param count
     */
    public void resetFieldUseCount(Long count) {
        setMapValue(this.selectFields,  count);
        setMapValue(this.whereFields,   count);
        setMapValue(this.groupByFields, count);
        setMapValue(this.orderByFields, count);
    }

    /**
     * 切分字符串，然后累加计数器
     *
     * @param selectField
     * @param fieldMap
     */
    private void splitFieldsThenAddMetric(String selectField, Map<String, Long> fieldMap) {
        String[] selectFieldArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(selectField, ",");
        for (String fieldName : selectFieldArray) {
            Long count = fieldMap.computeIfAbsent(fieldName, k -> 0L);
            ++count;
            fieldMap.put(fieldName, count);
        }
    }

    /**
     * 设置map中的value
     * @param fieldMap
     * @param value
     */
    private void setMapValue(Map<String, Long> fieldMap, Long value) {
        if (fieldMap == null) {
            return;
        }
        for (Map.Entry<String, Long> entry : fieldMap.entrySet()) {
            entry.setValue(value);
        }
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    // 没有更新场景，id可以为null
    @JSONField(serialize = false)
    @Override
    public String getKey() {
        return null;
    }

    @JSONField(serialize = false)
    @Override
    public String getRoutingValue() {
        return null;
    }
}
