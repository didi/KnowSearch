package com.didichuxing.datachannel.arius.admin.core.service.gateway.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayClusterDAO;
import java.util.List;
import java.util.Objects;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 网关集群service
 *
 * @author shizeying
 * @date 2022/10/31
 * @since 0.3.2
 */
@Service
@NoArgsConstructor
public class GatewayClusterServiceImpl implements GatewayClusterService {
	@Autowired
	private GatewayClusterDAO gatewayClusterDAO;
	
	@Override
	public List<GatewayClusterPO> listAll() {
		return gatewayClusterDAO.listAll();
	}
	
	@Override
	public boolean checkNameCluster(String clusterName) {
		return Objects.nonNull(gatewayClusterDAO.selectOneByClusterName(clusterName));
	}
	
	@Override
	public boolean insertOne(GatewayClusterDTO gatewayDTO) {
		final GatewayClusterPO gatewayCluster = ConvertUtil.obj2Obj(gatewayDTO,
				GatewayClusterPO.class);
		boolean add = gatewayClusterDAO.insert(gatewayCluster) == 1;
		gatewayDTO.setId(gatewayCluster.getId());
		return add;
	}
	
	@Override
	public List<GatewayClusterPO> pageByCondition(GatewayConditionDTO condition) {
		String sortTerm = null == condition.getSortTerm() ? SortConstant.ID : condition.getSortTerm();
		String sortType = condition.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
		condition.setSortTerm(sortTerm);
		condition.setSortType(sortType);
		condition.setFrom((condition.getPage() - 1) * condition.getSize());
		return gatewayClusterDAO.listByCondition(condition);
	}
	
	@Override
	public Long countByCondition(GatewayConditionDTO condition) {
		return gatewayClusterDAO.countByCondition(condition);
	}
	
	@Override
	public GatewayClusterPO getOneById(Integer gatewayClusterId) {
		return gatewayClusterDAO.getOneById(gatewayClusterId);
	}
	
	@Override
	public boolean deleteOneById(Integer gatewayClusterId) {
		return gatewayClusterDAO.deleteOneById(gatewayClusterId);
	}
	
	@Override
	public boolean editOne(GatewayClusterDTO data) {
		return gatewayClusterDAO.updateOne(ConvertUtil.obj2Obj(data, GatewayClusterPO.class));
	}
	
	@Override
	public String getClusterNameById(Integer gatewayClusterId) {
		return gatewayClusterDAO.getClusterNameById(gatewayClusterId);
	}
	
	@Override
	public Integer getComponentIdById(Integer gatewayClusterId) {
		return gatewayClusterDAO.getComponentIdById(gatewayClusterId);
	}
		@Override
		public GatewayClusterPO getOneByName(String name) {
				return gatewayClusterDAO.getOneByName(name);
		}
		
		@Override
		public GatewayClusterPO getOneByComponentId(Integer componentId) {
				return gatewayClusterDAO.getOneByComponentId(componentId);
		}
		
		@Override
		public Boolean updateVersion(Integer componentId, String version) {
		
				return gatewayClusterDAO.updateVersion(componentId,version);
		}
}