package com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by chengxiang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "gateway节点查询")
public class MultiGatewayNodesDTO extends GatewayMetricsDTO {

    @ApiModelProperty("节点Ip")
    private List<String> nodeIps;

    @ApiModelProperty("top数字")
    private Integer topNu;

    @Override
    public String getGroup() {
        return "node";
    }
}
