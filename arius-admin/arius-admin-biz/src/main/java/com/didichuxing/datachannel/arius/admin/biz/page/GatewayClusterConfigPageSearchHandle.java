package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.ConfigConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterNodePO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayConfigVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayNodeService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 网关集群配置页面搜索
 *
 * @author shizeying
 * @date 2022/11/04
 * @since 0.3.2
 */
@Component
public class GatewayClusterConfigPageSearchHandle extends
		AbstractPageSearchHandle<ConfigConditionDTO, GatewayConfigVO> {
	
	private static final ILog LOGGER = LogFactory.getLog(GatewayClusterConfigPageSearchHandle.class);
	private static final CharSequence[] CHAR_SEQUENCES = {"*", "?"};
	@Autowired
	private GatewayClusterService gatewayClusterService;
	@Autowired
	private GatewayNodeService gatewayNodeService;
	@Autowired
	private ComponentDomainService componentDomainService;
	
	@Override
	protected Result<Boolean> checkCondition(ConfigConditionDTO condition, Integer projectId) {
		String configName = condition.getGroupName();
		if (StringUtils.containsAny(configName, CHAR_SEQUENCES)) {
			return Result.buildParamIllegal("物理集群名称不允许带类似 *, ? 等通配符查询");
		}
		
		return Result.buildSucc();
	}
	
	@Override
	protected void initCondition(ConfigConditionDTO condition, Integer projectId) {
	
	}
	
	@Override
	protected PaginationResult<GatewayConfigVO> buildPageData(ConfigConditionDTO condition,
			Integer projectId) {
		Integer componentId = gatewayClusterService.getComponentIdById(condition.getClusterId());
		// 如果找不到
		if (Objects.isNull(componentId)) {
			return PaginationResult.buildSucc(Collections.emptyList(), 0L, condition.getPage(),
					condition.getSize());
		}
		final List<ComponentGroupConfig> data = componentDomainService.getComponentConfig(
				componentId).getData();
		if (CollectionUtils.isEmpty(data)) {
			return PaginationResult.buildSucc(Collections.emptyList(), 0L, condition.getPage(),
					condition.getSize());
		}
		//获取host列表
			final List<String> hostsList = data.stream().map(this::hostsConvertHostList)
				.flatMap(Collection::stream).distinct()
				.collect(Collectors.toList());
		//1.获取全量的node节点信息
		List<GatewayClusterNodePO> gatewayClusterNodeList=gatewayNodeService.listByHosts(hostsList);
		final List<GatewayClusterNodeVO> nodes = ConvertUtil.list2List(
				gatewayClusterNodeList, GatewayClusterNodeVO.class);
		//进行对应的转换
		final Map<Integer, Integer> configIdComponentIdMap = ConvertUtil.list2Map(data,
				ComponentGroupConfig::getId, ComponentGroupConfig::getComponentId);
		final Map<Integer, List<GatewayClusterNodeVO>> hostComponentId2NodesMaps = ConvertUtil.list2MapOfList(
				nodes, GatewayClusterNodeVO::getComponentId, i -> i);
		//通过ComponentId进行选出对应的对应填充
		final List<GatewayConfigVO> gatewayConfigList = ConvertUtil.list2List(data,
				GatewayConfigVO.class, i -> Optional.ofNullable(configIdComponentIdMap.get(i.getId()))
						.map(hostComponentId2NodesMaps::get)
						.ifPresent(i::setNodes));
		final List<GatewayConfigVO> gatewayConfigPage = gatewayConfigList.stream()
				.filter(i -> filterByConfigConditionDTO(i, condition)).collect(Collectors.toList());
		int total = gatewayConfigList.size();
		final List<GatewayConfigVO> records = gatewayConfigPage.stream()
				.skip((condition.getPage() - 1) * condition.getSize()).limit(condition.getSize()).
				collect(Collectors.toList());
		return PaginationResult.buildSucc(records,total,condition.getPage(),condition.getSize());
	}
	
	/**
	 * > 如果条件的配置名称为空，则返回 true。否则，返回条件的配置名称是否包含网关配置的组名
	 *
	 * @param gatewayConfigVO 要过滤的对象
	 * @param condition 包含查询条件的条件对象。
	 * @return GatewayConfigVO 对象的列表。
	 */
	private boolean filterByConfigConditionDTO(GatewayConfigVO gatewayConfigVO, ConfigConditionDTO condition) {
		if (StringUtils.isBlank(condition.getGroupName())) {
			return true;
		}
		return StringUtils.containsAny(condition.getGroupName(),gatewayConfigVO.getGroupName());
	}
	
	/**
	 * > 将逗号分隔的主机列表转换为字符串列表
	 *
	 * @param componentGroupConfig 传入方法的 ComponentGroupConfig 对象。
	 * @return 字符串列表
	 */
	private List<String> hostsConvertHostList(ComponentGroupConfig componentGroupConfig) {
		return Lists.newArrayList(StringUtils.split(",", componentGroupConfig.getHosts()));
	}
	
	
}