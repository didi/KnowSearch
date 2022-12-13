package com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据迁移任务指标统计
 *
 * @author didi
 * @date 2022/11/02
 */
@NoArgsConstructor
@Data
public class ESIndexMoveTaskStats {
    private Long                           costTime;
    private Long                           failedDocumentNum;
    private List<FailedShardInfoStatusDTO> failedShardInfoStatus;
    private Boolean                        interruptMark;
    private String                         key;
    private Long                           readFileRateLimit;
    private Long                        shardNum;
    private String                         sourceCluster;
    private String                         sourceIndex;
    private String                         status;
    private Long                           succDocumentNum;
    private Long                        succShardNum;
    private String                         targetCluster;
    private String                         targetIndex;
    private String                         taskId;
    private Long                           totalDocumentNum;

    @NoArgsConstructor
    @Data
    public static class FailedShardInfoStatusDTO {
        private String  detail;
        private List<?> failedShardDataPaths;
        private String  ip;
    }
}
