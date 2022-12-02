package com.didi.cloud.fastdump.common.bean.stats;

import java.util.List;

import com.didi.cloud.fastdump.common.enums.TaskStatusEnum;

import lombok.Data;

/**
 * Created by linyunan on 2022/9/6
 */
@Data
public class TemplateMoveTaskStats extends BaseESMoveTaskStats {
    /***************************节点的索引信息****************************/
    private String                   sourceTemplate;
    private String                   sourceCluster;
    private String                   targetTemplate;
    private String                   targetCluster;

    /***************************各个节点层面信息****************************/
    /**
     * 写入速率
     */
    private Long                     globalReadFileRateLimit;

    /**
     * 内核计算出的读取文件限流值
     */
    private Long                     kernelEstimationReadFileRateLimit;

    /*********************************任务状态***************************************/
    /**
     * 成功、进行中、失败
     * @see TaskStatusEnum
     */
    private String                   status;

    private Long                     costTime;

    private Integer                  totalIndexNum;
    private Integer                  totalShardNum;
    private Integer                  succIndexNum;
    private Integer                  succShardNum;
    private Long                     totalDocNum;
    private Long                     succDocNum;

    private List<IndexMoveTaskStats> indexMoveTaskStatsDetails;

    /**
     * 提交的索引迁移任务id
     */
    private List<String>             submitIndexMoveTaskIds;

    @Override
    public String getKey() {
        return this.sourceTemplate + "@" + this.sourceCluster + "@" + this.targetTemplate + "@" + this.targetCluster;
    }
}
