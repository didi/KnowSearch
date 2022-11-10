package com.didichuxing.datachannel.arius.admin.core.service.gateway.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterNodePO;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayNodeService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayClusterNodeDAO;
import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 网关节点service
 *
 * @author shizeying
 * @date 2022/10/31
 * @since 0.3.2
 */
@Service
@NoArgsConstructor
public class GatewayNodeServiceImpl implements GatewayNodeService {
	@Autowired
	private GatewayClusterNodeDAO gatewayClusterNodeDAO;
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean insertBatch(List<GatewayNodeHostDTO> gatewayNodeHosts) {
		return gatewayNodeHosts.stream().mapToInt(this::insertOne).count()
						== gatewayNodeHosts.size();
	}
	
	@Override
	public List<GatewayClusterNodePO> selectByBatchClusterName(List<String> clusterName) {
		return gatewayClusterNodeDAO.selectByBatchClusterName(clusterName);
	}
	
	@Override
	public List<GatewayClusterNodePO> listByClusterName(String clusterName) {
		return gatewayClusterNodeDAO.selectByClusterName(clusterName);
	}
	
	@Override
	public boolean deleteByClusterName(String clusterName) {
		return gatewayClusterNodeDAO.deleteByClusterName(clusterName);
	}
	
	@Override
	public List<GatewayClusterNodePO> pageByCondition(GatewayNodeConditionDTO condition) {
		String sortTerm = null == condition.getSortTerm() ? SortConstant.ID : condition.getSortTerm();
		String sortType = condition.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
		condition.setSortTerm(sortTerm);
		condition.setSortType(sortType);
		condition.setFrom((condition.getPage() - 1) * condition.getSize());
		return gatewayClusterNodeDAO.listByCondition(condition);
	}
	
	@Override
	public Long countByCondition(GatewayNodeConditionDTO condition) {
		return gatewayClusterNodeDAO.countByCondition(condition);
	}
	
	@Override
	public List<GatewayClusterNodePO> listByHosts(List<String> hosts) {
		return gatewayClusterNodeDAO.listByHosts(hosts);
	}
	
	/**
	 * > 在数据库中插入一条记录
	 *
	 * @param gatewayNodeHostDTO 要插入的对象。
	 * @return 受插入影响的行数。
	 */
	private int insertOne(GatewayNodeHostDTO gatewayNodeHostDTO) {
		final GatewayClusterNodePO gatewayClusterNode = ConvertUtil.obj2Obj(gatewayNodeHostDTO,
				GatewayClusterNodePO.class);
		final int i = gatewayClusterNodeDAO.insert(gatewayClusterNode);
		gatewayClusterNode.setId(gatewayClusterNode.getId());
		return i;
	}
}