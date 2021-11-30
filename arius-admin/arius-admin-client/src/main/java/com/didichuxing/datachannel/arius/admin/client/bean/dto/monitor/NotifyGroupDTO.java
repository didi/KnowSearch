package com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "告警组")
public class NotifyGroupDTO extends BaseDTO {
    @ApiModelProperty(value = "ID", notes = "修改的时候用")
    private Long id;
    @ApiModelProperty(value = "告警组名称")
    private String name;
    @ApiModelProperty(value = "告警组成员", notes = "多个用户 id;name,id;name")
    private String members;
    @ApiModelProperty(value = "项目ID")
    private Long appId;
    @ApiModelProperty(value = "状态")
    private Integer status;
    @ApiModelProperty(value = "备注")
    private String comment;
}
