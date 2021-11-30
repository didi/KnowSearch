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
public class TemplateMetric {

    /**
     * 集群
     */
    private String cluster;

    /**
     * 模板名字
     */
    private String template;

    /***************************************** AMS指标 ****************************************************/

    /**
     * 模板总的磁盘消耗  单位G
     */
    private Double sumIndexSizeG;

    /**
     * 模板最大的索引磁盘消耗  单位G
     */
    private Double maxIndexSizeG;

    /**
     * 总条数
     */
    private Long   sumDocCount;

    /**
     * 模板最大的索引的文档个数
     */
    private Long   maxIndexDocCount;

    /**
     * tps峰值 单位 W/s
     */
    private Double maxTps;

    /**
     * 查询的峰值 ms
     */
    private Double maxQueryTime;

    /**
     * scroll的峰值 ms
     */
    private Double maxScrollTime;

}
