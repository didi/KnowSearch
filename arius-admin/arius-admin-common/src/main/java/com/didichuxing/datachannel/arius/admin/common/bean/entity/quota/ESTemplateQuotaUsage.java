package com.didichuxing.datachannel.arius.admin.common.bean.entity.quota;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-09-03
 */
@Data
public class ESTemplateQuotaUsage extends BaseEntity {

    /**
     * 逻辑模板id
     */
    private Integer logicId;

    /**
     * 模板
     */
    private String  template;

    /**
     * 实际的磁盘消耗
     */
    private Double  actualDiskG;

    /**
     * 实际的CPU消耗
     */
    private Double  actualCpuCount;

    /**
     * Quota的磁盘消耗
     */
    private Double  quotaDiskG;

    /**
     * Quota的CPU消耗
     */
    private Double  quotaCpuCount;

    /**
     * Quota的磁盘消耗率
     */
    private Double  quotaDiskUsage;

    /**
     * Quota的CPU消耗率
     */
    private Double  quotaCpuUsage;

}
