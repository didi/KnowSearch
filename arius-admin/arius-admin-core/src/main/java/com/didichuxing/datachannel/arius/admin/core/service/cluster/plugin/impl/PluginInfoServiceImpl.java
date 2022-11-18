package com.didichuxing.datachannel.arius.admin.core.service.cluster.plugin.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.po.plugin.PluginInfoPO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.plugin.PluginInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.plugin.PluginDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}