package com.didi.cloud.fastdump.common.bean.stats;

import com.didi.cloud.fastdump.common.enums.TaskStatusEnum;

import lombok.Data;

/**
 * Created by linyunan on 2022/9/6
 */
@Data
public class SuccIndexMoveTaskStats extends BaseESMoveTaskStats {
    private String  sourceIndex;
    private String  sourceCluster;

    private String  targetIndex;
    private String  targetCluster;

    private String  status = TaskStatusEnum.SUCCESS.getValue();

    private Integer shardNum;
    private Integer succShardNum;
    private Long    totalDocumentNum;
    private Long    succDocumentNum;

    private Long    succTime;
    /**
     * 完成节点层面sinker任务耗时
     */
    private Long    costTime;

    @Override
    public String getKey() {
        return this.sourceIndex + "@" + this.sourceCluster + "@" + targetIndex + "@" + this.targetCluster;
    }
}
