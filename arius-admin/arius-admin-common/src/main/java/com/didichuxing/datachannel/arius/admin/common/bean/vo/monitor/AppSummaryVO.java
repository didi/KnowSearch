package com.didichuxing.datachannel.arius.admin.common.bean.vo.monitor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description="App概览信息")
public class AppSummaryVO {

    @ApiModelProperty(value="AppId")
    private String appId;

    @ApiModelProperty(value="App名称")
    private String name;

    @ApiModelProperty(value="App负责人")
    private String principals;
}
