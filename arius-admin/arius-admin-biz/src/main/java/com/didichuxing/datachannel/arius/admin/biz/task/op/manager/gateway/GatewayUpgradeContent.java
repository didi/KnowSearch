package com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway;

import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.GeneralUpgradeComponentContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 集群升级内容
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatewayUpgradeContent extends GeneralUpgradeComponentContent {
		
		/**
		 * 升级类型：0,是升级,1是回滚
		 */
		private Integer upgradeType;
}