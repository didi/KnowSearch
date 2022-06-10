package com.didichuxing.datachannel.arius.admin.common.bean.dto.app;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
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
@ApiModel(description = "应用配置信息")
public class ProjectConfigDTO extends BaseDTO {
	
	@ApiModelProperty(value = "项目id(更新操作必填)", dataType = "projectId", hidden = true,required = false)
	private Integer projectId;
	
	@ApiModelProperty("gateway返回结果解析开关")
	private Integer analyzeResponseEnable;
	
	@ApiModelProperty("是否生效DSL分析查询限流值 1为生效DSL分析查询限流值，0不生效DSL分析查询限流值")
	private Integer dslAnalyzeEnable;
	
	@ApiModelProperty("是否开启聚合分析  1 开启  0 不开启  默认为 1")
	private Integer aggrAnalyzeEnable;
	
	@ApiModelProperty("是否索引存储分离，1为分离，0为不分离，默认为0")
	private Integer isSourceSeparated;
	/**
	 * 慢查询时间
	 */
	@ApiModelProperty("慢查询耗时：ms")
	
	private Integer slowQueryTimes;
	@ApiModelProperty("备注")
	private String  memo;
	
}