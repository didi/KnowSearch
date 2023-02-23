package com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl;

import com.alibaba.fastjson.JSON;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/6/20 下午4:48
 * @Modified By
 * 查询模板限流值
 */
@ApiModel(value = "DslQueryLimit", description = "查询语句限流值参数")
public class DslQueryLimitDTO extends DslBaseDTO {

    @ApiModelProperty(value = "查询语句当前限流值", example = "50")
    private Double queryLimit;

    @ApiModelProperty(value = "查询语句上一次更改的限流值", example = "50")
    private Double queryLimitBefore;

    public DslQueryLimitDTO() {
    }

    public DslQueryLimitDTO(Integer projectId, String dslTemplateMd5, Double queryLimit) {
        super(projectId, dslTemplateMd5);
        this.queryLimit = queryLimit;
    }

    public DslQueryLimitDTO(Integer projectId, String dslTemplateMd5, Double queryLimit, Double queryLimitBefore) {
        super(projectId, dslTemplateMd5);
        this.queryLimit = queryLimit;
        this.queryLimitBefore = queryLimitBefore;
    }

    public Double getQueryLimit() {
        return queryLimit;
    }

    public DslQueryLimitDTO setQueryLimit(Double queryLimit) {
        this.queryLimit = queryLimit;
        return this;
    }

    public Double getQueryLimitBefore() {
        return queryLimitBefore;
    }

    public DslQueryLimitDTO setQueryLimitBefore(Double queryLimitBefore) {
        this.queryLimitBefore = queryLimitBefore;
        return this;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}