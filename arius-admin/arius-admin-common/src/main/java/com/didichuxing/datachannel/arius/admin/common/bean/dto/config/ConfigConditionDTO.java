package com.didichuxing.datachannel.arius.admin.common.bean.dto.config;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("configDTO")
public class ConfigConditionDTO extends BaseDTO {
    @ApiModelProperty("绑定 ecm 的组件 id")
    private Integer componentId;
    @ApiModelProperty("配置名称")
    private String  configName;
    @ApiModelProperty("排序字段")
    private String  sortTerm;
    
    private String sortType;
    
    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean", required = false)
    private Boolean orderByDesc = true;
}