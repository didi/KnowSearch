package com.didichuxing.datachannel.arius.admin.common.bean.vo.monitor;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "监控告警")
public class MonitorRuleSummaryVO extends BaseVO {
    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "告警名")
    private String name;

    @ApiModelProperty(value = "应用ID")
    private Long appId;

    @ApiModelProperty(value = "应用名称")
    private String appName;

    @ApiModelProperty(value = "应用负责任")
    private String principals;

    @ApiModelProperty(value = "操作人")
    private String operator;
}