package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.ConfigConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.op.manager.ComponentGroupConfigWithHostVO;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.interfaces.vo.ComponentGroupConfigVO;
import java.util.List;
import java.util.Set;

/**
 * esclusterconfig
 *
 * @author shizeying
 * @date 2022/07/11
 */
public interface ESClusterConfigManager {
    /**
     * 编辑configdesc
     *
     * @param param     入参
     * @param operator  操作人或角色
     * @param projectId 项目id
     * @return {@code Result<Void>}
     */
    Result<Void> editConfigDesc(ESConfigDTO param, String operator, Integer projectId);

    /**
     * 获取ES集群模板config
     *
     * @param type 类型
     * @return {@code Result<ESConfigVO>}
     */
    Result<ESConfigVO> getEsClusterTemplateConfig(String type);

    Result<Set<String>> gainEsClusterRoles(Long clusterId);

    /**
     * 获取ES集群config通过id
     *
     * @param configId configid
     * @return {@code Result<ESConfigVO>}
     */
    Result<ESConfigVO> getEsClusterConfigById(Long configId);

    /**
     * 获得ES集群配置
     *
     * @param clusterId 集群id
     * @return {@code Result<List<ESConfigVO>>}
     */
    Result<List<ESConfigVO>> gainEsClusterConfigs(Long clusterId);
		
		/**
		 * 获取集群的配置列表
		 *
		 * @param condition 查询条件，包括配置名称、配置类型、配置状态。
		 * @param projectId 项目编号
		 * @param phyClusterId 物理集群 ID
		 * @return ClusterPhyConfigVO 对象列表。
		 */
		PaginationResult<ClusterPhyConfigVO> pageGetConfig(ConfigConditionDTO condition, Integer projectId, Integer phyClusterId);
    
    /**
     * 获取指定集群的配置
     *
     * @param clusterPhyId 集群的物理 ID。
     * @param configId 要查询的配置的配置ID。
     * @return GeneralGroupConfigHostVO
     */
    Result<ComponentGroupConfigVO> getConfigByClusterPhyId(Integer clusterPhyId, Integer configId);
		
		/**
		 * 通过集群物理id获取组件组配置
		 *
		 * @param clusterPhyId 集群的物理 ID。
		 * @return ComponentGroupConfigVO 列表
		 */
		Result<List<ComponentGroupConfigWithHostVO>> getConfigsByClusterPhyId(Integer clusterPhyId);
}