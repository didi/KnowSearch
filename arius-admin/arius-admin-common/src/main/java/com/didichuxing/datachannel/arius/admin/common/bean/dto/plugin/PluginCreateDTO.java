package com.didichuxing.datachannel.arius.admin.common.bean.dto.plugin;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * es 集群创建任务处理程序
 *
 * @author shizeying
 * @date 2022/11/20
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginCreateDTO extends BaseDTO {
		/**
		 * 插件名
		 */
		@ApiModelProperty("插件名")
		private String name;
		
		/**
		 * 集群 id
		 */
		@ApiModelProperty("集群 id")
		private Integer clusterId;
		
		/**
		 * 插件版本
		 */
		@ApiModelProperty("插件版本")
		private String version;
		
		/**
		 * 注释
		 */
		@ApiModelProperty("注释")
		private String memo;
		
		/**
		 * 组建 ID
		 */
		@ApiModelProperty("组建 ID")
		private Integer componentId;
		
		/**
		 * 插件类型（1. 平台;2. 引擎 )
		 */
		@ApiModelProperty("插件类型（1. 平台;2. 引擎)")
		private Integer pluginType;
		
		/**
		 * 集群类型 (1.es;2.gateway)
		 */
		@ApiModelProperty("集群类型 (1.es;2.gateway)")
		private Integer clusterType;
}