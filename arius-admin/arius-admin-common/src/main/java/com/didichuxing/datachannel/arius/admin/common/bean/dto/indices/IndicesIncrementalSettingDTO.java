package com.didichuxing.datachannel.arius.admin.common.bean.dto.indices;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Authoer: zyl
 * @Date: 2022/10/12
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "索引增量settings实体")
public class IndicesIncrementalSettingDTO {
    @ApiModelProperty("集群名称")
    private String  cluster;

    @ApiModelProperty("索引名称")
    private String  index;

    // "index.translog.durability" : "async" ? "request"
    // "index.priority" : "10"、"5"、"0"
    @ApiModelProperty("要修改的settings的key")
    private String key;

    @ApiModelProperty("要修改的settings的value")
    private String value;
}
