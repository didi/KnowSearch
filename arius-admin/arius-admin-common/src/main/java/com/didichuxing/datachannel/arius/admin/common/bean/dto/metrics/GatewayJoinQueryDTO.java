package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cjm
 */
@Data
@ApiModel(description = "GatewayJoin异常查询分页列表查询条件")
public class GatewayJoinQueryDTO {

    @ApiModelProperty(value = "查询索引（模糊）", dataType = "String", required = false)
    private String queryIndex;

    @ApiModelProperty(value = "DSL模版最近使用时间start（时间戳ms）", dataType = "Long", required = true)
    private Long   startTime;

    @ApiModelProperty(value = "DSL模版最近使用时间end（时间戳ms）", dataType = "Long", required = true)
    private Long   endTime;

    @ApiModelProperty(value = "查询总耗时（时间戳ms）", dataType = "Long", required = true)
    private Long   totalCost;
}
