package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * es集群索引服务信息
 * @author zhaoqingrong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "es集群索引服务信息")
public class ESClusterTemplateSrvVO extends BaseVO {
    private static final long serialVersionUID = 1905122041950251207L;

    @ApiModelProperty("索引服务id")
    private Integer serviceId;

    @ApiModelProperty("索引服务名称")
    private String serviceName;

    @ApiModelProperty("索引服务所需的最低es版本号")
    private String esVersion;
}
