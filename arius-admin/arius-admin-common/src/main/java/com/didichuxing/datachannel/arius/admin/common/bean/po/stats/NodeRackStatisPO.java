package com.didichuxing.datachannel.arius.admin.common.bean.po.stats;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description ="节点rack统计信息")
public class NodeRackStatisPO {
    /**
     * 集群
     */
    @ApiModelProperty(value = "集群名称")
    private String  cluster;
    /**
     * rack名字
     */
    @ApiModelProperty(value = "rack名字")
    private String  name;
    /**
     * 磁盘空闲空间
     */
    @ApiModelProperty(value = "磁盘空闲空间，单位:GB")
    private Double  diskFreeG;
    /**
     * 磁盘总空间
     */
    @ApiModelProperty(value = "磁盘总的空间，单位:GB")
    private Double  totalDiskG;
    /**
     * 文档个数
     */
    @ApiModelProperty(value = "文档个数")
    private long    docNu;
    /**
     * cpu使用率
     */
    @ApiModelProperty(value = "cpu使用率")
    private Double  cpuUsedPercent;
    /**
     * 文档总数
     */
    @ApiModelProperty(value = "文档总数")
    private long    indexNu;
}
