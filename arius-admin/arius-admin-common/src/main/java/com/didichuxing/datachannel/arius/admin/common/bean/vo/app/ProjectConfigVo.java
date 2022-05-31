package com.didichuxing.datachannel.arius.admin.common.bean.vo.app;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "应用信息")
public class ProjectConfigVo extends BaseEntity {
    
    /**
     * projectId
     */
    @ApiModelProperty(value = "项目id", required = true)
    private Integer projectId;
    
    /**
     * gateway返回结果解析开关
     */
    @ApiModelProperty(value = "gateway返回结果解析开关", required = false)
    private Integer analyzeResponseEnable;
    
    /**
     * 是否生效DSL分析查询限流值 1为生效DSL分析查询限流值，0不生效DSL分析查询限流值
     */
    @ApiModelProperty(value = "是否生效DSL分析查询限流值 1为生效DSL分析查询限流值，0不生效DSL分析查询限流值", required = false)
    private Integer dslAnalyzeEnable;
    
    /**
     * 是否开启聚合分析  1 开启  0 不开启  默认为 1
     */
    @ApiModelProperty(value = "是否开启聚合分析  1 开启  0 不开启  默认为 1", required = false)
    private Integer aggrAnalyzeEnable;
    
    /**
     * 是否索引存储分离，1为分离，0为不分离，默认为0
     */
    @ApiModelProperty(value = "是否索引存储分离，1为分离，0为不分离，默认为0", required = false)
    private Integer isSourceSeparated;
    /**
     * 慢查询时间
     */
    @ApiModelProperty(value = "慢查询时间", required = true)
    private Integer slowQueryTimes;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注", required = false)
    private String  memo;
    
}