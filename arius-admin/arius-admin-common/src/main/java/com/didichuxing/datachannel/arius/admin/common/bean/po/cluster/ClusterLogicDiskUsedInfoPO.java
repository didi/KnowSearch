package com.didichuxing.datachannel.arius.admin.common.bean.po.cluster;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 详细介绍类情况.
 *
 * @ClassName ClusterLogicDiskUsedInfo
 * @Author gyp
 * @Date 2022/5/30
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterLogicDiskUsedInfoPO {
    @ApiModelProperty("磁盘使用率")
    private Double diskUsagePercent;

    @ApiModelProperty("磁盘总量")
    private Long   diskTotal;

    @ApiModelProperty("磁盘使用量")
    private Long   diskUsage;
}