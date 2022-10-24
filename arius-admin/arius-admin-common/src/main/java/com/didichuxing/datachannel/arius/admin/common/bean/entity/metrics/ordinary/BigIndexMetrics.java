package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Created by linyunan on 2021-08-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BigIndexMetrics implements Serializable {

    /**
     * 索引名称
     */
    private String               indexName;

    /**
     * 归属节点node名称
     */
    private List<IndexShardInfo> belongNodeInfo;

    /**
     * 文档数
     */
    private Long                 docsCount;
}
