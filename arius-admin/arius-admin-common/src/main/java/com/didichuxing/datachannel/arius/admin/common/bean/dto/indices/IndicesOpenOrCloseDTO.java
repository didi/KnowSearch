package com.didichuxing.datachannel.arius.admin.common.bean.dto.indices;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cjm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
@ApiModel(description = "索引开启或关闭实体")
public class IndicesOpenOrCloseDTO extends BaseDTO {
    @ApiModelProperty("集群名称")
    private String clusterPhyName;

    @ApiModelProperty("索引名称")
    private String index;
}
