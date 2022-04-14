package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ESNodeToIndexTempBean implements CollectBean {
    /**
     * 单位：毫秒
     */
    private long                timestamp;

    private String              cluster;

    private String              node;

    private String              port;

    private String              template;

    /**
     * 物理模板id
     */
    private long                templateId;

    /**
     * 逻辑模板id
     */
    private long                logicTemplateId;

    private String              index;

    /**
     * 采集出来的原始值
     */
    private Double              value;

    private String              valueName;

    /**
     * 计算后的值
     */
    private String              computeValue;

    /**
     * 衍生计算需要的参数
     */
    private Map<String, String> deriveParam = Maps.newHashMap();

    public ESNodeToIndexTempBean() {
    }

    public ESNodeToIndexTempBean(Double value) {
        this.value = value;
    }

    public String getKey() {
        return getKeyPre() + valueName;
    }

    //此处不能带上monitorTimestamp2min(timestamp)
    public String getKeyPre() {
        return cluster + "@" + node + "@" + port + "@" + index + "@";
    }

    public String getDeriveParam(String name) {
        return deriveParam.get(name);
    }

    public Map<String, String> getDeriveParam() {
        return deriveParam;
    }

    public void setDeriveParam(Map<String, String> deriveParam) {
        this.deriveParam = deriveParam;
    }
}
