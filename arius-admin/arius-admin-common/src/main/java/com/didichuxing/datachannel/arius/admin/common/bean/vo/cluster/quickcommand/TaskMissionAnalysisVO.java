package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * task任务分析.
 *
 * @ClassName TaskMissionAnalysisVO
 * @Author gyp
 * @Date 2022/6/7
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskMissionAnalysisVO {
    @ApiModelProperty("节点名称")
    private String node;
    @ApiModelProperty("活动")
    private String action;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("开始时间")
    private Long startTimeInMillis;
    @ApiModelProperty("运行时间")
    private Integer runningTimeInNanos;

}