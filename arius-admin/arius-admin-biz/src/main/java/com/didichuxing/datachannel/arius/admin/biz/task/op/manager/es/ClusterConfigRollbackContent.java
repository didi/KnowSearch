package com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es;

import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 集群配置回滚内容
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class ClusterConfigRollbackContent extends ClusterConfigContent {
		

		
		
		/**
		 * 旧的分组配置列表
		 */
		private List<GeneralGroupConfigDTO> oldGroupConfigList;
		
}