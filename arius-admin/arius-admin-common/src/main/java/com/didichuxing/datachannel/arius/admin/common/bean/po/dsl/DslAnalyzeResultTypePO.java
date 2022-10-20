package com.didichuxing.datachannel.arius.admin.common.bean.po.dsl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ErrorDsl;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.SearchOverview;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.SlowDsl;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DslAnalyzeResultTypePO extends BaseESPO {

    /**
     * projectId
     */
    private Integer        projectId;
    /**
     * 分析所在日期
     */
    private String         date;
    /**
     * 访问gateway信息
     */
    private String         accessGatewayInfo;
    /**
     * 查询模板信息
     */
    private DslTemplate    dslTemplate;
    /**
     *  查询概述
     */
    private SearchOverview overview;
    /**
     * 慢查信息
     */
    private SlowDsl        slowDsl;
    /**
     * 异常查询
     */
    private ErrorDsl       errorDsl;
    /**
     * 数据类型
     */
    private String         ariusType;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    @JSONField(serialize = false)
    @Override
    public String getKey() {
        return String.format("%d_%s", projectId, date);
    }

    @Override
    public String getRoutingValue() {
        return null;
    }
}