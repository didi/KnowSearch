package com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 网关集群创建dto
 *
 * @author shizeying
 * @date 2022/11/18
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class GatewayClusterCreateDTO extends GatewayClusterDTO{

		private List<GatewayNodeHostDTO> nodes;
}