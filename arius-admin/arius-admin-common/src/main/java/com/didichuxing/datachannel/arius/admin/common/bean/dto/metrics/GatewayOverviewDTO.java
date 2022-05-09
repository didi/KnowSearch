package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by fitz on 2021-08-11
 */
@Data
@ApiModel(description = "gateway总览查询")
public class GatewayOverviewDTO extends GatewayMetricsDTO {
    @Override
    public String getGroup() {
        return "overview";
    }
}
