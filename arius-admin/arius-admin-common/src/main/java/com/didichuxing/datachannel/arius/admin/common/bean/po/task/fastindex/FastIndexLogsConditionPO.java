package com.didichuxing.datachannel.arius.admin.common.bean.po.task.fastindex;

import lombok.Data;

import java.util.Date;

@Data
public class FastIndexLogsConditionPO {
    /**
     * 当前任务ID
     */
    private Integer taskId;
    /**
     * fastdump任务ID
     */
    private String fastDumpTaskId;
    /**
     * 执行节点
     */
    private String  executionNode;
    /**
     * 索引名称
     */
    private String  indexName;
    /**
     * 模版名称
     */
    private String  templateName;
    /**
     * 日志级别：1.DEBUG 2.INFO 3.WARNING 4.ERROR 5.CRITICAL
     */
    private String  logLevel;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
}
