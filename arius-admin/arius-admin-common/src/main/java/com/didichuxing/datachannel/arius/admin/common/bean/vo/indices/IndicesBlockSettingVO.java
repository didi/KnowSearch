package com.didichuxing.datachannel.arius.admin.common.bean.vo.indices;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

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
@ApiModel(description = "索引setting阻塞信息")
public class IndicesBlockSettingVO extends BaseVO {
    @ApiModelProperty("集群名称")
    private String  clusterPhyName;

    @ApiModelProperty("索引名称")
    private String  indexName;

    @ApiModelProperty("阻塞类型 1 read 读 2 write 写")
    private String  type;

    @ApiModelProperty("阻塞值 true false")
    private Boolean value;
}
