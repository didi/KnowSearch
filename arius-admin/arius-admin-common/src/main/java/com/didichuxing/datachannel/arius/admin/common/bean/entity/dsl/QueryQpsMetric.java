package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/1/15 下午8:16
 * @modified By D10865
 *
 * 查询qps统计信息
 */
@Data
@AllArgsConstructor
public class QueryQpsMetric {

    /**
     * 最大qps
     */
    private Long maxQps;
    /**
     * 最大qps发生时刻
     */
    private String maxQpsTime;
    /**
     * 平均qps
     */
    private Long avgQps;
    /**
     * 最小qps
     */
    private Long minQps;
    /**
     * 最小qps发生时刻
     */
    private String minQpsTime;

    public QueryQpsMetric() {
        maxQps = avgQps = minQps = 0L;
        maxQpsTime = minQpsTime = "";
    }
}
