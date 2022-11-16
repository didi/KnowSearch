package com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

/**
 * fast_index_task_info
 * @author 
 */
@Data
public class FastIndexTaskInfo implements Serializable {
    private Integer           id;

    /**
     * arius_op_task表中的ID
     */
    private Integer           taskId;

    /**
     * 子任务类型：1.template 2.index
     */
    private Integer           taskType;

    /**
     * 模版id
     */
    private Integer           templateId;

    /**
     * 模版名称
     */
    private String            templateName;

    /**
     * 索引名称
     */
    private String            indexName;

    /**
     * 索引的type，多个用','隔开
     */
    private String            indexTypes;

    /**
     * 目标索引名称
     */
    private String            targetIndexName;

    private String            mappings;

    private String            settings;

    /**
     * fastDump内核任务Id，每个索引子任务拥有一个，重试任务时，重新设置该id
     */
    private String            fastDumpTaskId;

    /**
     * 状态：-1.未提交 0.等待执行 1.执行中 2.执行成功 3.执行失败 4.已取消
     */
    private Integer           taskStatus;

    /**
     * 任务读取限流速率
     */
    private BigDecimal              readFileRateLimit;

    /**
     * 任务总文档数
     */
    private BigDecimal        totalDocumentNum;

    /**
     * shard数量
     */
    private BigDecimal           shardNum;

    /**
     * 成功迁移的文档数
     */
    private BigDecimal        succDocumentNum;

    /**
     * 成功shard数
     */
    private BigDecimal           succShardNum;

    /**
     * 失败文档数
     */
    private BigDecimal        failedDocumentNum;

    /**
     * 任务提交内核返回结果
     */
    private String            taskSubmitResult;

    /**
     * 任务耗时
     */
    private BigDecimal        taskCostTime;

    /**
     * 任务创建时间
     */
    private Date              createTime;

    /**
     * 任务更新时间
     */
    private Date              updateTime;

    /**
     * 任务开始时间
     */
    private Date              taskStartTime;

    /**
     * 任务结束时间
     */
    private Date              taskEndTime;

    /**
     * 计划任务开始时间
     */
    private Date              scheduledTaskStartTime;

    /**
     * 与内核交互最后一次内核返回结果
     */
    private String            lastResponse;

    private static final long serialVersionUID = 1L;
}