package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by linyunan on 2021-09-14
 */
@Data
@ApiModel(description = "用户zeus回写ES集群读写地址")
public class ESZeusHostInfoDTO {

	@ApiModelProperty("集群名称")
	private String clusterPhyName;

    @ApiModelProperty("角色")
    private String role;

    @ApiModelProperty("Http读写地址")
    private String httpAddress;
}
