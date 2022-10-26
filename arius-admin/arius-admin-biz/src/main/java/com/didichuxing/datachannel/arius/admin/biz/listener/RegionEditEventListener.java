package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionEditByAttributeEvent;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionEditByHostEvent;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionEditEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.COMMA;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.HISTORY_TEMPLATE_PHYSIC_INDICES_ALLOCATION_IS_EFFECTIVE_DEFAULT_VALUE;

/**
 * @author didi
 * @date 2022-05-23 2:46 下午
 */
@Component
public class RegionEditEventListener implements ApplicationListener<RegionEditEvent> {

    private static final ILog       LOGGER                           = LogFactory.getLog(RegionEditEventListener.class);

    @Autowired
    private ClusterRegionService    clusterRegionService;

    @Autowired
    private ClusterRoleHostService  clusterRoleHostService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private AriusConfigInfoService  ariusConfigInfoService;

    @Autowired
    private ESTemplateService       esTemplateService;

    @Autowired
    private ESIndexService          esIndexService;

    public static final String      TEMPLATE_INDEX_INCLUDE_NODE_NAME = "index.routing.allocation.include._name";

    public static final String      TEMPLATE_INDEX_INCLUDE_ATTRIBUTE_PREFIX = "index.routing.allocation.include.";

    public static final String      NOT_BIND_LOGIC_CLUSTER_ID        = "-1";

    public static final int         RETRY_COUNT                      = 2;

    @Override
    public void onApplicationEvent(RegionEditEvent regionEditEvent) {
        if (CollectionUtils.isEmpty(regionEditEvent.getRegionIdList())) {
            LOGGER.warn("class=RegionEditEventListener||method=onApplicationEvent,warnMsg=region is null");
            return;
        }

        if(regionEditEvent instanceof RegionEditByAttributeEvent){
            handleRegionEditByAttributeEvent((RegionEditByAttributeEvent)regionEditEvent);
        }else if (regionEditEvent instanceof RegionEditByHostEvent){
            handleRegionEditByHostEvent((RegionEditByHostEvent)regionEditEvent);
        }

    }

    /**
     * 根据host划分region时的handle
     * @param event
     */
    private void handleRegionEditByHostEvent(RegionEditByHostEvent event){
        Map<String/*集群名*/, List<TemplateWithNodeNames>> cluster2TemplateWithNodeNames = new HashMap<>(16);
        try {
            event.getRegionIdList().forEach(regionId -> {
                ClusterRegion clusterRegion = clusterRegionService.getRegionById(regionId);
                Set<String> nodeNames = new HashSet<>();
                Result<List<ClusterRoleHost>> result = clusterRoleHostService.listByRegionId(Math.toIntExact(regionId));
                if (result.failed()) {
                    LOGGER.error("class=RegionEditEventListener||method=onApplicationEvent,warnMsg={}",
                            result.getMessage());
                    return;
                }
                result.getData().stream().forEach(clusterRoleHost -> nodeNames.add(clusterRoleHost.getNodeSet()));
                buildCluster2TemplateWithNodeNamesSetMap(cluster2TemplateWithNodeNames, clusterRegion, nodeNames);
            });
        } catch (Exception e) {
            LOGGER.error(
                    "class=RegionEditEventListener||method=handleRegionEditByHostEvent,warnMsg=build cluster template map error,", e);
            return;
        }

        cluster2TemplateWithNodeNames.entrySet().forEach(entry -> {
            for (TemplateWithNodeNames templateWithNodeNames : entry.getValue()) {
                try {
                    if (templateWithNodeNames.getNodeNames().isEmpty()) {
                        LOGGER.warn("class=RegionEditEventListener||method=handleRegionEditByHostEvent,template={}, errMsg={}",
                                templateWithNodeNames.getTemplateName(), "has no node names");
                        continue;
                    }
                    updateTemplateAllocationSetting(entry.getKey(), templateWithNodeNames.getTemplateName(),
                            TEMPLATE_INDEX_INCLUDE_NODE_NAME, templateWithNodeNames.getNodeNames());
                    updateIndicesAllocationSetting(entry.getKey(), templateWithNodeNames.getTemplateName(),
                            TEMPLATE_INDEX_INCLUDE_NODE_NAME, templateWithNodeNames.getNodeNames());
                } catch (Exception e) {
                    LOGGER.error("class=RegionEditEventListener||method=handleRegionEditByHostEvent,template={}, errMsg={}",
                            templateWithNodeNames.getTemplateName(), e.getMessage());
                }
            }
        });
    }

