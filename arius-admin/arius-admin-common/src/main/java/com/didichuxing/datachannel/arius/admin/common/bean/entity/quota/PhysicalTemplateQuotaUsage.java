package com.didichuxing.datachannel.arius.admin.common.bean.entity.quota;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/4/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalTemplateQuotaUsage extends BaseEntity {

    /**
     * 集群
     */
    private String cluster;

    /**
     * 模板
     */
    private String template;

    /**
     * 实际的磁盘消耗
     */
    private Double actualDiskG;

    /**
     * 实际的CPU消耗
     */
    private Double actualCpuCount;

    /**
     * Quota的磁盘消耗
     */
    private Double quotaDiskG;

    /**
     * Quota的CPU消耗
     */
    private Double quotaCpuCount;

    /**
     * Quota的CPU消耗 热节点
     */
    private Double hotQuotaCpuCount;

}
