package com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 数据迁移任务日志查询类
 *
 * @author didi
 * @date 2022/10/25
 */
@Data
@ApiModel("数据迁移任务日志查询类")
public class FastIndexLogsConditionDTO {
    @ApiModelProperty("当前任务ID")
    private Integer taskId;
    @ApiModelProperty("执行节点")
    private String  executionNode;
    @ApiModelProperty("索引名称")
    private String  indexName;
    @ApiModelProperty("模版名称")
    private String  templateName;
    @ApiModelProperty("日志级别：1.DEBUG 2.INFO 3.WARNING 4.ERROR 5.CRITICAL")
    private String  logLevel;
    @ApiModelProperty("开始时间")
    private Long    startTime;
    @ApiModelProperty("结束时间")
    private Long    endTime;
}
