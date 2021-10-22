package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@ApiModel(description = "模板配置信息")
public class IndexTemplateConfigVO extends BaseVO {

    @ApiModelProperty("模板ID")
    private Long    id;

    @ApiModelProperty("模板ID")
    private Integer logicId;

    @ApiModelProperty("索引存储分离开关")
    private Integer isSourceSeparated;

    @ApiModelProperty("tps因子")
    private Double  adjustRackTpsFactor;

    @ApiModelProperty("shard资源因子")
    private Double  adjustRackShardFactor;

    @ApiModelProperty("动态限流开关")
    private Integer dynamicLimitEnable;

    @ApiModelProperty("mapping优化开关")
    private Integer mappingImproveEnable;

}
