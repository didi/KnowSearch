package com.didichuxing.datachannel.arius.admin.metadata.job.template;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateHitPO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayJoinESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateHitESDAO;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.metadata.job.template.model.AllClusterHit;
import com.didichuxing.datachannel.arius.admin.metadata.job.template.model.AllHitMetric;
import com.didichuxing.datachannel.arius.admin.metadata.job.template.model.TemplateHitPOMap;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.elasticsearch.common.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;


@Component
public class TemplateHitCollector extends AbstractMetaDataJob {

    private static final long _ONE_MINUTE = 60 * 1000;
    private static final Long _ONE_DAY = 24 * 60 * 60 * 1000L;

    @Autowired
    private TemplateHitESDAO templateHitEsDao;

    @Autowired
    private GatewayJoinESDAO gatewayJoinEsDao;

    @Autowired
    private TemplatePhyService templatePhyService;

    @Autowired
    private ESClusterService esClusterService;

    @Override
    public Object handleJobTask(String date) {
        LOGGER.info("templateHitCollector execute param -> [{}]", date);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("get search field");

        Long time = DateTimeUtil.getTime(date);
        date = DateTimeUtil.getDateStr(time);

        Map<String/*clusterName*/, List<IndexTemplatePhyWithLogic>> templateMap = Maps.newHashMap();

        List<IndexTemplatePhyWithLogic> templatePhies = templatePhyService.listTemplateWithLogicWithCache();

        for (IndexTemplatePhyWithLogic indexTemplate : templatePhies) {
            templateMap.computeIfAbsent(indexTemplate.getCluster(),
                    key -> Lists.newLinkedList()).add(indexTemplate);
        }

        AllClusterHit allClusterHit = new AllClusterHit(time, templateMap, esClusterService);
        stopWatch.stop().start("prepare template date");

        AllHitMetric allMetric = getAllIndicesMetric(time);
        LOGGER.info("all metric map:" + JSON.toJSONString(allMetric));
        stopWatch.stop().start("agg template index name hit");

        TemplateHitPOMap templateHitPoMap = new TemplateHitPOMap(templateMap, date);
        toTemplateHitPo(allMetric, date, allClusterHit, templateHitPoMap);
        stopWatch.stop().start("save result");

        // 计算实际使用时间
        templateHitPoMap.setUseTime(templateMap);

        List<TemplateHitPO> l =  templateHitPoMap.toList();

        boolean operatorResult = templateHitEsDao.batchInsert(l);

        LOGGER.info("templateHitCollector templateNameHitMap size {}, operatorResult {} cost {}", templateHitPoMap.getMap().size(), operatorResult, stopWatch.stop());

        return JOB_SUCCESS;
    }

    /**
     * 转换成TemplateHitPOMap对象
     * @param metric
     * @param date
     * @param allClusterHit
     * @param templateHitPoMap
     * @return
     */
    private TemplateHitPOMap toTemplateHitPo(AllHitMetric metric, String date, AllClusterHit allClusterHit, TemplateHitPOMap templateHitPoMap) {
        // 查询实际命中索引名称进行匹配
        for (Map.Entry<String/*indexName*/, AllHitMetric.MetricNode> entry : metric.getHitMap().entrySet()) {
            allClusterHit.matchIndex(entry.getKey(), entry.getValue().getCount(), templateHitPoMap, date);
        }

        // 聚合查询时传入的索引名称进行匹配
        for (Map.Entry<String/*indexName*/, AllHitMetric.MetricNode> entry : metric.getAggsMap().entrySet()) {
            allClusterHit.matchIndices(entry.getKey(), entry.getValue().getCount(), templateHitPoMap, date);
        }

        return templateHitPoMap;
    }

    /**
     * 获取一天查询使用索引名称及次数信息
     *
     * @param time
     * @return
     */
    private AllHitMetric getAllIndicesMetric(Long time) {
        String date = DateTimeUtil.getDateStr(time);

        AllHitMetric allHitMetric = new AllHitMetric();

        // 获得聚合查询时传入的索引名称
        long s = System.currentTimeMillis();
        allHitMetric.addAggs(gatewayJoinEsDao.getIndicesForAggsDsl(date), false);
        LOGGER.info("get indices for aggs cost:" + (System.currentTimeMillis() - s));
        s = System.currentTimeMillis();

        // 获得查询实际命中索引名称
        long start = time;
        long end = time + _ONE_DAY;
        while (start <= end) {
            long e = start + 3 * _ONE_MINUTE;
            allHitMetric.addAggs(gatewayJoinEsDao.getIndexForNormalDsl(start, e, date), true);
            start = e;

            LOGGER.info("get index for normal cost:" + (System.currentTimeMillis() - s));
            s = System.currentTimeMillis();
        }

        return allHitMetric;
    }
}