    /**
     * 根据attribute划分region时的handle
     * @param event
     */
    private void handleRegionEditByAttributeEvent(RegionEditByAttributeEvent event){
        Map<String/*集群名*/, List<TemplateWithAttributeValues>> cluster2TemplateWithAttributeValues = new HashMap<>(16);
        try {
            event.getRegionIdList().forEach(regionId -> {
                ClusterRegion clusterRegion = clusterRegionService.getRegionById(regionId);
                Result<List<ClusterRoleHost>> result = clusterRoleHostService.listByRegionId(Math.toIntExact(regionId));
                if (result.failed()) {
                    LOGGER.error("class=RegionEditEventListener||method=onApplicationEvent,warnMsg={}",
                            result.getMessage());
                    return;
                }

                // 构建attribute属性信息（根据划分方式attribute的属性构建）
                Set<String> attributeValueSet = Sets.newHashSet();
                List<String> attributesList = result.getData().stream().filter(Objects::nonNull)
                        .map(ClusterRoleHost::getAttributes).filter(attributes -> !AriusObjUtils.isBlank(attributes))
                        .distinct().collect(Collectors.toList());
                for (String attributes : attributesList) {
                    Map<String, String> attributeMap = ConvertUtil.str2Map(attributes);
                    attributeValueSet.add(attributeMap.get(event.getAttributeKey()));
                }
                // 构建集群 -> 模版以及对应的attribute值
                buildCluster2TemplateWithAttributeValuesSetMap(cluster2TemplateWithAttributeValues, clusterRegion, attributeValueSet);
            });
        } catch (Exception e) {
            LOGGER.error(
                    "class=RegionEditEventListener||method=handleRegionEditByAttributeEvent,warnMsg=build cluster template map error,", e);
            return;
        }

        cluster2TemplateWithAttributeValues.entrySet().forEach(entry -> {
            for (TemplateWithAttributeValues templateWithAttributeValues : entry.getValue()) {
                try {
                    if (templateWithAttributeValues.getAttributeValues().isEmpty()) {
                        LOGGER.warn("class=RegionEditEventListener||method=handleRegionEditByAttributeEvent,template={}, errMsg={}",
                                templateWithAttributeValues.getTemplateName(), "has no attribute values");
                        continue;
                    }
                    updateTemplateAllocationSetting(entry.getKey(), templateWithAttributeValues.getTemplateName(),
                            TEMPLATE_INDEX_INCLUDE_ATTRIBUTE_PREFIX + event.getAttributeKey(),
                            templateWithAttributeValues.getAttributeValues());
                    updateIndicesAllocationSetting(entry.getKey(), templateWithAttributeValues.getTemplateName(),
                            TEMPLATE_INDEX_INCLUDE_ATTRIBUTE_PREFIX + event.getAttributeKey(),
                            templateWithAttributeValues.getAttributeValues());
                } catch (Exception e) {
                    LOGGER.error("class=RegionEditEventListener||method=handleRegionEditByAttributeEvent,template={}, errMsg={}",
                            templateWithAttributeValues.getTemplateName(), e.getMessage());
                }
            }
        });
    }

    /**
     * 构建集群->模板以及对应的节点名称
     * @param cluster2TemplateWithNodeNames map
     * @param clusterRegion region
     * @param nodeNames 节点名称
     */
    private void buildCluster2TemplateWithNodeNamesSetMap(Map<String, List<TemplateWithNodeNames>> cluster2TemplateWithNodeNames,
                                                          ClusterRegion clusterRegion, Set<String> nodeNames) {
        Result<List<IndexTemplatePhy>> templatePhyListResult = indexTemplatePhyService
            .listByRegionId(Math.toIntExact(clusterRegion.getId()));
        if (templatePhyListResult.failed()) {
            LOGGER.error(
                "class=RegionEditEventListener||method=buildCluster2TemplateWithNodeNamesSetMap||region={}||err={}",
                clusterRegion.getId(), "update indices setting failed");
            return;
        }

        List<TemplateWithNodeNames> templateWithNodeNamesList = cluster2TemplateWithNodeNames
            .getOrDefault(clusterRegion.getPhyClusterName(), new ArrayList<>());

        for (IndexTemplatePhy indexTemplatePhy : templatePhyListResult.getData()) {
            TemplateWithNodeNames templateWithNodeNames = new TemplateWithNodeNames();
            templateWithNodeNames.setNodeNames(nodeNames);
            templateWithNodeNames.setTemplateName(indexTemplatePhy.getName());
            templateWithNodeNamesList.add(templateWithNodeNames);
        }

        cluster2TemplateWithNodeNames.put(clusterRegion.getPhyClusterName(), templateWithNodeNamesList);
    }

