package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by linyunan on 2021-07-31
 */
@Data
@ApiModel("指标信息")
public abstract class MetricsVO implements Serializable {

    @ApiModelProperty("当前时刻")
    private String currentTime;

}
