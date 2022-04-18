package com.didichuxing.datachannel.arius.admin.common.bean.dto.indices;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyn
 * @date 2021/09/29
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "索引清理实体")
public class IndicesClearDTO extends BaseDTO {
    @ApiModelProperty("集群名称")
    private String clusterPhyName;

    @ApiModelProperty("索引名称")
    private String index;
}
