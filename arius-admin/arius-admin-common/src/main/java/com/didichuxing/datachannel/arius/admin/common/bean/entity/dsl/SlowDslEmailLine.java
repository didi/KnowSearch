package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/10/24 下午3:48
 * @Modified By
 *
 * 慢查一行
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class SlowDslEmailLine {
    /**
     * 日期
     */
    private String date;
    /**
     * 次数
     */
    private Long   count;
    /**
     * 索引名称
     */
    private String indices;
    /**
     * 耗时
     */
    private Double cost;
    /**
     * 查询语句
     */
    private String dsl;

}