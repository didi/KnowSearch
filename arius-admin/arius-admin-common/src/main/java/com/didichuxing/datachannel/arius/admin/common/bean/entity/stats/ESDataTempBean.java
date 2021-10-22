package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import com.didichuxing.tunnel.util.log.util.HostUtil;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESDataTempBean implements CollectBean {
    public static final Integer INDEX = 1;
    public static final Integer NODE  = 2;

    public static final String  DIVIDEND    = "Dividend";
    public static final String  DIVISOR     = "Divisor";

    public ESDataTempBean(Double value){
        this.value = value;
    }

    /**
     * 维度 1 是索引  2 是节点
     */
    private Integer             dimension;
    /**
     * 时间戳 单位是毫秒
     */
    private long                timestamp;
    /**
     * 模板所在集群
     */
    private String              cluster;
    /**
     * 模板名称
     */
    private String              template;
    /**
     * 物理模板id
     */
    private long                templateId;
    /**
     * 逻辑模板id
     */
    private long                logicTemplateId;
    /**
     * 索引名称
     */
    private String              index;

    private String              type;
    /**
     * ams host
     */
    private String              host        = HostUtil.getHostName();
    /**
     * es节点名称
     */
    private String              node;
    /**
     * es节点ip
     */
    private String              ip;
    /**
     * es节点port
     */
    private String              port;
    /**
     * rack
     */
    private String              rack;
    /**
     * 模板shardNu
     */
    private long                shardNu;
    /**
     * 采集出来的原始值
     */
    private Double              value;
    /**
     * 指标名称,上报Odin用的
     */
    private String              valueName;
    /**
     * 衍生计算需要的参数
     */
    private Map<String, String> deriveParam = Maps.newHashMap();
    /**
     * 计算后的值
     */
    private String              computeValue;
    /**
     * 是否需要发送odin
     */
    private boolean             sendToOdin  = false;

    public String getKey() {
        return getKeyPre() + valueName;
    }

    /**
     * 此处不能带上monitorTimestamp2min(timestamp)
     * @return
     */
    public String getKeyPre() {
        if (Objects.equals(dimension, INDEX)) {
            return cluster + "@" + index + "@";
        }

        if (Objects.equals(dimension, NODE)) {
            return cluster + "@" + node + "@" + port + "@";
        }

        throw new IllegalArgumentException("dimension not know.");
    }

    public String getDeriverParamByKey(String key){
        return deriveParam.get(key);
    }
}
