package com.didichuxing.datachannel.arius.admin.client.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/4/3
 */
@Data
@ApiModel(description = "模板升级信息")
public class TemplatePhysicalUpgradeDTO extends BaseDTO {

    @ApiModelProperty("物理模板id")
    private Long    physicalId;

    @ApiModelProperty("模板当前的shard个数")
    private Integer shard;

    @ApiModelProperty("模板的rack")
    private String  rack;

    @ApiModelProperty("版本")
    private Integer version;

}
