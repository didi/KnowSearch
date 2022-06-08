package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模板配置信息")
public class IndexTemplateConfigDTO extends BaseDTO {

    @ApiModelProperty("模板ID")
    private Long id;

    @ApiModelProperty("模板ID")
    private Integer logicId;

    @ApiModelProperty("索引存储分离开关")
    private Integer isSourceSeparated;

    @ApiModelProperty("tps因子")
    private Double adjustTpsFactor;

    @ApiModelProperty("shard资源因子")
    private Double adjustShardFactor;

    @ApiModelProperty("动态限流开关")
    private Integer dynamicLimitEnable;

    @ApiModelProperty("mapping优化开关")
    private Integer mappingImproveEnable;

    @ApiModelProperty("indexRollover功能")
    private Boolean disableIndexRollover;
}
