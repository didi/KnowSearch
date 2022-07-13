package com.didichuxing.datachannel.arius.admin.common.bean.po.index.healthdegree;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.*;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "HealthDegreesPO", description = "模板的健康分")
public class HealthDegreesPO extends BaseESPO {
    /**
     * 模块id
     */
    @ApiModelProperty(value = "物理模板id")
    private Integer            templateId;
    /**
     * 逻辑模块id
     */
    @ApiModelProperty(value = "逻辑模板id")
    private Integer            logicTemplateId;
    /**
     * 模板
     */
    @ApiModelProperty(value = "模板名称")
    private String             template;
    /**
     * 集群
     */
    @ApiModelProperty(value = "集群名称")
    private String             cluster;
    /**
     * 部门
     */
    @ApiModelProperty(value = "部门名称")
    private String             department;
    /**
     * 离线模块得分
     */
    @ApiModelProperty(value = "索引离线指标得分")
    private Double             offLineScore;
    /**
     * 实时模块得分
     */
    @ApiModelProperty(value = "索引实时指标得分")
    private Double             onLineScore;
    /**
     * 健康总分
     */
    @ApiModelProperty(value = "索引总的指标得分")
    private Double             totalScore;
    /**
     * 核算时间
     */
    @ApiModelProperty(value = "索引健康分计算时间")
    private Long               timestamp;
    /**
     * 健康分详情
     */
    @ApiModelProperty(value = "索引健康分说明")
    private String             desc;
    /**
     * 实时写入指标
     */
    @JSONField(name = "realTimeWrite")
    @ApiModelProperty(value = "索引实时写入详细指标")
    private RealTimeWrite      realTimeWrite;

    /**
     * 实时查询指标
     */
    @JSONField(name = "realTimeSearch")
    @ApiModelProperty(value = "索引实时查询详细指标")
    private RealTimeSearch     realTimeSearch;

    /**
     * 查询时长指标
     */
    @JSONField(name = "searchCostTime")
    @ApiModelProperty(value = "索引实时查询耗时详细指标")
    private RealTimeSearchCost realTimeSearchCost;

    /**
     * 实时jvm使用率
     */
    @JSONField(name = "jvmOldGcCollection")
    @ApiModelProperty(value = "索引实时GC详细指标")
    private RealTimeOldGC      realTimeOldGC;

    /**
     * 实时磁盘使用率
     */
    @JSONField(name = "diskUseRate")
    @ApiModelProperty(value = "索引实时磁盘详细指标")
    private RealTimeDiskUse    realTimeDiskUse;

    /**
     * 实时cpu使用率
     */
    @JSONField(name = "osCpuUseRate")
    @ApiModelProperty(value = "索引实时CPU详细指标")
    private RealTimeCpuUse     realTimeCpuUse;

    /**
     * 离线模块
     */
    @JSONField(name = "offLine")
    @ApiModelProperty(value = "索引离线详细指标")
    private OffLine            offLine;

    @Override
    public String getKey() {
        return cluster + "_" + template + "_" + timestamp;
    }

    @Override
    public String getRoutingValue() {
        return null;
    }
}
