package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

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

    @ApiModelProperty(value = "模板id")
    private Long templateId;

    @ApiModelProperty(value = "模板名称")
    private String templateName;

    @ApiModelProperty(value = "索引模板健康分")
    private double indexHealthDegree;

    @ApiModelProperty(value = "索引模板总存储容量")
    private double store;

    @ApiModelProperty(value = "索引模板的Quota")
    private double qutoa;

    @ApiModelProperty(value = "索引模板的成本")
    private double cost;

    @ApiModelProperty(value = "索引模板的每日访问均值")
    private double   accessCountPreDay;

    @ApiModelProperty(value = "索引模板的文档总数")
    private long   docNu;

    @ApiModelProperty(value = "索引模板的昨日写入tps峰值")
    private double   writeTps;

    /**
     * 索引对应的topic
     */
    private List<String> topics = new ArrayList<>();
}
