package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
		PLATFORM(1,"平台插件"),
		/**
		 * 引擎
		 */
		ENGINE(2,"引擎插件"),
		
		/**
		 * 内核
		 */
		KERNEL(3,"内核插件"),
		UNKNOWN(-1,"未知");
		private final Integer pluginType;
		private final String  desc;
		
		public static PluginInfoTypeEnum find(Integer pluginType) {
				switch (pluginType) {
						case 1:
								return PLATFORM;
						case 2:
								return ENGINE;
						case 3:
								return KERNEL;
						default:
								return UNKNOWN;
				}
		}
		
		public static List<TupleTwo<Integer, PluginInfoTypeEnum>> getAll() {
				return Arrays.stream(PluginInfoTypeEnum.values()).map(i -> Tuples.of(i.getPluginType(), i)).collect(
								Collectors.toList());
		}
}