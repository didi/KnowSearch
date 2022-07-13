package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexToNodeStats;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyNodeMetricsEnum.CPU_USAGE_PERCENT;

@Component
public class AriusStatsIndexNodeInfoESDAO extends BaseAriusStatsESDAO {

    @PostConstruct
    public void init() {
        super.indexName = dataCentreUtil.getAriusStatsIndexNodeInfo();

        BaseAriusStatsESDAO.register(AriusStatsEnum.INDEX_NODE_INFO, this);
    }

    /**
     * 获取一段时间内索引的index_node统计信息
     * @param startDate
     * @param endDate
     * @return
     */
    public List<ESIndexToNodeStats> getIndexToNodeStats(String template, String cluster, Long startDate, Long endDate) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startDate, endDate);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_INDEX_NODE_STATS_BY_TIME_RANGE, SCROLL_SIZE,
            startDate, endDate, template, cluster);

        List<ESIndexToNodeStats> esIndexToNodeStats = Lists.newLinkedList();
        gatewayClient.queryWithScroll(realIndexName, TYPE, dsl, SCROLL_SIZE, null, ESIndexToNodeStats.class,
            resultList -> {
                if (resultList != null) {
                    esIndexToNodeStats.addAll(resultList);
                }
            });

        return esIndexToNodeStats;
    }

    /**
     * 获取一段时间内索引的index_node统计信息
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public List<ESIndexToNodeStats> getIndexToNodeStats(Long templateId, Long startDate, Long endDate) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startDate, endDate);
        String dsl = dslLoaderUtil.getFormatDslByFileName(
            DslsConstant.GET_INDEX_NODE_STATS_BY_TIME_RANGE_AND_TEMPALTEID, SCROLL_SIZE, startDate, endDate,
            templateId);

        List<ESIndexToNodeStats> esIndexToNodeStats = Lists.newLinkedList();
        gatewayClient.queryWithScroll(realIndexName, TYPE, dsl, SCROLL_SIZE, null, ESIndexToNodeStats.class,
            resultList -> {
                if (resultList != null) {
                    esIndexToNodeStats.addAll(resultList);
                }
            });

        return esIndexToNodeStats;
    }

    /**
     * 根据模板名称和集群获取，模板所在节点【startTime， endTime】内的平均cpu
     * @param templateId
     * @param startDate 毫秒
     * @param endDate   毫秒
     * @return
     */
    public List<Tuple<String/*node:port*/, Double/*cpu avg*/>> getTemplateNodeCpu(Long templateId, Long startDate,
                                                                                  Long endDate) {
        List<ESIndexToNodeStats> esIndexStats = getIndexToNodeStats(templateId, startDate, endDate);

        Map<String, List<Double>> templateCpuMap = Maps.newHashMap();

        for (ESIndexToNodeStats indexNode : esIndexStats) {
            String node = indexNode.getNode() + ":" + indexNode.getPort();
            Double cpu = Double.valueOf(indexNode.getMetrics().get(CPU_USAGE_PERCENT.getType()));

            List<Double> cpuList = templateCpuMap.get(node);
            if (CollectionUtils.isEmpty(cpuList)) {
                List<Double> doubleList = Lists.newArrayList();
                doubleList.add(cpu);
                templateCpuMap.put(node, doubleList);
            } else {
                cpuList.add(cpu);
            }
        }

        List<Tuple<String, Double>> ret = Lists.newArrayList();
        for (Map.Entry<String, List<Double>> entry : templateCpuMap.entrySet()) {
            String node = entry.getKey();
            List<Double> doubleList = templateCpuMap.get(node);
            if (CollectionUtils.isNotEmpty(doubleList)) {
                final Double[] totalCpu = { 0.0d };
                doubleList.forEach(d -> totalCpu[0] += d);

                ret.add(new Tuple<>(node, totalCpu[0] / doubleList.size()));
            }
        }

        return ret;
    }
}
