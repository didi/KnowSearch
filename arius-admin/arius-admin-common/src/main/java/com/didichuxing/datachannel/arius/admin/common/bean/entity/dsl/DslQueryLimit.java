package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

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
public class DslQueryLimit extends DslBase {

    @ApiModelProperty(value = "查询语句限流值", example = "50")
    private Double queryLimit;

    public DslQueryLimit() {
    }

    public DslQueryLimit(Integer appid, String dslTemplateMd5, Double queryLimit) {
        super(appid, dslTemplateMd5);
        this.queryLimit = queryLimit;
    }


    public Double getQueryLimit() {
        return queryLimit;
    }

    public DslQueryLimit setQueryLimit(Double queryLimit) {
        this.queryLimit = queryLimit;
        return this;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
