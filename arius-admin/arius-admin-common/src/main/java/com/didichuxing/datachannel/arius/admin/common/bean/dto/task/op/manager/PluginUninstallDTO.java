package com.didichuxing.datachannel.arius.admin.common.bean.dto.task.op.manager;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 插件卸载内容
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginUninstallDTO {
		
		/**
		 * 卸载的组件 id
		 */
		private Integer componentId;
		/**
		 * 依赖组件 id
		 */
		private Integer dependComponentId;

		
}