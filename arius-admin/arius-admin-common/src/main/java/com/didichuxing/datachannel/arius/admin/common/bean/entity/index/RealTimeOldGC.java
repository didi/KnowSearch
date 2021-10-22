package com.didichuxing.datachannel.arius.admin.common.bean.entity.index;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description ="索引健康分实时jvm统计信息")
public class RealTimeOldGC extends BaseDegree {
    /**
     * 近十分钟内,总的old gc次数
     */
    @ApiModelProperty(value = "近十分钟内,总的old gc次数")
    private Double avgJvmOldGc;

    public String getJvmOldGcDesc(){
        return super.getDesc();
    }

    /**
     * 实时jvm使用率(用fullgc次数作为指标)得分
     */
    public double getJvmOldGcScore(){
        return super.getScore();
    }
}
