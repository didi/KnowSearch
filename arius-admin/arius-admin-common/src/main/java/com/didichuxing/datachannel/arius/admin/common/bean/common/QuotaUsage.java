package com.didichuxing.datachannel.arius.admin.common.bean.common;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "配额使用情况")
public class QuotaUsage implements Serializable {

    private static final long serialVersionUID = -4616447110740302304L;
    /**
     * 实际的磁盘消耗
     */
    @ApiModelProperty("实际磁盘消耗(G)")
    private Double actualDiskG;

    /**
     * 实际的CPU消耗
     */
    @ApiModelProperty("实际CPU消耗")
    private Double actualCpuCount;

    /**
     * Quota的磁盘消耗
     */
    @ApiModelProperty("磁盘配额(G)")
    private Double quotaDiskG;

    /**
     * Quota的CPU消耗
     */
    @ApiModelProperty("CPU配额")
    private Double quotaCpuCount;

    /**
     * Quota的磁盘消耗率
     */
    @ApiModelProperty("磁盘配额利用率")
    private Double quotaDiskUsage;

    /**
     * Quota的CPU消耗率
     */
    @ApiModelProperty("CPU配额利用率")
    private Double quotaCpuUsage;

}
