package com.didichuxing.datachannel.arius.admin.common.bean.entity.index;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "索引健康分实时cpu统计信息")
public class RealTimeCpuUse extends BaseDegree {
    /**
     * 近十分钟索引所在节点平均cpu使用率
     */
    @ApiModelProperty(value = "近十分钟索引所在节点平均cpu使用率分")
    private Double avgCpuAvgUse;

    public String getCpuUseScoreDesc() {
        return super.getDesc();
    }

    /**
     * 实时cpu使用率得分
     */
    public double getCpuUseScore() {
        return super.getScore();
    }
}
