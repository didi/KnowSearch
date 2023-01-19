package com.didichuxing.datachannel.arius.admin.core.service.cluster.plugin.impl;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.po.plugin.PluginInfoPO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.plugin.PluginInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.plugin.PluginDAO;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

/**
 * 插件信息 service 实现
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Service
public class PluginInfoServiceImpl implements PluginInfoService {
		 @Autowired
    private PluginDAO pluginDAO;
		
		private static final ILog LOGGER = LogFactory.getLog(PluginInfoServiceImpl.class);
		
		
		@Override
		public List<PluginInfoPO> listByClusterId(Integer clusterId,
				PluginClusterTypeEnum clusterTypeEnum) {
				return pluginDAO.listByClusterIdAndClusterType(clusterId,
						clusterTypeEnum.getClusterType());
		}
		
		@Override
		public boolean create(PluginInfoPO pluginInfoPO) {
				return pluginDAO.insert(pluginInfoPO)==1;
		}
		
		@Override
		public boolean delete(Integer clusterId, Integer clusterType, Integer componentId) {
				return pluginDAO.deleteByClusterIdAndClusterTypeAndComponentId(clusterId,clusterType,componentId);
		}
		
		@Override
		public boolean update(PluginInfoPO pluginInfoPO) {
				return pluginDAO.updateByPrimaryKeySelective(pluginInfoPO)==1;
		}
		
		@Override
		public PluginInfoPO selectByClusterIdAndComponentIdAndClusterType(Integer clusterId, Integer componentId,
		                                                                  Integer clusterType) {
				return pluginDAO.selectByClusterIdAndComponentIdAndClusterType(clusterId,clusterType,componentId);
		}
		
		@Override
		public PluginInfoPO getPluginById(Long pluginId) {
				return pluginDAO.selectByPrimaryKey(pluginId);
		}
		
		@Override
		public PluginInfoPO selectByCondition(PluginInfoPO pluginInfoPO) {
				return pluginDAO.selectByCondition(pluginInfoPO);
		}
		
		@Override
		public Boolean deleteByIds(List<Long> pluginIds) {
				if (CollectionUtils.isEmpty(pluginIds)) {
						return true;
				}
				return pluginIds.stream().map(pluginDAO::deleteByPrimaryKey).count() == pluginIds.size();
		}
		
		@Override
		public PluginInfoPO getOneByComponentId(Integer componentId, PluginClusterTypeEnum typeEnum) {
				return pluginDAO.selectOneByComponentIdAndClusterType(componentId,typeEnum.getClusterType());
		}
}