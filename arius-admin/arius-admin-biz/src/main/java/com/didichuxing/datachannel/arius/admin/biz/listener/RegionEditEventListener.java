package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionEditEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.EventException;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.COMMA;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.HISTORY_TEMPLATE_PHYSIC_INDICES_ALLOCATION_IS_EFFECTIVE_DEFAULT_VALUE;

/**
 * @author didi
 * @date 2022-05-23 2:46 下午
 */
@Component
public class RegionEditEventListener extends ApplicationRetryListener <RegionEditEvent> {

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

    //用来判断事件是否需要重试
    public boolean flag = true;

    public static final String      TEMPLATE_INDEX_INCLUDE_NODE_NAME = "index.routing.allocation.include._name";

    public static final String      NOT_BIND_LOGIC_CLUSTER_ID        = "-1";

    public static final int         RETRY_COUNT                      = 2;

    @Override
    public boolean onApplicationRetryEvent(RegionEditEvent regionEditEvent) throws EventException {
        if (CollectionUtils.isEmpty(regionEditEvent.getRegionIdList())) {
            LOGGER.warn("class=RegionEditEventListener||method=onApplicationEvent,warnMsg=region is null");
            return false;
        }

        /**
         * key是集群名，value是TemplateWithNodeNames
         */
        Map<String, List<TemplateWithNodeNames>> cluster2TemplateWithNodeNames = new HashMap<String, List<TemplateWithNodeNames>>(
                16);
        try {
            regionEditEvent.getRegionIdList().forEach(regionId -> {
                ClusterRegion clusterRegion = clusterRegionService.getRegionById(regionId);
                Set<String> nodeNames = new HashSet<>();
                Result<List<ClusterRoleHost>> result = clusterRoleHostService.listByRegionId(Math.toIntExact(regionId));
                if (result.failed()) {
                    LOGGER.error("class=RegionEditEventListener||method=onApplicationEvent,warnMsg={}",
                            result.getMessage());
                    flag = false;
                    return;
                }
                result.getData().stream().forEach(clusterRoleHost -> nodeNames.add(clusterRoleHost.getNodeSet()));
                flag = buildCluster2TemplateWithNodeNamesSetMap(cluster2TemplateWithNodeNames, clusterRegion, nodeNames);
            });
        } catch (Exception e) {
            LOGGER.error(
                    "class=RegionEditEventListener||method=onApplicationEvent,warnMsg=build cluster template map error,",
                    e);
            throw new EventException(e.getMessage(), e);
        }

        cluster2TemplateWithNodeNames.entrySet().forEach(entry -> {
            for (TemplateWithNodeNames templateWithNodeNames : entry.getValue()) {
                try {
                    if (templateWithNodeNames.getNodeNames().isEmpty()) {
                        LOGGER.warn("class=RegionEditEventListener||method=onApplicationEvent,template={}, errMsg={}",
                                templateWithNodeNames.getTemplateName(), "has no node names");
                        flag = false;
                        continue;
                    }
                    if (updateTemplateAllocationSetting(entry.getKey(), templateWithNodeNames.getTemplateName(),
                            templateWithNodeNames.getNodeNames()) == false || updateIndicesAllocationSetting(entry.getKey(), templateWithNodeNames.getTemplateName(),
                            templateWithNodeNames.getNodeNames()) == false){
                        flag = false;
                    };
                } catch (Exception e) {
                    LOGGER.error("class=RegionEditEventListener||method=onApplicationEvent,template={}, errMsg={}",
                            templateWithNodeNames.getTemplateName(), e.getMessage());
                    flag = false;
                }
            }

        });
        //flag为false时，事件需要重试
        return flag;
    }

    /**
     * 构建集群->模板以及对应的节点名称
     * @param cluster2TemplateWithNodeNames map
     * @param clusterRegion region
     * @param nodeNames 节点名称
     */
    private boolean buildCluster2TemplateWithNodeNamesSetMap(Map<String, List<TemplateWithNodeNames>> cluster2TemplateWithNodeNames,
                                                          ClusterRegion clusterRegion, Set<String> nodeNames) {
        Result<List<IndexTemplatePhy>> templatePhyListResult = indexTemplatePhyService
            .listByRegionId(Math.toIntExact(clusterRegion.getId()));
        if (templatePhyListResult.failed()) {
            LOGGER.error(
                "class=RegionEditEventListener||method=buildCluster2TemplateWithNodeNamesSetMap||region={}||err={}",
                clusterRegion.getId(), "update indices setting failed");
            return false;
        }

        List<TemplateWithNodeNames> templateWithNodeNamesList = cluster2TemplateWithNodeNames
            .get(clusterRegion.getPhyClusterName());
        if (null == templateWithNodeNamesList) {
            templateWithNodeNamesList = new ArrayList<>();
            cluster2TemplateWithNodeNames.put(clusterRegion.getPhyClusterName(), templateWithNodeNamesList);
        }

        for (IndexTemplatePhy indexTemplatePhy : templatePhyListResult.getData()) {
            TemplateWithNodeNames templateWithNodeNames = new TemplateWithNodeNames();
            templateWithNodeNames.setNodeNames(nodeNames);
            templateWithNodeNames.setTemplateName(indexTemplatePhy.getName());
            templateWithNodeNamesList.add(templateWithNodeNames);
        }
        return true;
    }

    /**
     * region改动后更新索引的setting
     * @param cluster 集群名
     * @param templateName 模板名
     * @param nodeNames 节点名
     * @throws ESOperateException
     */
    private boolean updateIndicesAllocationSetting(String cluster, String templateName,
                                                Set<String> nodeNames) throws ESOperateException {
        if (ariusConfigInfoService.booleanSetting(AriusConfigConstant.ARIUS_TEMPLATE_GROUP,
            AriusConfigConstant.HISTORY_TEMPLATE_PHYSIC_INDICES_ALLOCATION_IS_EFFECTIVE, HISTORY_TEMPLATE_PHYSIC_INDICES_ALLOCATION_IS_EFFECTIVE_DEFAULT_VALUE)) {
            boolean response = esIndexService.syncPutIndexSetting(cluster,
                Collections.singletonList(templateName + "*"), TEMPLATE_INDEX_INCLUDE_NODE_NAME,
                String.join(COMMA, nodeNames), "", RETRY_COUNT);
            if (!response) {
                LOGGER.error("class=RegionEditEventListener||method=onApplicationEvent,template={}, errMsg={}",
                    templateName, "update indices setting failed");
                return false;
            }
        }
        return true;
    }

    /**
     * 更新模板分配setting
     * @param cluster 集群名
     * @param templateName 模板名
     * @param nodeNames
     * @throws ESOperateException
     */
    private boolean updateTemplateAllocationSetting(String cluster, String templateName,
                                                 Set<String> nodeNames) throws ESOperateException {
        Map<String, String> setting = new HashMap<>(16);
        setting.put(TEMPLATE_INDEX_INCLUDE_NODE_NAME, String.join(COMMA, nodeNames));
        boolean response = esTemplateService.syncUpsertSetting(cluster, templateName, setting, RETRY_COUNT);
        if (!response) {
            LOGGER.error("class=RegionEditEventListener||method=onApplicationEvent,template={}, errMsg={}",
                templateName, "update template setting failed");
            return false;
        }
        return true;
    }

    @Data
    public class TemplateWithNodeNames {
        private String      templateName;
        private Set<String> nodeNames;
    }

}
