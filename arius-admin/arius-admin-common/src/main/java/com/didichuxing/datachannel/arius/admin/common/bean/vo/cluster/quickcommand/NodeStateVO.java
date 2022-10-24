package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 详细介绍类情况.
 *
 * @ClassName indicesVO
 * @Author gyp
 * @Date 2022/6/1
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeStateVO {
    @ApiModelProperty("节点名称")
    private String  nodeName;
    @ApiModelProperty("segments大小")
    private Long    segmentsMemory;
    @ApiModelProperty("cpu占用")
    private Integer osCpu;
    @ApiModelProperty("load_average_1m")
    private double  loadAverage1m;
    @ApiModelProperty("load_average_5m")
    private double  loadAverage5m;
    @ApiModelProperty("load_average_15m")
    private double  loadAverage15m;
    @ApiModelProperty("jvm堆内存使用率")
    private long    jvmHeapUsedPercent;
    @ApiModelProperty("线程数量")
    private long    threadsCount;
    @ApiModelProperty("currentOpen")
    private long    currentOpen;
    @ApiModelProperty("线程池写活跃数")
    private long    threadPoolWriteActive;
    @ApiModelProperty("线程池写队列数")
    private long    threadPoolWriteQueue;
    @ApiModelProperty("线程池写拒绝数")
    private long    threadPoolWriteReject;
    @ApiModelProperty("线程池搜索活跃数")
    private long    threadPoolSearchActive;
    @ApiModelProperty("线程池搜索队列数")
    private long    threadPoolSearchQueue;
    @ApiModelProperty("线程池搜索拒绝数")
    private long    threadPoolSearchReject;
    @ApiModelProperty("线程池管理活跃数")
    private long    threadPoolManagementActive;
    @ApiModelProperty("线程池管理队列数")
    private long    threadPoolManagementQueue;
    @ApiModelProperty("线程池管理拒绝数")
    private long    threadPoolManagementReject;
}