package com.didichuxing.datachannel.arius.admin.common.bean.entity.index;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description ="索引健康分离线统计信息")
public class OffLine extends BaseDegree {
    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    private Integer templateId;
    /**
     * 模板名称
     */
    @ApiModelProperty(value = "模板名称")
    private String template;
    /**
     * 成本(单位GB)
     */
    @ApiModelProperty(value = "每GB成本")
    private double costByGb;
    /**
     * 模板天访问量(前一天)
     */
    @ApiModelProperty(value = "模板天访问量(前一天)")
    private long yesterdayAccessNum;
    /**
     * 单成本访问量(accessNum/costGb)
     */
    @ApiModelProperty(value = "单成本访问量(accessNum/costGb)")
    private double singleGbAccess;
    /**
     * 集群
     */
    @ApiModelProperty(value = "集群")
    private String cluster;
    /**
     * 是否文档数为0
     */
    @ApiModelProperty(value = "是否文档数为0")
    private boolean isZeroCount;
    /**
     * 时间字段(毫秒时间戳)
     */
    @ApiModelProperty(value = "时间字段(毫秒时间戳)")
    private long timestamp;
    /**
     * 部门
     */
    @ApiModelProperty(value = "部门")
    private String department;

    public double getOffLineSocre(){
        return super.getScore();
    }

    public double getOffLineWeighScore(){
        return super.getWeightScore();
    }
}
