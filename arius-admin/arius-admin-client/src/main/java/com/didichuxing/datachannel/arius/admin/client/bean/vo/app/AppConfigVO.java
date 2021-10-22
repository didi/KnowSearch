package com.didichuxing.datachannel.arius.admin.client.bean.vo.app;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/13
 */
@Data
@ApiModel(description = "应用配置信息")
public class AppConfigVO extends BaseVO {

    /**
     * 主键
     */
    @ApiModelProperty("主键")
    private Integer id;

    /**
     * app主键
     */
    @ApiModelProperty("应用ID")
    private Integer appId;

    /**
     * gateway返回结果解析开关
     */
    @ApiModelProperty("gateway返回结果解析开关")
    private Integer analyzeResponseEnable;

    /**
     * 是否生效DSL分析查询限流值 1为生效DSL分析查询限流值，0不生效DSL分析查询限流值
     */
    @ApiModelProperty("是否生效DSL分析查询限流值 1为生效DSL分析查询限流值，0不生效DSL分析查询限流值")
    private Integer dslAnalyzeEnable;

    /**
     * 是否开启聚合分析  1 开启  0 不开启  默认为 1
     */
    @ApiModelProperty("是否开启聚合分析  1 开启  0 不开启  默认为 1")
    private Integer aggrAnalyzeEnable;

    /**
     * 是否索引存储分离，1为分离，0为不分离，默认为0
     */
    @ApiModelProperty("是否索引存储分离，1为分离，0为不分离，默认为0")
    private Integer isSourceSeparated;

}
