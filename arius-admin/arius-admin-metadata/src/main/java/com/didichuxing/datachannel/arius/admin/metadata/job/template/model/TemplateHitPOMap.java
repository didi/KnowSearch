package com.didichuxing.datachannel.arius.admin.metadata.job.template.model;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateHitPO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateHitPOMap {
    protected static final ILog LOGGER = LogFactory.getLog(TemplateHitPOMap.class);

    // 设置实际使用周期时间
    private static final long MAX_QPS_PER_DAY = 100;
    private static final long MINUSE_TIME = 3;
    private static final Long _ONE_DAY = 24 * 60 * 60 * 1000L;

    private Map<Long/*id*/, TemplateHitPO> templateHitPOMap = new HashMap<>();

    public TemplateHitPOMap(Map<String, List<IndexTemplatePhyWithLogic>> templateMap, String date) {
        for (String cluster : templateMap.keySet()) {
            if (templateMap.get(cluster) == null) {
                continue;
            }

            for (IndexTemplatePhyWithLogic template : templateMap.get(cluster)) {
                if (template == null) {
                    continue;
                }
                templateHitPOMap.put(template.getId(), new TemplateHitPO(template.getId().intValue(), template.getName(), template.getCluster(), date));
            }
        }
    }

    public void addData(IndexTemplatePhyWithLogic template, String indexName, long count, String date) {
        if (templateHitPOMap.get(template.getId()) != null) {
            templateHitPOMap.get(template.getId()).addCount(indexName, count);
        }
    }

    public void setUseTime(Map<String, List<IndexTemplatePhyWithLogic>> templateMap) {
        for(String cluster : templateMap.keySet()) {
            if(templateMap.get(cluster) == null) {
                LOGGER.info("TemplateHitPOMap setUseTime have null cluster, cluster:" + cluster);
                continue;
            }

            for(IndexTemplatePhyWithLogic template : templateMap.get(cluster)) {
                if(template==null) {
                    LOGGER.info("TemplateHitPOMap setUseTime have null template");
                    continue;
                }

                TemplateHitPO TemplateHitPO = templateHitPOMap.get(template.getId());
                if(TemplateHitPO==null) {
                    LOGGER.info("TemplateHitPOMap setUseTime have null TemplateHitPO, id:" + template.getId());
                    continue;
                }

                long useTimeLong = TemplateHitPO.getUsedDayNum(template.getExpression(), template.getLogicTemplate().getDateFormat(), MAX_QPS_PER_DAY);
                if(useTimeLong<=0) {
                    LOGGER.info("TemplateHitPOMap setUseTime useTimeLong<0, useTimeLong:" + useTimeLong);
                    TemplateHitPO.setUseTime(useTimeLong);
                }

                long useTime = (useTimeLong+_ONE_DAY)/_ONE_DAY;
                if(useTime< MINUSE_TIME) {
                    useTime = MINUSE_TIME;
                }

                TemplateHitPO.setUseTime(useTime);

                TemplateHitPO.setSumHits(template.getExpression(), template.getLogicTemplate().getDateFormat());
            }
        }

    }

    public List<TemplateHitPO> toList() {
        return Lists.newArrayList(templateHitPOMap.values());
    }

    public Map<Long, TemplateHitPO> getMap() {
        return templateHitPOMap;
    }
}
