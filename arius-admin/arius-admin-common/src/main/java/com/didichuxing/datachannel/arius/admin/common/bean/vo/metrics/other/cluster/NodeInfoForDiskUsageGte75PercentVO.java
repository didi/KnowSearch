package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by linyunan on 2021-08-09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("大于75%磁盘利用率节点列表")
public class NodeInfoForDiskUsageGte75PercentVO implements Serializable {

    @ApiModelProperty("大于75%磁盘利用率节点Ip列表")
    private String nodeIp;

    @ApiModelProperty("大于75%磁盘利用率节点名称列表")
    private String nodeName;

    @ApiModelProperty("利用率")
    private Double value;
}
