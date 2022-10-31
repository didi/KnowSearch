package com.didichuxing.datachannel.arius.admin.core.service.gateway.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterBriefVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterVO;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway.GatewayClusterDAO;
import java.util.List;
import java.util.Objects;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	public List<GatewayClusterBriefVO> listAll() {
		return ConvertUtil.list2List(gatewayClusterDAO.listAll(), GatewayClusterBriefVO.class);
	}
	
	@Override
	public boolean checkNameCluster(String clusterName) {
		return Objects.nonNull(gatewayClusterDAO.selectOneByClusterName(clusterName));
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean insert(GatewayClusterDTO gatewayDTO) {
		final GatewayClusterPO gatewayCluster = ConvertUtil.obj2Obj(gatewayDTO,
				GatewayClusterPO.class);
		boolean add = gatewayClusterDAO.insert(gatewayCluster) == 1;
		gatewayDTO.setId(gatewayCluster.getId());
		return add;
	}
	
	@Override
	public List<GatewayClusterVO> listByCondition(GatewayConditionDTO condition) {
		String sortTerm = null == condition.getSortTerm() ? SortConstant.ID : condition.getSortTerm();
		String sortType = condition.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
		condition.setSortTerm(sortTerm);
		condition.setSortType(sortType);
		condition.setFrom((condition.getPage() - 1) * condition.getSize());
		return ConvertUtil.list2List(gatewayClusterDAO.listByCondition(condition),
				GatewayClusterVO.class);
	}
	
	@Override
	public Long countByCondition(GatewayConditionDTO condition) {
		return gatewayClusterDAO.countByCondition(condition);
	}
}