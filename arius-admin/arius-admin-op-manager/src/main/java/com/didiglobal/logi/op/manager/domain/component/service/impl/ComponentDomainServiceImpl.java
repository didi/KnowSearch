package com.didiglobal.logi.op.manager.domain.component.service.impl;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.SPLIT;

import com.alibaba.fastjson.JSON;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentGroupConfigRepository;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentHostRepository;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentRepository;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.component.service.handler.ScaleHandler;
import com.didiglobal.logi.op.manager.domain.component.service.handler.ScaleHandlerFactory;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralConfigChangeComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralExecuteComponentFunction;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralInstallComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralRestartComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralRollbackComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralScaleComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralUninstallComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralUpgradeComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.event.DomainEvent;
import com.didiglobal.logi.op.manager.infrastructure.common.event.SpringEventPublisher;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * @author didi
 * @date 2022-07-12 2:34 下午
 */
@Service
public class ComponentDomainServiceImpl implements ComponentDomainService {

    public static final ILog LOGGER = LogFactory.getLog(ComponentDomainServiceImpl.class);

    @Autowired
    private SpringEventPublisher publisher;

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private ComponentHostRepository componentHostRepository;

    @Autowired
    private ComponentGroupConfigRepository componentGroupConfigRepository;

    @Autowired
    private ScaleHandlerFactory scaleHandlerFactory;

    @Override
    public Result<Integer> submitInstallComponent(GeneralInstallComponent installComponent) {
        //校验是否有同名的任务
        List<Component> repeatNameList = componentRepository.listAllComponent().stream().filter(component ->
                component.getName().equals(installComponent.getName())
        ).collect(Collectors.toList());
        if (!repeatNameList.isEmpty()) {
            return Result.fail(ResultCode.COMPONENT_NAME_REPEAT_ERROR);
        }
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createInstallEvent(installComponent));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<Integer> submitScaleComponent(GeneralScaleComponent scaleComponent) {
        Result checkRes = scaleHandlerFactory.getScaleHandler(scaleComponent.getType()).check(scaleComponent);
        if (checkRes.failed()) {
            return checkRes;
        }
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createScaleEvent(scaleComponent));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<Integer> submitConfigChangeComponent(GeneralConfigChangeComponent changeComponent) {
        List<ComponentGroupConfig> currentConfigList = componentGroupConfigRepository.getConfigByComponentId(changeComponent.getComponentId());
        for (GeneralGroupConfig config : changeComponent.getGroupConfigList()) {
            boolean isExist = currentConfigList.stream().filter(currentConfig ->
                    config.getGroupName().equals(currentConfig.getGroupName())).findFirst().isPresent();
            if (!isExist) {
                return Result.fail("更改的配置分组名不存在");
            }
        }
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createConfigChangeEvent(changeComponent));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<Integer> submitRestartComponent(GeneralRestartComponent restartComponent) {
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createRestartEvent(restartComponent));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<Integer> submitUninstallComponent(GeneralUninstallComponent uninstallComponent) {
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createUninstallEvent(uninstallComponent));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<Integer> submitUpgradeComponent(GeneralUpgradeComponent upgradeComponent) {
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createUpdateEvent(upgradeComponent));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<Integer> submitRollbackComponent(GeneralRollbackComponent rollbackComponent) {
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createRollbackEvent(rollbackComponent));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<Integer> submitExecuteFunctionComponent(GeneralExecuteComponentFunction executeComponentFunction) {
        //发送事件，领域解耦
        DomainEvent domainEvent = publisher.publish(ComponentEvent.createExecuteFunctionEvent(executeComponentFunction));
        return (Result<Integer>) domainEvent.getResult();
    }

    @Override
    public Result<List<ComponentGroupConfig>> getComponentConfig(int componentId) {
        Component component = componentRepository.getComponentById(componentId);
        List<ComponentGroupConfig> configList;
        if(null != component.getDependConfigComponentId()) {
            configList = componentGroupConfigRepository.getConfigByComponentId(component.getDependConfigComponentId());
        } else {
            configList = componentGroupConfigRepository.getConfigByComponentId(componentId);
        }
        return Result.success(getLatestGroupConfig(configList));
    }

    @Override
    public Result<Integer> updateComponent(Component component) {
        return Result.success(componentRepository.updateComponent(component));
    }

