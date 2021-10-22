package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: D10865
 * @description: 错误dsl模板
 * @date: Create on 2019/1/15 下午8:23
 * @modified By D10865
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDslInfo {
    /**
     * 异常名称
     */
    private String name;
    /**
     * 异常次数
     */
    private Long count;
    /**
     * 异常详情
     */
    private List<ErrorDslDetail> details = null;
}
