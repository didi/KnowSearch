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
@ApiModel(description = "gateway索引查询")
public class GatewayIndexDTO extends GatewayMetricsDTO {

    @ApiModelProperty("索引名称")
    private String indexName;

    @ApiModelProperty("top数字")
    private Integer topNu;
}
