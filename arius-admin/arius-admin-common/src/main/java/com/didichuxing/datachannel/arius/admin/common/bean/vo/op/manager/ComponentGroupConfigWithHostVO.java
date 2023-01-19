package com.didichuxing.datachannel.arius.admin.common.bean.vo.op.manager;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleVO;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * component分组配置withhostvo
 *
 * @author shizeying
 * @date 2022/12/08
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentGroupConfigWithHostVO extends ComponentGroupConfig {
		private List<ComponentHost> componentHosts;
		
		private Integer packageId;
		private Integer dependComponentId;
		
		private List<ESClusterRoleHostVO> esClusterRoles;
    private List<ESClusterRoleVO>     roleWithNodes;

}