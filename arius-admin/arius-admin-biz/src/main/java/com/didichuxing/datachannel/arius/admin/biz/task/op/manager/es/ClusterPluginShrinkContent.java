package com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es;

import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.GeneralScaleComponentContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 集群插件缩容内容
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterPluginShrinkContent extends GeneralScaleComponentContent {
		
		/**
		 * 依赖组件 id
		 */
		private Integer dependComponentId;
}