package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 3/11/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateMetrics {
    /**
     * 统计的时间戳，单位：毫秒
     */
    private Long   timestamp;

    /**
     * 集群名称
     */
    private String cluster;

    /**
     * arius-admin模板名称
     */
    private String template;

    /**
     * arius-admin模板id
     */
    private Long   templateId;

    /**
     * 分段(倒排table)数量
     */
    private Long   segmentNum;

    /**
     * Segements内存大小（MB）
     */
    private Double segmentMemSize;
}