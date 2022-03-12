package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.client.constant.app.AppClusterPhyAuthEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-06-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("控制台物理集群信息")
public class ConsoleClusterPhyVO extends ESClusterPhyVO {

    @ApiModelProperty("归属项目(App)Id")
    private Integer belongAppId;

	@ApiModelProperty("归属项目名称")
	private String  belongAppName;

	/** @see AppClusterPhyAuthEnum */
	@ApiModelProperty("当前App对集群的权限 1:配置管理, 2:访问, -1:无权限")
	private Integer currentAppAuth;

	@ApiModelProperty("gateway地址")
	private String  gatewayAddress;

    @ApiModelProperty("负责人")
    private String  responsible;
}
