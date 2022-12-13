package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.ES_CLUSTER_CONFIG;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ESClusterConfigManager;
import com.didichuxing.datachannel.arius.admin.biz.page.ESClusterConfigPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.ConfigConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.op.manager.ComponentGroupConfigWithHostVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESClusterConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.logi.op.manager.application.ComponentService;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.interfaces.vo.ComponentGroupConfigVO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ESClusterConfigManagerImpl implements ESClusterConfigManager {
    
    @Autowired
    private ESClusterConfigService esClusterConfigService;
    @Autowired
    private OperateRecordService   operateRecordService;
    @Autowired
    private ClusterRoleService     clusterRoleService;
    @Autowired
    private ComponentService       componentService;
    @Autowired
    private ClusterPhyService      clusterPhyService;
    @Autowired
    private HandleFactory          handleFactory;
    
    /**
     * 编辑 configdesc
     *
     * @param param     入参
     * @param operator  操作人或角色
     * @param projectId 项目 id
     * @return {@code Result<Void>}
     */
    @Override
    public Result<Void> editConfigDesc(ESConfigDTO param, String operator, Integer projectId) {
        final Result<Void> checkProjectCorrectly = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (checkProjectCorrectly.failed()) {
            return checkProjectCorrectly;
        }
        final ESConfig oldEsConfig = esClusterConfigService.getEsClusterConfigById(param.getId());
        final Result<Void> result = esClusterConfigService.editConfigDesc(param);
        if (result.success()) {
            operateRecordService.saveOperateRecordWithManualTrigger(
                    String.format("描述变更：【%s】->【%s】", oldEsConfig.getDesc(), param.getDesc()), operator, projectId,
                    param.getId(), OperateTypeEnum.PHYSICAL_CLUSTER_CONF_FILE_CHANGE);
        }
        return result;
    }

    /**
     * 获取 ES 集群模板 config
     *
     * @param type 类型
     * @return {@code Result<ESConfigVO>}
     */
    @Override
    public Result<ESConfigVO> getEsClusterTemplateConfig(String type) {
        return Result
            .buildSucc(ConvertUtil.obj2Obj(esClusterConfigService.getEsClusterTemplateConfig(type), ESConfigVO.class));

    }

    /**
     * @param clusterId
     * @return
     */
    @Override
    public Result<Set<String>> gainEsClusterRoles(Long clusterId) {
        List<ClusterRoleInfo> clusterRoleInfos = clusterRoleService.getAllRoleClusterByClusterId(clusterId.intValue());
        return Result.buildSucc(clusterRoleInfos.stream().filter(Objects::nonNull).map(ClusterRoleInfo::getRole)
            .collect(Collectors.toSet()));
    }

    /**
     * @param configId
     * @return
     */
    @Override
    public Result<ESConfigVO> getEsClusterConfigById(Long configId) {
        return Result
            .buildSucc(ConvertUtil.obj2Obj(esClusterConfigService.getEsClusterConfigById(configId), ESConfigVO.class));
    }

    /**
     * 获得 ES 集群配置
     *
     * @param clusterId 集群 id
     * @return {@code Result<List<ESConfigVO>>}
     */
    @Override
    public Result<List<ESConfigVO>> gainEsClusterConfigs(Long clusterId) {
        Result<List<ESConfig>> listResult = esClusterConfigService.listEsClusterConfigByClusterId(clusterId);
        return Result.buildSucc(ConvertUtil.list2List(listResult.getData(), ESConfigVO.class));
    }
    
    @Override
    public PaginationResult<ClusterPhyConfigVO> pageGetConfig(ConfigConditionDTO condition,
        Integer projectId, Integer phyClusterId) {
        condition.setClusterId(phyClusterId);
    
        BaseHandle baseHandle;
        try {
            baseHandle = handleFactory.getByHandlerNamePer(
                ES_CLUSTER_CONFIG.getPageSearchType());
        } catch (NotFindSubclassException e) {
            return PaginationResult.buildFail("没有找到对应的处理器");
        }
        if (baseHandle instanceof ESClusterConfigPageSearchHandle) {
        
            ESClusterConfigPageSearchHandle handler = (ESClusterConfigPageSearchHandle) baseHandle;
            return handler.doPage(condition, projectId);
        }
        return PaginationResult.buildFail("没有找到对应的处理器");
    }
    
    @Override
    public Result<ComponentGroupConfigVO> getConfigByClusterPhyId(Integer clusterPhyId,
        Integer configId) {
        final Integer componentIdById = clusterPhyService.getComponentIdById(clusterPhyId);
        final Optional<ComponentGroupConfig> componentGroupConfigOptional = Optional.ofNullable(
                componentService.getComponentConfig(componentIdById).getData())
            .orElse(Collections.emptyList()).stream()
            .filter(i -> Objects.equals(i.getId(), configId)).findFirst();
    
        return componentGroupConfigOptional.map(
                i -> ConvertUtil.obj2Obj(i, ComponentGroupConfigVO.class)).map(Result::buildSucc)
            .orElse(Result.buildSucc());
    }
    
    @Override
    public Result<List<ComponentGroupConfigWithHostVO>> getConfigsByClusterPhyId(Integer clusterPhyId) {
        final Integer componentIdById = clusterPhyService.getComponentIdById(clusterPhyId);
        if (Objects.isNull(componentIdById)) {
            return Result.buildSucc(Collections.emptyList());
        }
        final com.didiglobal.logi.op.manager.infrastructure.common.Result<com.didiglobal.logi.op.manager.domain.component.entity.Component> componentRes = componentService.queryComponentById(
            componentIdById);
        if (Objects.isNull(componentRes.getData()) || CollectionUtils.isEmpty(
            componentRes.getData().getGroupConfigList())) {
            return Result.buildSucc(Collections.emptyList());
        }
        	final List<Integer> groupConfigIds = componentService.getComponentConfig(
								componentIdById).getData().stream().map(ComponentGroupConfig::getId)
						.collect(Collectors.toList());
        final Map<String, List<ComponentHost>> groupName2HostLists = com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil.list2MapOfList(
            componentRes.getData().getHostList(),
            ComponentHost::getGroupName, i -> i);
        final List<ComponentGroupConfig> data = componentRes.getData().getGroupConfigList().stream()
            .filter(i -> groupConfigIds.contains(i.getId())).collect(Collectors.toList());
        final List<ComponentGroupConfigWithHostVO> hostVOS = com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil.list2List(
            data,
            ComponentGroupConfigWithHostVO.class);
        for (ComponentGroupConfigWithHostVO hostVO : hostVOS) {
            final String              groupName      = hostVO.getGroupName();
            final List<ComponentHost> componentHosts = groupName2HostLists.get(groupName);
            hostVO.setComponentHosts(componentHosts);
        }
				return Result.buildSucc(hostVOS);
    }
}