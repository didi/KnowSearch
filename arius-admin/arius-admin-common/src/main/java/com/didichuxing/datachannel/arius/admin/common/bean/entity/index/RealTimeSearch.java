package com.didichuxing.datachannel.arius.admin.common.bean.entity.index;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "索引健康分实时查询统计信息")
public class RealTimeSearch extends BaseDegree {
    /**
     * 近十分钟平均查询率
     */
    @ApiModelProperty(value = "近十分钟平均查询率")
    private Double avgSearchRate;

    /**
     * 昨天近十分钟平均查询率
     */
    @ApiModelProperty(value = "昨天近十分钟平均查询率")
    private Double yesterdayAvgSearchRate;

    public String getRealTimeSearchDesc() {
        return super.getDesc();
    }

    /**
     * 实时查询得分
     */
    public double getSearchScore() {
        return super.getScore();
    }
}
