package com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 数据迁移限流值设置实体
 *
 * @author didi
 * @date 2022/10/23
 */
@Data
@ApiModel("数据迁移限流值设置")
public class FastIndexRateLimitDTO {

    @ApiModelProperty("任务id")
    private Integer taskId;
    @ApiModelProperty("任务读取限流速率（条/S）")
    private Long    taskReadRate;
}
