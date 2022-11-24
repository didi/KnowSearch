package com.didichuxing.datachannel.arius.admin.biz.task.op.manager;

import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralUpgradeComponentDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 插件升级内容
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginUpgradeContent extends GeneralUpgradeComponentDTO {
		
		/**
		 * 依赖组件 id
		 */
		private Integer dependComponentId;
		/**
		 * 原因
		 */
		private String reason;
		/**
		 * 插件类型
		 */
		private Integer pluginType;
}