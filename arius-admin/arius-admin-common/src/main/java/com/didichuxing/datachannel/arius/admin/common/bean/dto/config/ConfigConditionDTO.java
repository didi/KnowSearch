package com.didichuxing.datachannel.arius.admin.common.bean.dto.config;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
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
public class ConfigConditionDTO extends PageDTO {
    @ApiModelProperty(value = "绑定 ecm 的组件 id")
    private Integer componentId;
    @ApiModelProperty("配置名称")
    private String  configName;
    @ApiModelProperty(value = "排序字段",hidden = true)
    private String  sortTerm;
    
    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean", required = true)
    private Boolean orderByDesc = true;
}