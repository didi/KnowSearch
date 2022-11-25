package com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FastDumpTaskLogVO {

    private String failedLuceneDataPath;
    private String ip;
    private String level;
    private String message;
    private String sourceClusterName;
    private String sourceIndex;
    private String targetClusterName;
    private String targetIndex;
    private String taskId;
    private Long timestamp;
}
