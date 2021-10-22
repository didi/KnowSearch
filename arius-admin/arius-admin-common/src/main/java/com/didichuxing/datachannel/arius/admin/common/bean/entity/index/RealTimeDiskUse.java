package com.didichuxing.datachannel.arius.admin.common.bean.entity.index;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description ="索引健康分实时磁盘统计信息")
public class RealTimeDiskUse extends BaseDegree {
    /**
     * 近十分钟,平均磁盘使用率
     */
    @ApiModelProperty(value = "近十分钟,平均磁盘使用率")
    private Double avgDiskUse;

    public String getDiskUseScoreDesc(){
        return super.getDesc();
    }

    /**
     * 实时磁盘使用率得分
     */
    public double getDiskUseScore(){
        return super.getScore();
    }

}
