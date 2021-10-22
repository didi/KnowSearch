package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: D10865
 * @description: 错误dsl模板详情
 * @date: Create on 2019/1/15 下午8:24
 * @modified By D10865
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDslDetail {
    /**
     * dsl查询模板MD5
     */
    private String dslTemplateMd5;
    /**
     * DSL查询模板
     */
    private String dslTemplate;
    /**
     * 异常索引名称
     */
    private String indices;
    /**
     * 异常dsl语句
     */
    private String dsl;
    /**
     * 次数
     */
    private Long count;
}
