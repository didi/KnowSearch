package com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es;

import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralRestartComponentDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 集群插件重新启动内容
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClusterPluginRestartContent extends GeneralRestartComponentDTO {
		
		
		/**
		 * 依赖组件id
		 */
		private Integer dependComponentId;

}