package com.didichuxing.datachannel.arius.admin.common.bean.dto.indices;

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
@ApiModel(description = "索引阻塞实体")
public class IndicesBlockSettingDTO {
    @ApiModelProperty("集群名称")
    private String cluster;

    @ApiModelProperty("索引名称")
    private String  index;

    @ApiModelProperty("阻塞类型 1 read 读 2 write 写")
    private String  type;

    @ApiModelProperty("阻塞值 true false")
    private Boolean value;
}
