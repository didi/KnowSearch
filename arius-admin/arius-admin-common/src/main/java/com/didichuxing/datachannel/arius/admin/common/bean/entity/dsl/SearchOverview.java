package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/1/15 下午8:20
 * @modified By D10865
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchOverview {
    /**
     * 查询总次数
     */
    private Long count;
    /**
     * 查询qps信息
     */
    private QueryQpsMetric qpsMetric;
    /**
     * 查询耗时分位图
     */
    private Map<String, Object> costQuantile;

}
