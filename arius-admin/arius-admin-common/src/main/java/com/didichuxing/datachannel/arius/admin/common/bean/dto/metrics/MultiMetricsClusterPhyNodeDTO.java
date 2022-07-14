package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by liuchengxiang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "指标信息")
public class MultiMetricsClusterPhyNodeDTO extends MetricsClusterPhyDTO {

    @ApiModelProperty("集群多个节点ip")
    private List<String> nodeNames;
}
