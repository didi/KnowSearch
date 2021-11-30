package com.didichuxing.datachannel.arius.admin.metadata.job.template.model;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateHitPO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateHitPOMap {
    protected static final ILog LOGGER = LogFactory.getLog(TemplateHitPOMap.class);

    // 设置实际使用周期时间
    private static final long MAX_QPS_PER_DAY = 100;
    private static final long MINUSE_TIME = 3;
    private static final Long ONE_DAY = 24 * 60 * 60 * 1000L;

    private Map<Long/*id*/, TemplateHitPO> templateHitMap = new HashMap<>();

    public TemplateHitPOMap(Map<String, List<IndexTemplatePhyWithLogic>> templateMap, String date) {
        for(Map.Entry<String, List<IndexTemplatePhyWithLogic>> entry : templateMap.entrySet()){
            String cluster = entry.getKey();
            if (templateMap.get(cluster) == null) {
                continue;
            }

            for (IndexTemplatePhyWithLogic template : templateMap.get(cluster)) {
                if (template == null) {
                    continue;
                }
                templateHitMap.put(template.getId(), new TemplateHitPO(template.getId().intValue(), template.getName(), template.getCluster(), date));
            }
        }
    }

    public void addData(IndexTemplatePhyWithLogic template, String indexName, long count) {
        if (templateHitMap.get(template.getId()) != null) {
            templateHitMap.get(template.getId()).addCount(indexName, count);
        }
    }

    public void setUseTime(Map<String, List<IndexTemplatePhyWithLogic>> templateMap) {
        for(Map.Entry<String, List<IndexTemplatePhyWithLogic>> entry : templateMap.entrySet()){
            String cluster = entry.getKey();

            if(templateMap.get(cluster) == null) {
                LOGGER.info("class=TemplateHitPOMap||method=setUseTime||msg=TemplateHitPOMap setUseTime have null cluster, cluster:" + cluster);
                continue;
            }

            handleTemplateMap(templateMap, cluster);
        }

    }

    private void handleTemplateMap(Map<String, List<IndexTemplatePhyWithLogic>> templateMap, String cluster) {
        for(IndexTemplatePhyWithLogic template : templateMap.get(cluster)) {
            if(template==null) {
                LOGGER.info("class=TemplateHitPOMap||method=handleTemplateMap||msg=TemplateHitPOMap setUseTime have null template");
                continue;
            }

            TemplateHitPO templateHitPO = templateHitMap.get(template.getId());
            if(templateHitPO==null) {
                LOGGER.info("class=TemplateHitPOMap||method=handleTemplateMap||msg=TemplateHitPOMap setUseTime have null templateHitPO, id:" + template.getId());
                continue;
            }

            long useTimeLong = templateHitPO.getUsedDayNum(template.getExpression(), template.getLogicTemplate().getDateFormat(), MAX_QPS_PER_DAY);
            if(useTimeLong<=0) {
                LOGGER.info("class=TemplateHitPOMap||method=handleTemplateMap||msg=TemplateHitPOMap setUseTime useTimeLong<0, useTimeLong:" + useTimeLong);
                templateHitPO.setUseTime(useTimeLong);
            }

            long useTime = (useTimeLong+ ONE_DAY)/ ONE_DAY;
            if(useTime< MINUSE_TIME) {
                useTime = MINUSE_TIME;
            }

            templateHitPO.setUseTime(useTime);

            templateHitPO.setSumHits(template.getExpression(), template.getLogicTemplate().getDateFormat());
        }
    }

    public List<TemplateHitPO> toList() {
        return Lists.newArrayList( templateHitMap.values());
    }

    public Map<Long, TemplateHitPO> getMap() {
        return templateHitMap;
    }
}
