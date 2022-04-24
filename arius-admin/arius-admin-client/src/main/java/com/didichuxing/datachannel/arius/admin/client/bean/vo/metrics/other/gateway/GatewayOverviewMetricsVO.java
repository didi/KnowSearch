package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.other.gateway;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.top.MetricsContentCellVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by fitz on 2021-08-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("gateway总览折线图指标信息")
public class GatewayOverviewMetricsVO {

    @ApiModelProperty("指标类型")
    private String type;

    @ApiModelProperty("指标值")
    private List<MetricsContentCellVO> metrics;
}
