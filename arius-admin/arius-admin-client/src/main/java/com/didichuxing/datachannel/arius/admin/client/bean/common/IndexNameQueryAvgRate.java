package com.didichuxing.datachannel.arius.admin.client.bean.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
