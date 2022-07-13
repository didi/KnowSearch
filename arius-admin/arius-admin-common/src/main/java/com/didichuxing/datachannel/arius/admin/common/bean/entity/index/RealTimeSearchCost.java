package com.didichuxing.datachannel.arius.admin.common.bean.entity.index;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "索引健康分实时查询耗时统计信息")
public class RealTimeSearchCost extends BaseDegree {
    /**
     * 近十分钟平均查询时长
     */
    @ApiModelProperty(value = "近十分钟平均查询时长")
    private Double avgSearchCostTime;

    /**
     * 查询耗时得分
     */
    public double getSearchCostTimeScore() {
        return super.getScore();
    }

    public String getSearchCostTimeDesc() {
        return super.getDesc();
    }
}
