package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsConfigInfoDTO extends BaseDTO {

    @ApiModelProperty("账号信息")
    private String       domainAccount;

    @ApiModelProperty("一级目录下的指标配置类型,如集群看板，网关看板")
    private String       firstMetricsType;

    @ApiModelProperty("二级目录下的指标配置类型,如集群看板下的总览指标类型")
    private String       secondMetricsType;

    @ApiModelProperty("二级目录指标配置下具体的配置列表,如cpu利用率")
    private List<String> metricsTypes;
}
