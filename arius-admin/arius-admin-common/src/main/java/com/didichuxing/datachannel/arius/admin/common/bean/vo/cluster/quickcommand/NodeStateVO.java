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
    private String nodeName;
    @ApiModelProperty("segments大小")
    private Integer segmentsMemory;
    @ApiModelProperty("cpu占用")
    private Integer osCpu;
    @ApiModelProperty("load_average_1m")
    private BigDecimal loadAverage1m;
    @ApiModelProperty("load_average_5m")
    private BigDecimal loadAverage5m;
    @ApiModelProperty("load_average_15m")
    private BigDecimal loadAverage15m;
    @ApiModelProperty("jvm堆内存使用率")
    private Integer jvmHeapUsedPercent;
    @ApiModelProperty("线程数量")
    private Integer threadsCount;
    @ApiModelProperty("currentOpen")
    private Integer currentOpen;
    @ApiModelProperty("线程池写活跃数")
    private Integer threadPoolWriteActive;
    @ApiModelProperty("线程池写队列数")
    private Integer threadPoolWriteQueue;
    @ApiModelProperty("线程池写拒绝数")
    private Integer threadPoolWriteReject;
    @ApiModelProperty("线程池搜索活跃数")
    private Integer threadPoolSearchActive;
    @ApiModelProperty("线程池搜索队列数")
    private Integer threadPoolSearchQueue;
    @ApiModelProperty("线程池搜索拒绝数")
    private Integer threadPoolSearchReject;
    @ApiModelProperty("线程池管理活跃数")
    private Integer threadPoolManagementActive;
    @ApiModelProperty("线程池管理队列数")
    private Integer threadPoolManagementQueue;
    @ApiModelProperty("线程池管理拒绝数")
    private String threadPoolManagementReject;
}