package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

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
@ApiModel(description = "gateway节点查询")
public class GatewayNodeDTO extends GatewayMetricsDTO {

    @ApiModelProperty("节点Ip")
    private String nodeIp;

    @ApiModelProperty("top数字")
    private Integer topNu;

    @Override
    public String getGroup() {
        return "node";
    }
}