    @Override
    public Result<List<Component>> listComponentWithAll() {
        List<Component> componentList = componentRepository.listAllComponent();
        List<ComponentHost> hosts = componentHostRepository.listComponentHost();
        List<ComponentGroupConfig> configs = componentGroupConfigRepository.listGroupConfig();
        Map<String, List<ComponentHost>> componentIdToHostMap = new HashMap<>(componentList.size());
        Map<String, List<ComponentGroupConfig>> componentIdToGroupConfigMap = new HashMap<>(componentList.size());

        //构建componentIdToHostMap
        hosts.forEach(componentHost -> {
            List<ComponentHost> hostList = componentIdToHostMap.get(componentHost.getComponentId().toString());
            if (null == hostList) {
                hostList = new ArrayList<>();
                componentIdToHostMap.put(componentHost.getComponentId().toString(), hostList);
            }
            hostList.add(componentHost);
        });

        //构建componentIdToGroupConfigMap
        configs.forEach(groupConfig -> {
            List<ComponentGroupConfig> groupConfigList = componentIdToGroupConfigMap.get(groupConfig.getComponentId().toString());
            if (null == groupConfigList) {
                groupConfigList = new ArrayList<>();
                componentIdToGroupConfigMap.put(groupConfig.getComponentId().toString(), groupConfigList);
            }
            groupConfigList.add(groupConfig);
        });
        //赋值汇总
        componentList.forEach(component -> {
            component.setHostList(componentIdToHostMap.get(component.getId().toString()));
            if (null != componentIdToGroupConfigMap.get(component.getId().toString())) {
                component.setGroupConfigList(getLatestGroupConfig(componentIdToGroupConfigMap.get(component.getId().toString())));
            }

        });

        return Result.success(componentList);
    }

    @Override
    public Result<List<Component>> queryComponent(Component queryComponent) {
        List<Component> componentList = componentRepository.queryComponent(queryComponent);
        return Result.success(componentList);
    }
    
    @Override
    public Result<Component> queryComponentByName(String name) {
        Component component = componentRepository.queryComponentByName(name);
        if (Objects.isNull(component)){
            return Result.fail(ResultCode.COMPONENT_NOT_EXIST_ERROR);
        }
        List<ComponentHost> hosts = componentHostRepository.listComponentHost();
        Map<Integer, List<ComponentHost>> componentId2ListMap = ConvertUtil.list2MapOfList(hosts,
                                                                                           ComponentHost::getComponentId,
                                                                                           i -> i);
        List<ComponentGroupConfig> configs = componentGroupConfigRepository.listGroupConfig();
        Map<Integer, List<ComponentGroupConfig>> componentId2ConfigListMap = ConvertUtil.list2MapOfList(configs,
                                                                                                        ComponentGroupConfig::getComponentId,
                                                                                                        i -> i);
        component.setHostList(componentId2ListMap.get(component.getId()));
        component.setGroupConfigList(componentId2ConfigListMap.get(component.getId()));
    
        return Result.buildSuccess(component);
    }
    
    /**
     * 获取组件下，最新的分组配置信息
     *
     * @param configList 组件对应的所有分组配置
     * @return List<ComponentGroupConfig> 最新分组配置信息
     */
    @NotNull
    private List<ComponentGroupConfig> getLatestGroupConfig(List<ComponentGroupConfig> configList) {
        Map<Integer, List<ComponentGroupConfig>> map = new TreeMap<>((o1, o2) -> o2 - o1);
        configList.forEach(groupConfig -> {
            List<ComponentGroupConfig> list = map.get(Integer.parseInt(groupConfig.getVersion()));
            if (null == list) {
                list = new ArrayList<>();
                map.put(Integer.parseInt(groupConfig.getVersion()), list);
            }
            list.add(groupConfig);
        });
        return map.entrySet().stream().findFirst().get().getValue();
    }

    @Override
    public Result<Boolean> hasPackageDependComponent(int packageId) {
        if (componentRepository.getComponentByPackageId(packageId).size() > 0) {
            return Result.success(true);
        }
        return Result.success(false);
    }

    @Override
    public Result<ComponentGroupConfig> getComponentConfigByGroupName(int componentId, String groupName) {
        List<ComponentGroupConfig> configList = componentGroupConfigRepository.getConfigByComponentId(componentId);
        List<ComponentGroupConfig> filterConfigList = configList.stream().filter(groupConfig -> groupConfig.getGroupName().equals(groupName)).collect(Collectors.toList());
        return Result.buildSuccess(getLatestGroupConfig(filterConfigList).stream().findFirst().get());
    }

