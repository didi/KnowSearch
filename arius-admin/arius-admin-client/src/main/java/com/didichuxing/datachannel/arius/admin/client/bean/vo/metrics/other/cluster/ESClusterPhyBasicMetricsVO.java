package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.other.cluster;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-07-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("物理集群基础指标信息")
public class ESClusterPhyBasicMetricsVO implements Serializable {
    /******************************************1.状态栏信息*****************************************/

    @ApiModelProperty("集群状态, green yellow red")
    private String  status;

    @ApiModelProperty("集群节点数量")
    private Long    numberNodes;

    @ApiModelProperty("活跃节点数量")
    private Long    activeNodeNu;

    @ApiModelProperty("无效节点数量")
    private Long    invalidNodeNu;

    @ApiModelProperty("无效节点数量")
    private Integer totalNodeNu;

    @ApiModelProperty("活跃节点百分比")
    private Double  activeNodeNuPercent;

    @ApiModelProperty("无效节点百分比")
    private Double  invalidNodeNuPercent;

    @ApiModelProperty("集群模板数")
    private Long totalTemplateNu;

    @ApiModelProperty("集群索引数")
    private Long    totalIndicesNu;

    @ApiModelProperty("集群shard数量")
    private Long    shardNu;

    @ApiModelProperty("集群文档数")
    private Long    totalDocNu;

    @ApiModelProperty("已用内存大小")
    private Long  memUsed;

    @ApiModelProperty("剩余空闲内存大小")
    private Long  memFree;

    @ApiModelProperty("总内存大小")
    private Long  memTotal;

    @ApiModelProperty("已用堆内存大小")
    private Long heapMemUsed;

    @ApiModelProperty("剩余堆内存大小")
    private Long heapMemFree;

    @ApiModelProperty("总堆内存大小")
    private Long heapMemTotal;

    @ApiModelProperty("堆内存使用率")
    private Double  heapUsage;

    @ApiModelProperty("堆内存空闲率")
    private Double  heapFreeUsage;

    @ApiModelProperty("已用内存百分比")
    private Double    memUsedPercent;

    @ApiModelProperty("剩余空闲内存百分比")
    private Double    memFreePercent;

    @ApiModelProperty("集群已使用容量，bytes 单位")
    private Long  storeSize;

    @ApiModelProperty("集群总容量，bytes 单位")
    private Long  totalStoreSize;

    @ApiModelProperty("集群空余容量，bytes 单位")
    private Long  freeStoreSize;

    @ApiModelProperty("磁盘使用率")
    private Double  storeUsage;

    @ApiModelProperty("磁盘空闲率")
    private Double  storeFreeUsage;

    @ApiModelProperty("master节点数量")
    private Long    numberMasterNodes;

    @ApiModelProperty("dataNode数量")
    private Long    numberDataNodes;

    @ApiModelProperty("client节点数量")
    private Long    numberClientNodes;

    @ApiModelProperty("Ingest节点数量")
    private Long    numberIngestNodes;

    @ApiModelProperty("CoordinatingOnly节点数量")
    private Long    numberCoordinatingOnlyNodes;
}
