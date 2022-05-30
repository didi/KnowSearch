package com.didichuxing.datachannel.arius.admin.common.bean.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyn
 * @date 2021/09/29
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "分页实体")
public class PageDTO extends BaseDTO{
    @ApiModelProperty("起始页码 前端需求默认第一页的页码是1而不是0")
    private Long page;

    @ApiModelProperty("当前页数量")
    private Long size;

    @ApiModelProperty("查询数据开始下标，自动计算")
    private Long from;

}
