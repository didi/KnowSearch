package com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by fitz on 2021-08-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "gateway项目查询")
public class GatewayAppDTO extends GatewayMetricsDTO {

    @ApiModelProperty("项目Id")
    private String appId;

    @ApiModelProperty("top数字")
    private Integer topNu;

}
