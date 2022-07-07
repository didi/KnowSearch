package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 * @author guoyoupeng_v
 * @date 2022-05-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "快捷命令信息")
public class ClusterPhyQuickCommandDTO extends PageDTO {


    @ApiModelProperty("查询关键字")
    private String                     keyword;

}
