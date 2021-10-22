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
@ApiModel(description = "模板复制信息")
public class TemplatePhysicalCopyDTO extends BaseDTO {

    @ApiModelProperty("物理模板id")
    private Long    physicalId;

    @ApiModelProperty("目标集群")
    private String  cluster;

    @ApiModelProperty("shard个数")
    private Integer shard;

    @ApiModelProperty("rack")
    private String  rack;

    @ApiModelProperty("目标集群是否升级模板版本")
    private Boolean upgrade;
}
