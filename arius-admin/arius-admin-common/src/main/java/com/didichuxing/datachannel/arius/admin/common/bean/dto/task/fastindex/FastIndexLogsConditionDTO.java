package com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex;

import java.util.Date;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;

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
public class FastIndexLogsConditionDTO extends PageDTO {
    @ApiModelProperty("当前任务ID")
    private Integer taskId;
    @ApiModelProperty("当前任务ID")
    private String fastDumpTaskId;
    @ApiModelProperty("执行节点")
    private String  executionNode;
    @ApiModelProperty("索引名称")
    private String  indexName;
    @ApiModelProperty("模版名称")
    private String  templateName;
    @ApiModelProperty("日志级别：1.DEBUG 2.INFO 3.WARNING 4.ERROR 5.CRITICAL")
    private String  logLevel;
    @ApiModelProperty("开始时间")
    private Date    startTime;
    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("排序字段 时间timeStamp")
    private String  sortTerm;

    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean", required = false)
    private Boolean orderByDesc = true;

    /**
     * 内部查询使用
     */
    private List<String> fastDumpTaskIdList;
}
