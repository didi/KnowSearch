package com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author didi
 */
@Data
public class FastIndexStats {
    /**
     * arius_op_task表中的ID
     */
    private Integer              taskId;

    /**
     * 子任务类型：1.template 2.index
     */
    @ApiModelProperty("任务统计类型：0.total 1.template 2.index")
    private Integer              taskType;

    /**
     * 模版id
     */
    private Integer              templateId;

    /**
     * 模版名称
     */
    private String               templateName;

    /**
     * 索引名称
     */
    @ApiModelProperty("原索引名称")
    private String               indexName;
    private String               sourceIndex;

    /**
     * 索引的type，多个用','隔开
     */
    private String               indexTypes;

    /**
     * 目标索引名称
     */
    private String               targetIndexName;

    /**
     * fastDump内核任务Id，每个索引子任务拥有一个，重试任务时，重新设置该id
     */
    private String               fastDumpTaskId;

    /**
     * 状态：-1.未提交 0.等待执行 1.执行中 2.执行成功 3.执行失败 4.已取消
     */
    private Integer              taskStatus;

    @ApiModelProperty("任务读取速率")
    private BigDecimal           readFileRateLimit;
    @ApiModelProperty("数据迁移速率（个/S）")
    private BigDecimal                 indexMoveRate;
    @ApiModelProperty("总文档数量")
    private BigDecimal           totalDocumentNum;
    @ApiModelProperty("shard数量")
    private BigDecimal           shardNum;
    @ApiModelProperty("成功文档数")
    private BigDecimal           succDocumentNum;
    @ApiModelProperty("成功shard数")
    private BigDecimal           succShardNum;
    @ApiModelProperty("失败文档数")
    private BigDecimal           failedDocumentNum;

    /**
     * 任务耗时
     */
    @ApiModelProperty("任务耗时")
    private BigDecimal           taskCostTime;

    /**
     * 任务开始时间
     */
    private Date                 taskStartTime;

    /**
     * 任务结束时间
     */
    private Date                 taskEndTime;

    /**
     * 计划任务开始时间
     */
    private Date                 scheduledTaskStartTime;

    @ApiModelProperty("下层统计信息")
    private List<FastIndexStats> childrenList;
}
