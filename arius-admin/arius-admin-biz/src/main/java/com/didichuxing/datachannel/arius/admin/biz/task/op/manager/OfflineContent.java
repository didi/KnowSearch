package com.didichuxing.datachannel.arius.admin.biz.task.op.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组件下线
 *
 * @author shizeying
 * @date 2022/11/20
 * @since 0.3.2
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfflineContent {
		/**
		 * 名称
		 */
		private String  name;
		/**
		 * 组件id
		 */
		private Integer componentId;
		/**
		 * 原因
		 */
		private String reason;
}