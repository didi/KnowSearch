package com.didichuxing.datachannel.arius.admin.biz.listener;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.COMMA;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionEditEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author didi
 * @date 2022-05-23 2:46 下午
 */
@Component
public class RegionEditEventListener implements ApplicationListener<RegionEditEvent> {

    private static final ILog      LOGGER                           = LogFactory.getLog(RegionEditEventListener.class);

    @Autowired
    private ClusterRegionService   clusterRegionService;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Autowired
    private IndexTemplateService   indexTemplateService;

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @Autowired
    private ESTemplateService      esTemplateService;

    @Autowired
    private ESIndexService         esIndexService;

    public static final String     TEMPLATE_INDEX_INCLUDE_NODE_NAME = "index.routing.allocation.include._name";

    public static final String     NOT_BIND_LOGIC_CLUSTER_ID        = "-1";

    public static final int        RETRY_COUNT                      = 2;

    @Override
    public void onApplicationEvent(RegionEditEvent regionEditEvent) {
        if (CollectionUtils.isEmpty(regionEditEvent.getRegionIdList())) {
            LOGGER.warn("class=RegionEditEventListener||method=onApplicationEvent,warnMsg=region is null");
            return;
        }

        /**
         * key是集群名，value是列表
         */
        Map<String, Set<String>> cluster2TemplateSetMap = new HashMap<String, Set<String>>(16);
        Set<String> nodeNames = new HashSet<>();
        try {
            regionEditEvent.getRegionIdList().forEach(regionId -> {
                ClusterRegion clusterRegion = clusterRegionService.getRegionById(regionId);
                Result<List<ClusterRoleHost>> result = clusterRoleHostService.listByRegionId(Math.toIntExact(regionId));
                if (result.failed()) {
                    LOGGER.error("class=RegionEditEventListener||method=onApplicationEvent,warnMsg={}",
                        result.getMessage());
                    return;
                }
                result.getData().stream().forEach(clusterRoleHost -> nodeNames.add(clusterRoleHost.getNodeSet()));
                String logicClusterIds = clusterRegion.getLogicClusterIds();
                if (!NOT_BIND_LOGIC_CLUSTER_ID.equals(logicClusterIds)) {
                    buildCluster2TemplateSetMap(cluster2TemplateSetMap, clusterRegion, logicClusterIds);
                }
            });
        } catch (Exception e) {
            LOGGER.error("class=RegionEditEventListener||method=onApplicationEvent,warnMsg=build cluster template map error,", e);
            return;
        }

        if (nodeNames.isEmpty()) {
            LOGGER.warn("class=RegionEditEventListener||method=onApplicationEvent,warnMsg=nodeNames is empty");
            return;
        }

        cluster2TemplateSetMap.entrySet().forEach(entry -> {
            for (String templateName : entry.getValue()) {
                try {
                    updateTemplateAllocationSetting(entry.getKey(), templateName, nodeNames);
                    updateIndicesAllocationSetting(entry.getKey(), templateName, nodeNames);
                } catch (Exception e) {
                    LOGGER.error("class=RegionEditEventListener||method=onApplicationEvent,template={}, errMsg={}",
                        templateName, e.getMessage());
                }
            }

        });

    }

    /**
     * 构建集群->模板名的map
     * @param clusterTemplateMap 集群模板map
     * @param clusterRegion 集群region
     * @param logicClusterIds  逻辑集群ids
     */
    private void buildCluster2TemplateSetMap(Map<String, Set<String>> clusterTemplateMap, ClusterRegion clusterRegion,
                                             String logicClusterIds) {
        Arrays.stream(logicClusterIds.split(COMMA)).forEach(logicClusterId -> {
            List<IndexTemplate> indexTemplates = indexTemplateService
                .getLogicClusterTemplates(Long.valueOf(logicClusterId));
            Set<String> logicTemplateNames = clusterTemplateMap.get(clusterRegion.getPhyClusterName());
            if (null == logicTemplateNames) {
                logicTemplateNames = new HashSet<>();
                clusterTemplateMap.put(clusterRegion.getPhyClusterName(), logicTemplateNames);
            }
            for (IndexTemplate indexTemplate : indexTemplates) {
                logicTemplateNames.add(indexTemplate.getName());
            }
        });
    }

    /**
     * region改动后更新索引的setting
     * @param cluster 集群名
     * @param templateName 模板名
     * @param nodeNames 节点名
     * @throws ESOperateException
     */
    private void updateIndicesAllocationSetting(String cluster, String templateName,
                                                Set<String> nodeNames) throws ESOperateException {
        if (ariusConfigInfoService.booleanSetting(AriusConfigConstant.ARIUS_COMMON_GROUP,
            AriusConfigConstant.TEMPLATE_PHYSIC_INDICES_ALLOCATION_IS_EFFECTIVE, true)) {
            boolean response = esIndexService.syncPutIndexSetting(cluster,
                Collections.singletonList(templateName + "*"), TEMPLATE_INDEX_INCLUDE_NODE_NAME,
                String.join(COMMA, nodeNames), "", RETRY_COUNT);
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
     * @param nodeNames
     * @throws ESOperateException
     */
    private void updateTemplateAllocationSetting(String cluster, String templateName,
                                                 Set<String> nodeNames) throws ESOperateException {
        Map<String, String> setting = new HashMap<>(16);
        setting.put(TEMPLATE_INDEX_INCLUDE_NODE_NAME, String.join(COMMA, nodeNames));
        boolean response = esTemplateService.syncUpsertSetting(cluster, templateName, setting, RETRY_COUNT);
        if (!response) {
            LOGGER.error("class=RegionEditEventListener||method=onApplicationEvent,template={}, errMsg={}",
                templateName, "update template setting failed");
        }
    }

}
