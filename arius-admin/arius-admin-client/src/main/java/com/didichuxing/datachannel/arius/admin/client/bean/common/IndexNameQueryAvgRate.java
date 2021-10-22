package com.didichuxing.datachannel.arius.admin.client.bean.common;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
public class IndexNameQueryAvgRate {

    /**
     * 索引名称
     */
    private String indexName;

    /**
     * 访问次数
     */
    private Double queryTotalRate;

    /**
     * 统计日期 yyyy-MM-dd
     */
    private String date;

}
