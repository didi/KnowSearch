package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/1/15 下午6:39
 * @modified By D10865
 *
 * dsl模板信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "DslTemplateVO", description = "dsl模板信息")
public class DslTemplateVO {

    @ApiModelProperty("查询模板创建时间")
    private String ariusCreateTime;

    @ApiModelProperty("查询模板修改时间")
    private String ariusModifyTime;

    @ApiModelProperty("平均响应长度")
    private Double responseLenAvg;

    @ApiModelProperty("请求类型")
    private String requestType;

    @ApiModelProperty("查询类型")
    private String searchType;

    @ApiModelProperty("查询次数(分钟级别)")
    private Long searchCount;

    @ApiModelProperty("es查询耗时")
    private Double esCostAvg;

    @ApiModelProperty("平均查询语句长度")
    private Double dslLenAvg;

    @ApiModelProperty("平均命中记录数")
    private Double totalHitsAvg;

    @ApiModelProperty("平均查询shard成功个数")
    private Double successfulShardsAvg;

    @ApiModelProperty("平均shard成功个数")
    private Double totalShardsAvg;

    @ApiModelProperty("查询请求时刻")
    private String logTime;

    @ApiModelProperty("查询索引示例")
    private String indiceSample;

    @ApiModelProperty("查询模板")
    private String dslTemplate;

    @ApiModelProperty("查询请求时刻")
    private Long timeStamp;

    @ApiModelProperty("查询语句类型")
    private String dslType;

    @ApiModelProperty("查询索引名称")
    private String indices;

    @ApiModelProperty("查询模板MD5")
    private String dslTemplateMd5;

    @ApiModelProperty("平均查询总耗时")
    private Double totalCostAvg;

    @ApiModelProperty("查询shard失败个数")
    private Double failedShardsAvg;

    @ApiModelProperty("dsink写入时间")
    private Long sinkTime;

    @ApiModelProperty("appid")
    private Integer appid;

    @ApiModelProperty("查询语句")
    private String dsl;

    @ApiModelProperty("平均gateway处理耗时")
    private Double beforeCostAvg;

    @ApiModelProperty("flin")
    private String flinkTime;

    @ApiModelProperty("查询限流")
    private Double queryLimit;

    @ApiModelProperty("是否来自用户控制台")
    private Boolean isFromUserConsole;

    @ApiModelProperty("是否强制设置查询限流值")
    private Boolean forceSetQueryLimit;

    @ApiModelProperty("是否可用 null/true表示可用，false表示不可用")
    private Boolean enable;

    @ApiModelProperty("黑白名单 null/white表示白名单，black表示黑名单")
    private String checkMode;

    @ApiModelProperty("慢查dsl阈值，单位为ms")
    private Long slowDslThreshold;

    @ApiModelProperty("查询模板版本号")
    private String version;

    @ApiModelProperty("查询模板危害标签")
    private String dslTag;
}
