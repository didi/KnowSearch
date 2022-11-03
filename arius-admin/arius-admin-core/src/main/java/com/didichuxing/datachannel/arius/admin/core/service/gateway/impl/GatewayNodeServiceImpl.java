package com.didichuxing.datachannel.arius.admin.core.service.gateway.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterNodePO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
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
	public List<GatewayClusterNodeVO> selectByBatchClusterName(List<String> clusterName) {
		return ConvertUtil.list2List(gatewayClusterNodeDAO.selectByBatchClusterName(clusterName),
				GatewayClusterNodeVO.class);
	}
	
	@Override
	public List<GatewayClusterNodeVO> listByClusterName(String clusterName) {
		return ConvertUtil.list2List(gatewayClusterNodeDAO.selectByClusterName(clusterName),
				GatewayClusterNodeVO.class);
	}
	
	@Override
	public boolean deleteByClusterName(String clusterName) {
		return gatewayClusterNodeDAO.deleteByClusterName(clusterName);
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