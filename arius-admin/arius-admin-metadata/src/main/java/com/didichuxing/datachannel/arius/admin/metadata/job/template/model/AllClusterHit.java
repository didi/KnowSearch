package com.didichuxing.datachannel.arius.admin.metadata.job.template.model;


import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllClusterHit {
    private static final ILog LOGGER = LogFactory.getLog(AllClusterHit.class);

    private Map<String /* clusterName */, ClusterHitNode> m = new HashMap<>();

    public AllClusterHit(long time, Map<String, List<IndexTemplatePhyWithLogic>> templates, ESClusterService esClusterService) {
        for(Map.Entry<String, List<IndexTemplatePhyWithLogic>> entry : templates.entrySet()){
            String clusterName = entry.getKey();
            m.put(clusterName, new ClusterHitNode(time, clusterName, templates.get(clusterName), esClusterService));
        }
    }

    /**
     * 查询实际命中的索引名称进行匹配
     *
     * @param index
     * @param count
     * @param templateHitPOMap
     * @param date
     */
    public void matchIndex(String index, Long count, TemplateHitPOMap templateHitPOMap, String date) {
        boolean match = false;
        for(Map.Entry<String, ClusterHitNode> entry : m.entrySet()){
            String cluster = entry.getKey();
            if (m.get(cluster).matchIndex(index, count, templateHitPOMap, date, false)) {
                match = true;
            }
        }

        if (!match) {
            LOGGER.error("class=AllClusterHit||method=matchIndex||errMsg=index not found template, index {}", index);
        }
    }

    /**
     * 根据用户传入的索引名称进行匹配
     *
     * @param indices
     * @param count
     * @param templateHitPOMap
     * @param date
     */
    public void matchIndices(String indices, Long count, TemplateHitPOMap templateHitPOMap, String date) {
        boolean match = false;
        for(Map.Entry<String, ClusterHitNode> entry : m.entrySet()){
            String cluster = entry.getKey();
            if (m.get(cluster).matchIndices(indices, count, templateHitPOMap, date)) {
                match = true;
            }
        }

        if (!match) {
            LOGGER.error("class=AllClusterHit||method=matchIndices||errMsg=indices not found template, indices {}", indices);
        }
    }

}
