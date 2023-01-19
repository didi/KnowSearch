package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
		
		public static List<TupleTwo<Integer, PluginClusterTypeEnum>> getAll() {
				return Arrays.stream(PluginClusterTypeEnum.values()).map(i -> Tuples.of(i.getClusterType(), i)).collect(
								Collectors.toList());
		}
		
}