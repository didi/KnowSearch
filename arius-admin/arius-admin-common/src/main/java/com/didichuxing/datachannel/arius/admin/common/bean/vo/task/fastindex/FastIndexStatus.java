package com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FastIndexStatus {

    private String                fastDumpTaskId;
    private String                sourceIndex;
    private String                status;
    @ApiModelProperty("任务读取速率")
    private Long                  readFileRateLimit;
    @ApiModelProperty("总文档数量")
    private Long                  totalDocumentNum;
    @ApiModelProperty("shard数量")
    private Long                  shardNum;
    @ApiModelProperty("成功文档数")
    private Long                  succDocumentNum;
    @ApiModelProperty("成功shard数")
    private Long                  succShardNum;
    @ApiModelProperty("失败文档数")
    private Long                  failedDocumentNum;
    private List<FastIndexStatus> childrenList;
}
