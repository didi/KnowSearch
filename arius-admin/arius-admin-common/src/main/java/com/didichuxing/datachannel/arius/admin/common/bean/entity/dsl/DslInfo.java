package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/2/14 下午5:21
 * @modified By D10865
 *
 * dsl查询语句
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "DslInfo", description = "查询语句")
public class DslInfo {
    /**
     * 查询语句，如：select * from A
     */
    private String dsl;
    /**
     * 查询语句描述
     */
    private String memo;
}
