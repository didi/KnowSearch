package com.didichuxing.datachannel.arius.admin.common.bean.dto.task.op.manager;


import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralScaleComponentDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 插件卸载内容
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginUninstallDTO extends GeneralScaleComponentDTO {
		
		/**
		 * 依赖组件 id
		 */
		private Integer dependComponentId;

		
}