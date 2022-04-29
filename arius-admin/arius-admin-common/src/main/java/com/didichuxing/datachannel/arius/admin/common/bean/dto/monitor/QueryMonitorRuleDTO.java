package com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "告警策略查询条件")
public class QueryMonitorRuleDTO extends BaseDTO {
    @ApiModelProperty(value = "所属项目id")
    private Integer appId;
    @ApiModelProperty(value = "告警级别")
    private Integer priority;
    @ApiModelProperty(value = "策略名称")
    private String name;
    @ApiModelProperty(value = "监控类型")
    private Integer category;
    @ApiModelProperty(value = "监控对象")
    private String objectId;
    @ApiModelProperty(value = "最后修改人")
    private String operator;
    @ApiModelProperty(value = "状态")
    private Integer status;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
