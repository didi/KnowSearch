package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/1/15 下午8:23
 * @modified By D10865
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDsls {

    /**
     * 异常查询总次数
     */
    private Long count;
    /**
     * 异常查询
     */
    private List<ErrorDslInfo> details;
}