    /**
     * 构建集群 -> 模版以及对应的attribute值
     * @param cluster2TemplateWithAttributeValues   map
     * @param clusterRegion     region
     * @param attributeValues   attribute值
     */
    private void buildCluster2TemplateWithAttributeValuesSetMap(Map<String, List<TemplateWithAttributeValues>> cluster2TemplateWithAttributeValues,
                                                                ClusterRegion clusterRegion, Set<String> attributeValues) {
        Result<List<IndexTemplatePhy>> templatePhyListResult = indexTemplatePhyService
                .listByRegionId(Math.toIntExact(clusterRegion.getId()));
        if (templatePhyListResult.failed()) {
            LOGGER.error(
                    "class=RegionEditEventListener||method=buildCluster2TemplateWithAttributeValuesSetMap||region={}||err={}",
                    clusterRegion.getId(), "select physical templates failed");
            return;
        }

        List<TemplateWithAttributeValues> templateWithAttributeValuesList = cluster2TemplateWithAttributeValues
                .getOrDefault(clusterRegion.getPhyClusterName(), new ArrayList<>());
        for (IndexTemplatePhy indexTemplatePhy : templatePhyListResult.getData()) {
            TemplateWithAttributeValues templateWithAttributeValues = new TemplateWithAttributeValues();
            templateWithAttributeValues.setTemplateName(indexTemplatePhy.getName());
            templateWithAttributeValues.setAttributeValues(attributeValues);
            templateWithAttributeValuesList.add(templateWithAttributeValues);
        }
        cluster2TemplateWithAttributeValues.put(clusterRegion.getPhyClusterName(), templateWithAttributeValuesList);
    }

    /**
     * region改动后更新索引的setting
     * @param cluster 集群名
     * @param templateName 模板名
     * @param includeValues setting的value
     * @throws ESOperateException
     */
    private void updateIndicesAllocationSetting(String cluster, String templateName, String key,
                                                Set<String> includeValues) throws ESOperateException {
        if (ariusConfigInfoService.booleanSetting(AriusConfigConstant.ARIUS_TEMPLATE_GROUP,
            AriusConfigConstant.HISTORY_TEMPLATE_PHYSIC_INDICES_ALLOCATION_IS_EFFECTIVE, HISTORY_TEMPLATE_PHYSIC_INDICES_ALLOCATION_IS_EFFECTIVE_DEFAULT_VALUE)) {
            boolean response = esIndexService.syncPutIndexSetting(cluster,
                Collections.singletonList(templateName + "*"), key,
                String.join(COMMA, includeValues), "", RETRY_COUNT);
            if (!response) {
                LOGGER.error("class=RegionEditEventListener||method=onApplicationEvent,template={}, errMsg={}",
                    templateName, "update indices setting failed");
            }
        }
    }

    /**
     * 更新模板分配setting
     * @param cluster 集群名
     * @param templateName 模板名
     * @param includeValues setting的value
     * @throws ESOperateException
     */
    private void updateTemplateAllocationSetting(String cluster, String templateName, String key,
                                                 Set<String> includeValues) throws ESOperateException {
        Map<String, String> setting = new HashMap<>(16);
        setting.put(key, String.join(COMMA, includeValues));
        boolean response = esTemplateService.syncUpsertSetting(cluster, templateName, setting, RETRY_COUNT);
        if (!response) {
            LOGGER.error("class=RegionEditEventListener||method=onApplicationEvent,template={}, errMsg={}",
                templateName, "update template setting failed");
        }
    }

    @Data
    public class Template {
        private String      templateName;
    }

    @Data
    public class TemplateWithNodeNames extends Template{
        private Set<String> nodeNames;
    }

    @Data
    public class TemplateWithAttributeValues extends Template{
        private Set<String> attributeValues;
    }
}
