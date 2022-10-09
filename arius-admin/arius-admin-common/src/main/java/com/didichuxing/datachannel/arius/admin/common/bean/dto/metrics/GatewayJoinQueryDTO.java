package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cjm
 */
@Data
@ApiModel(description = "GatewayJoin异常查询分页列表查询条件")
public class GatewayJoinQueryDTO extends PageDTO {

    @ApiModelProperty(value = "查询索引（模糊）", dataType = "String", required = false)
    private String queryIndex;

    @ApiModelProperty(value = "DSL模版最近使用时间start（时间戳ms）", dataType = "Long", required = true)
    private Long   startTime;

    @ApiModelProperty(value = "DSL模版最近使用时间end（时间戳ms）", dataType = "Long", required = true)
    private Long   endTime;

    @ApiModelProperty(value = "查询总耗时（时间戳ms）", dataType = "Long", required = true)
    private Long   totalCost;

    @ApiModelProperty(value = "应用Id", dataType = "Integer", required = false)
    private Integer   projectId;

    @ApiModelProperty(value = "物理集群名称（仅限超管侧使用）", dataType = "String", required = false)
    private String clusterName;

    @ApiModelProperty(value = "排序信息（精确）,排序字段 响应时间（ms）esCost、总耗时（ms）totalCost、单次命中数totalHits、单次响应长度responseLen", dataType = "String", required = false)
    private String  sortTerm;

    @ApiModelProperty(value = "是否逆序排序（默认逆序）", dataType = "Boolean", required = false)
    private Boolean orderByDesc = true;

    @ApiModelProperty(value = "tab页面名称", dataType = "String", required = false)
    private String tabName;
}