    @Override
    public Result<Integer> reportComponentHostStatus(int componentId, String groupName, String host, int status) {
        return Result.buildSuccess(componentHostRepository.updateComponentHostStatus(componentId, host, groupName, status));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Integer> offLine(int componentId) {
        Component component = componentRepository.getComponentById(componentId);
        if (null == component) {
            return Result.fail(ResultCode.COMPONENT_NOT_EXIST_ERROR);
        }
        if(!Strings.isNullOrEmpty(component.getContainComponentIds())) {
            Arrays.stream(component.getContainComponentIds().split(SPLIT)).forEach(id->{
                componentRepository.deleteComponent(Integer.parseInt(id));
            });
        }
        return Result.success(componentRepository.deleteComponent(componentId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createComponent(Component component, Map<String, Set<String>> groupName2HostNotNormalStatusMap) {
        //创建并保存组件
        component.create();
        int componentId = componentRepository.saveComponent(component);

        //依赖组件更新
        if (null != component.getDependComponentId()) {
            Component dependentComponent = componentRepository.getComponentById(component.getDependComponentId());
            dependentComponent.updateContainIds(componentId);
            componentRepository.updateContainIds(dependentComponent.getId(), dependentComponent.getContainComponentIds());
        }

        for (ComponentGroupConfig groupConfig : component.getGroupConfigList()) {

            Set<String> invalidHosts = groupName2HostNotNormalStatusMap.get(groupConfig.getGroupName()) == null ? new HashSet<>() :
            groupName2HostNotNormalStatusMap.get(groupConfig.getGroupName());

            //不依赖配置的组件，更新自己的配置
            if (null == component.getDependConfigComponentId()) {
                //把一些不可用的节点移除掉
                groupConfig.updateInstallConfig(invalidHosts);
                groupConfig.create();
                groupConfig.setComponentId(componentId);
                //创建并保存配置
                componentGroupConfigRepository.saveGroupConfig(groupConfig);
            }
            for (String host : groupConfig.getHosts().split(SPLIT)) {
                //创建并保存组件host
                //TODO 考虑下批量提交
                ComponentHost componentHost = new ComponentHost();
                componentHost.setHost(host);
                componentHost.setProcessNum(JSON.parseObject(groupConfig.getProcessNumConfig()).getInteger(host));
                Optional.ofNullable(groupConfig.getMachineSpec())
                    .map(JSON::parseObject)
                    .map(i -> i.getString(host))
                    .ifPresent(componentHost::setMachineSpec);
                componentHost.setComponentId(componentId);
                componentHost.setGroupName(groupConfig.getGroupName());
                if (invalidHosts.contains(host)) {
                    componentHost.create(HostStatusEnum.OFF_LINE.getStatus());
                } else {
                    componentHost.create();
                }
                componentHostRepository.saveComponentHost(componentHost);
            }
        }

        //更改配置
        //TODO 这里有个场景，我安装的组件配置依赖其他组件的，然后安装过程中如果有节点过滤掉，
        // 都会加入到默认加入(如果不是依赖配置的都会过滤掉)
        if (null != component.getDependConfigComponentId()) {
            changeComponentConfig(component.newDeployComponent());
        }
        return Result.success();
    }

    @Override
    public Result<Component> getComponentById(Integer id) {
        final Component component = componentRepository.getComponentById(id);
        if (Objects.isNull(component)) {
            return Result.fail(ResultCode.COMPONENT_NOT_EXIST_ERROR);
        }
        List<ComponentHost> hosts = componentHostRepository.listHostByComponentId(id);
        component.setHostList(hosts);
        List<ComponentGroupConfig> configList;
        if (null != component.getDependConfigComponentId()) {
            configList = componentGroupConfigRepository.getConfigByComponentId(
                component.getDependConfigComponentId());
        } else {
            configList = componentGroupConfigRepository.getConfigByComponentId(id);
        }
        component.setGroupConfigList(configList);
        return Result.buildSuccess(component);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> scaleComponent(Component component, Map<String, Set<String>> groupName2HostNormalStatusMap, int type) {
        //TODO 需要考虑部分成功以及部分失败场景
        //TODO 优化批量插入能力
        //TODO 扩容后会自动提交多个任务，而不是一个任务自动安装所有
        List<ComponentGroupConfig> configList = getComponentConfig(component.getId()).getData();
        Map<String, ComponentGroupConfig> oriConfigMap = new HashMap<>(configList.size());
        configList.forEach(oriConf -> {
            oriConfigMap.put(oriConf.getGroupName(), oriConf);
        });

        ScaleHandler scaleHandler = scaleHandlerFactory.getScaleHandler(type);
        for (ComponentGroupConfig config : component.getGroupConfigList()) {
            for (String host : config.getHosts().split(SPLIT)) {
                //如果节点成功则加入,因为有些ignore之类的
                Set<String> validHosts = groupName2HostNormalStatusMap.get(config.getGroupName());
                if (null != validHosts && validHosts.contains(host)) {
                    scaleHandler.dealComponentHost(component.getId(), host, config);
                }
            }

            //修改配置
            if (groupName2HostNormalStatusMap.containsKey(config.getGroupName())) {
                ComponentGroupConfig oriGroupConfig = oriConfigMap.get(config.getGroupName());
                scaleHandler.dealComponentGroupConfig(oriGroupConfig, config, groupName2HostNormalStatusMap.get(config.getGroupName()));
            }
        }
        return Result.success();
    }

    @Override
    public Result<Void> changeComponentConfig(Component component) {
        Map<String, ComponentGroupConfig> oriConfigMap = new HashMap<>();
        getComponentConfig(component.getId()).getData().forEach(oriConfig -> {
            oriConfigMap.put(oriConfig.getGroupName(), oriConfig);
        });
        //用改的配置把老的配置替换带点，然后统一所有分组都版本加1
        boolean needChangeConfig = false;
        for (ComponentGroupConfig config : component.getGroupConfigList()) {
            if (!oriConfigMap.get(config.getGroupName()).isSame(config)) {
                needChangeConfig = true;
            }
            config.setVersion(oriConfigMap.get(config.getGroupName()).getVersion());
            config.setComponentId(component.getId());
            oriConfigMap.put(config.getGroupName(), config);
        }

        //统一新建配置
        if (needChangeConfig) {
            oriConfigMap.keySet().forEach(k -> {
                ComponentGroupConfig config = oriConfigMap.get(k);
                config.createWithoutVersion();
                config.setVersion(String.valueOf(Integer.parseInt(config.getVersion()) + 1));
                componentGroupConfigRepository.saveGroupConfig(config);
            });
        } else {
            LOGGER.info("组件[{}]配置未发生变更,老配置", component.getId());
        }
        return Result.success();
    }

    @Override
    public List<Integer> hasPackagesDependComponent(List<Integer> packageIds) {
        List<Component> componentByPackageIds = componentRepository.getComponentByPackageIds(packageIds);
        List<Integer> usingPackageIds = Lists.newArrayList();
        if(!componentByPackageIds.isEmpty()){
            usingPackageIds = componentByPackageIds.stream().map(Component::getPackageId).collect(Collectors.toList());
        }
        return usingPackageIds;
    }

    @Override
    public Result<String> queryComponentNameById(Integer componentId) {
        final Optional<String> nameOpt = componentRepository.queryComponentNameById(componentId);
        return nameOpt.map(Result::buildSuccess)
            .orElseGet(() -> Result.fail(ResultCode.COMPONENT_NOT_EXIST_ERROR));
    }
    
    @Override
    public Result<List<ComponentHost>> queryComponentHostById(Integer componentId) {
        return Result.buildSuccess(componentHostRepository.listHostByComponentId(componentId));
    }
    
    @Override
    public Result<Boolean> checkComponent(Integer componentId) {
        return Result.buildSuccess(Objects.nonNull( componentRepository.getComponentById(componentId)));
    }
    @Override
    public Result<Boolean> deleteComponentIds(List<Integer> deleteComponentIds) {
        //删除配置表
        final List<Integer> delete = componentRepository.listAllComponent().stream()
            .map(Component::getId)
            .filter(deleteComponentIds::contains)
            .collect(Collectors.toList());
    
        final List<Integer> deleteComponentIdsSize = delete.stream()
            .map(componentRepository::deleteComponent)
            .collect(Collectors.toList());
        final boolean deleteCom=deleteComponentIdsSize.size()==delete.size();
        //删除组建表
        final Boolean deleteHostByComponentIds =
            componentHostRepository.deleteByComponentIds(deleteComponentIds);
        
        //删除config列表
        final boolean deleteConfigByComponentIds =
            componentGroupConfigRepository.deleteByComponentIds(
                deleteComponentIds);
        if (!deleteHostByComponentIds || !deleteConfigByComponentIds || !deleteCom) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.fail("清理组建失败");
        }
        
        return Result.success();
    }
    
    @Override
    public Result<Component> queryComponentById(Integer componentId) {
        final Optional<Component> componentOpt = componentRepository.queryComponentById(componentId);
        componentOpt.ifPresent(component -> {
            List<ComponentHost> hosts = componentHostRepository.listComponentHost();
            Map<Integer, List<ComponentHost>> componentId2ListMap = ConvertUtil.list2MapOfList(hosts,
                                                                                               ComponentHost::getComponentId,
                                                                                               i -> i);
            List<ComponentGroupConfig> configs = componentGroupConfigRepository.listGroupConfig();
            Map<Integer, List<ComponentGroupConfig>> componentId2ConfigListMap = ConvertUtil.list2MapOfList(configs,
                                                                                                            ComponentGroupConfig::getComponentId,
                                                                                                            i -> i);
            component.setHostList(componentId2ListMap.get(component.getId()));
            component.setGroupConfigList(componentId2ConfigListMap.get(component.getId()));
        });
        return componentOpt.map(Result::buildSuccess).orElseGet(
                () -> Result.fail(ResultCode.COMPONENT_NOT_EXIST_ERROR));
    }
}