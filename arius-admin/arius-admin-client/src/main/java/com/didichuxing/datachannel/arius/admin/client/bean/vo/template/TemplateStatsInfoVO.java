package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 索引的基本统计信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "TemplateStatsInfoVO", description = "模板的基本统计信息")
public class TemplateStatsInfoVO {
    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    private Long templateId;
    /**
     * 模板名称
     */
    @ApiModelProperty(value = "模板名称")
    private String templateName;
    /**
     * 索引健康分
     */
    @ApiModelProperty(value = "索引模板健康分")
    private double indexHealthDegree;
    /**
     * 索引存储容量
     */
    @ApiModelProperty(value = "索引模板总存储容量")
    private double store;
    /**
     * 索引qutoa
     */
    @ApiModelProperty(value = "索引模板的Quota")
    private double qutoa;
    /**
     * 索引成本
     */
    @ApiModelProperty(value = "索引模板的成本")
    private double cost;
    /**
     * 索引昨日访问均量
     */
    @ApiModelProperty(value = "索引模板的每日访问均值")
    private double   accessCountPreDay;
    /**
     * 索引文档数
     */
    @ApiModelProperty(value = "索引模板的文档总数")
    private long   docNu;
    /**
     * 索引昨日峰值写入tps
     */
    @ApiModelProperty(value = "索引模板的昨日写入tps峰值")
    private double   writeTps;
    /**
     * 索引对应的topic
     */
    private List<String> topics = new ArrayList<>();
}
