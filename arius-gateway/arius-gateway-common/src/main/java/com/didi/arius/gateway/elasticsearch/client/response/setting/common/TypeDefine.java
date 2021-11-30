package com.didi.arius.gateway.elasticsearch.client.response.setting.common;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.type.ESVersion;

public class TypeDefine {
    private JSONObject define;

    public TypeDefine(JSONObject root) {
        this.define = root;
    }

    public JSONObject toJson() {
        return define;
    }

    public JSONObject toJson(ESVersion version) {
        return TypeDefineOperator.toJson(define, version);
    }

    public void setDefine(JSONObject define) {
        this.define = define;
    }

    public JSONObject getDefine() {
        return define;
    }

    /**
     * 是否需要忽略mapping优化
     * @return
     */
    public boolean isNotOptimze() {
        return TypeDefineOperator.isNotOptimze(define);
    }

    /**
     * 获取类型
     *
     * @return
     */
    public String getType() {
        return TypeDefineOperator.getType(define);
    }

    public boolean isIndexOff() {
        return TypeDefineOperator.isIndexOff(define);
    }

    /**
     * 设置成不检索
     *
     */
    public void setIndexOff() {
        TypeDefineOperator.setIndexOff(define);
    }

    /**
     * 设置成检索
     *
     */
    public void setIndexOn() {
        TypeDefineOperator.setIndexOn(define);
    }

    public boolean isDocValuesOff() {
        return TypeDefineOperator.isDocValuesOff(define);
    }

    /**
     * 设置成不支持排序
     */
    public void setDocValuesOff() {
        TypeDefineOperator.setDocValuesOff(define);
    }

    /**
     * 设置成支持排序
     */
    public void setDocValuesOn() {
        TypeDefineOperator.setDocValuesOn(define);
    }

    @Override
    public boolean equals(Object obj) {
        return TypeDefineOperator.isEquals(define, obj);
    }
}
