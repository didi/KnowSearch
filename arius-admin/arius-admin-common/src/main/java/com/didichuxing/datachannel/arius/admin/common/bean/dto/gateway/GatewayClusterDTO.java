package com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 网关集群dto
 *
 * @author shizeying
 * @date 2022/10/31
 * @since 0.3.2
 */
@EqualsAndHashCode
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("GatewayClusterDTO")
public class GatewayClusterDTO {
	
	@ApiModelProperty("id")
	private Integer id;
	@ApiModelProperty(value = "gateway 集群")
	private String clusterName;
	@ApiModelProperty("集群健康")
	private Integer health;
	@ApiModelProperty(value = "是否为 ecm 接入",hidden = true)
	private Boolean ecmAccess;
	@ApiModelProperty("备注")
	private String memo;
	@ApiModelProperty(value = "组建 id", hidden = true)
	private Integer componentId;
	@ApiModelProperty(value = "版本号",hidden = true)
	private String version;
	@ApiModelProperty(value = "代理地址",hidden = true)
	private String proxyAddress;
	@ApiModelProperty("数据中心")
	private String dataCenter;
}