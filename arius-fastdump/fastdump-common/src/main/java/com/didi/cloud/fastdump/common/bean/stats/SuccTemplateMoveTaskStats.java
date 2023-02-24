package com.didi.cloud.fastdump.common.bean.stats;

import com.didi.cloud.fastdump.common.enums.TaskStatusEnum;

import lombok.Data;

/**
 * Created by linyunan on 2022/9/6
 */
@Data
public class SuccTemplateMoveTaskStats extends BaseESMoveTaskStats {
    /***************************节点的索引信息****************************/
    private String  sourceTemplate;
    private String  sourceCluster;
    private String  targetTemplate;
    private String  targetCluster;
    /**
     * 成功、进行中、失败
     * @see TaskStatusEnum
     */
    private String  status;

    private Integer totalIndexNum;
    private Integer totalShardNum;
    private Integer succIndexNum;
    private Integer succShardNum;
    private Long    totalDocNum;
    private Long    succDocNum;

    private Long    costTime;
    private Long    succTime;

    @Override
    public String getKey() {
        return this.sourceTemplate + "@" + this.sourceCluster + "@" + this.targetTemplate + "@" + this.targetCluster;
    }
}
