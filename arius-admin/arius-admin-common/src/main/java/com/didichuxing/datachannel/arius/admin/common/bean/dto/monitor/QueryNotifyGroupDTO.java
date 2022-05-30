package com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Deprecated
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "告警组查询条件")
public class QueryNotifyGroupDTO extends BaseDTO {
    @ApiModelProperty(value = "项目ID")
    private Long appId;
    @ApiModelProperty(value = "告警组名称")
    private String name;
    @ApiModelProperty(value = "告警组成员")
    private String members;
    @ApiModelProperty(value = "最后修改人")
    private String operator;
    @ApiModelProperty(value = "状态", notes = "状态. -1 删除, 0 停用, 1 启用.")
    private Integer status;
    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;
    @ApiModelProperty(value = "每页数量")
    private Integer pageSize = 10;
}