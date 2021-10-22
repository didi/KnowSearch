package com.didichuxing.datachannel.arius.admin.common.bean.entity.index;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description ="索引健康分实时写入统计信息")
public class RealTimeWrite extends BaseDegree {
    /**
     * 10分钟内平均写入率
     */
    @ApiModelProperty(value = "10分钟内平均写入率")
    private Double avgIndexingRate;

    /**
     * 前一天近10分钟内平均写入率
     */
    @ApiModelProperty(value = "前一天近10分钟内平均写入率")
    private Double yesterdayAvgIndexingRate;

    public String getRealTimeWriteDesc(){
        return super.getDesc();
    }

    /**
     * 实时写入得分
     */
    public double getRealTimeWriteScore(){
        return super.getScore();
    }
}
