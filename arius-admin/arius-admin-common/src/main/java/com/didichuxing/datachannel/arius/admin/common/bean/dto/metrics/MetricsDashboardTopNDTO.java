package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Dashboard大盘Top类型类型的指标信息")
public class MetricsDashboardTopNDTO extends BaseDTO {
    @ApiModelProperty("开始时间")
    private Long         startTime;

    @ApiModelProperty("结束时间")
    private Long         endTime;

    @ApiModelProperty("聚合类型")
    private String       aggType;

    @ApiModelProperty("指标类型")
    private List<String> metricsTypes;

    @ApiModelProperty("Top-Level:5,10,15,20")
    private Integer      topNu;

    public void init() {
        // 这里后端暂时写定max, 防止前端乱传聚合类型导致返回数据失真
        // aggType   = null == aggType ? "max" : aggType;
        aggType = "max";
        topNu = null == topNu ? 5 : topNu;
        long currentTimeMillis = System.currentTimeMillis();
        // 默认为最新一小时的时间区间
        startTime = null == startTime ? (currentTimeMillis - 60 * 60 * 1000) : startTime;
        endTime = null == endTime ? currentTimeMillis : endTime;
    }
}
