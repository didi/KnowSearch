package com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("数据迁移任务简要VO")
@Data
public class FastIndexBriefVO {
    @ApiModelProperty("模版名称")
    private String               templateName;

    @ApiModelProperty("原索引名称")
    private String indexName;
}
