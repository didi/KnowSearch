package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 插件集群类型枚举
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Getter
@AllArgsConstructor
public enum PluginClusterTypeEnum {
		/**
		 * 网关
		 */
		GATEWAY(1),
		/**
		 * ES
		 */
		ES(2);
		private final Integer clusterType;
		
}