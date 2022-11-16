package com.didichuxing.datachannel.arius.admin.biz.task.op.manager;


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
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PluginUninstallContent {
		
		/**
		 * 卸载的组件 id
		 */
		private Integer componentId;
		/**
		 * 依赖组件 id
		 */
		private Integer dependComponentId;
		
}