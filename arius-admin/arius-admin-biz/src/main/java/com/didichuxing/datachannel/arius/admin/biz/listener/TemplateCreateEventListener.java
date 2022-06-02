package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.event.template.TemplateCreateEvent;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author chengxiang
 * @date 2022/5/30
 */
@Component
public class TemplateCreateEventListener implements ApplicationListener<TemplateCreateEvent> {

    private final String TEMPLATE_INDEX_INCLUDE_NODE_NAME = "index.routing.allocation.include._name";
    private final Integer RETRY_TIMES = 3;

    public static final ILog LOGGER = LogFactory.getLog(TemplateCreateEventListener.class);

    @Autowired
    private ESTemplateService esTemplateService;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Override
    public void onApplicationEvent(TemplateCreateEvent event) {
        if (null == event.getIndexTemplateDTO()) {
            LOGGER.warn("class=TemplateCreateEventListener||method=onApplicationEvent||msg=TemplateCreateEvent is null");
            return;
        }
        IndexTemplateDTO indexTemplateDTO = event.getIndexTemplateDTO();
        String templateName = indexTemplateDTO.getName();

        if (null == indexTemplateDTO.getPhysicalInfos()) {
            LOGGER.warn("class=TemplateCreateEventListener||method=onApplicationEvent||msg=indexTemplateDTO.getPhysicalInfos() is null");
            return;
        }
        String cluster = indexTemplateDTO.getPhysicalInfos().get(0).getCluster();

        Set<String> nodeNames = new HashSet<>();
        Result<List<ClusterRoleHost>> roleHostResult = clusterRoleHostService.listByRegionId(indexTemplateDTO.getRegionId());
        roleHostResult.getData().stream().forEach(roleHost -> nodeNames.add(roleHost.getNodeSet()));

        Map<String, String> setting = new HashMap<>(16);
        setting.put(TEMPLATE_INDEX_INCLUDE_NODE_NAME, String.join(",", nodeNames));
        try {
            boolean response = esTemplateService.syncUpsertSetting(cluster, templateName, setting, RETRY_TIMES);
            if (!response) {
                LOGGER.warn("class=TemplateCreateEventListener||method=onApplicationEvent||msg=syncUpsertSetting failed||cluster={}||templateName={}||setting={}", cluster, templateName, setting);
            }
        } catch (Exception e) {
            LOGGER.error("class=TemplateCreateEventListener||method=onApplicationEvent||msg=esTemplateService.syncUpsertSetting error", e);
        }
    }
}
