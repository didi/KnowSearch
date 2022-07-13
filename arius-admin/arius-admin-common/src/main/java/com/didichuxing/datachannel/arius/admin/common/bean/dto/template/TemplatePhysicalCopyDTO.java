package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/4/3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模板复制信息")
public class TemplatePhysicalCopyDTO extends BaseDTO {

    @ApiModelProperty("物理模板id")
    private Long    physicalId;

    @ApiModelProperty("目标集群")
    private String  cluster;

    @ApiModelProperty("shard个数")
    private Integer shard;

    @ApiModelProperty("regionId")
    private Integer regionId;
}
