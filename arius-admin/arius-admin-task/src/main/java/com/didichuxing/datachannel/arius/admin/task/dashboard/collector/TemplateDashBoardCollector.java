package com.didichuxing.datachannel.arius.admin.task.dashboard.collector;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.shard.Segment;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.DashBoardStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.TemplateMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESShardService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by linyunan on 3/11/22
 * dashboard单个集群模板采集器
 */
@Component
public class TemplateDashBoardCollector extends BaseDashboardCollector {
    private static final ILog LOGGER = LogFactory.getLog(TemplateDashBoardCollector.class);

    @Autowired
    IndexTemplatePhyService   indexTemplatePhyService;

    @Autowired
    ESShardService            esShardService;

    @Override
    public void collectSingleCluster(String cluster, long currentTime) {
        List<IndexTemplatePhyWithLogic> logicTemplates = indexTemplatePhyService.getTemplateByPhyCluster(cluster);
        if (logicTemplates.isEmpty()) {
            LOGGER.error(
                "class=TemplateDashBoardCollector||method=collectSingleCluster||errMsg=clusterTemplateList is null");
            return;
        }
        List<Segment> segments = esShardService.syncGetSegments(cluster);
        if (segments.isEmpty()) {
            LOGGER.error("class=TemplateDashBoardCollector||method=collectSingleCluster||errMsg=segments is null");
            return;
        }

        List<DashBoardStats> dashBoardStatsList = Lists.newArrayList();
        for (IndexTemplatePhyWithLogic indexTemplatePhyWithLogic : logicTemplates) {
            DashBoardStats dashBoardStats = buildInitDashBoardStats(currentTime);
            IndexTemplate indexTemplate = indexTemplatePhyWithLogic.getLogicTemplate();

            TemplateMetrics templateMetrics = new TemplateMetrics();
            templateMetrics.setTimestamp(currentTime);
            templateMetrics.setCluster(cluster);
            templateMetrics.setTemplate(indexTemplate.getName());
            templateMetrics.setTemplateId((long) indexTemplate.getId());

            //1 template segments 数量以及占用内存大小
            buildTemplateStats(templateMetrics, segments, indexTemplatePhyWithLogic.getExpression());

            dashBoardStats.setTemplate(templateMetrics);
            dashBoardStatsList.add(dashBoardStats);
        }

        if (CollectionUtils.isEmpty(dashBoardStatsList)) {
            return;
        }

        monitorMetricsSender.sendDashboardStats(dashBoardStatsList);
    }

    @Override
    public void collectAllCluster(List<String> clusterList, long currentTime) {
    }

    @Override
    public String getName() {
        return "TemplateDashBoardCollector";
    }

    private void buildTemplateStats(TemplateMetrics templateMetrics, List<Segment> clusterSegments, String expression) {
        //这里传入的segments 是整个cluster 所有segments，先按照template expression 过滤出该模板所有的segments
        List<Segment> matchExpSegments = clusterSegments.stream()
            .filter(s -> IndexNameUtils.indexExpMatch(s.getIndex(), expression)).collect(Collectors.toList());

        templateMetrics.setSegmentNum((long) matchExpSegments.size());
        templateMetrics.setSegmentMemSize(matchExpSegments.stream().mapToDouble(Segment::getMemoSize).sum() );
    }
}