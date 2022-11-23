package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 插件枚举类型
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Getter
@AllArgsConstructor
public enum PluginInfoTypeEnum {
		/**
		 * 平台
		 */
		PLATFORM(1),
		/**
		 * 引擎
		 */
		ENGINE(2),
		/**
		 * 内核
		 */
		KERNEL(3);
		private final Integer pluginType;
		
		
}