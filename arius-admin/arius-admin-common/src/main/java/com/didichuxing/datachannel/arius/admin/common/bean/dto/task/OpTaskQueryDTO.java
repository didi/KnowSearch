package com.didichuxing.datachannel.arius.admin.common.bean.dto.task;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "任务中心查询分页列表查询条件")
public class OpTaskQueryDTO extends PageDTO {
    @ApiModelProperty(value = "标题（模糊）", dataType = "String", required = true)
    private String title;
    @ApiModelProperty(value = "最近创建时间start（时间戳ms）", dataType = "Long")
    private Long   startTime;

    @ApiModelProperty(value = "最近创建时间end（时间戳ms）", dataType = "Long")
    private Long   endTime;

    @ApiModelProperty(value = "排序信息（精确）,排序字段 创建时间createTime,更新时间updateTime", dataType = "String")
    private String  sortTerm;

    @ApiModelProperty(value = "是否逆序排序（默认逆序）", dataType = "Boolean")
    private Boolean orderByDesc = true;
}
