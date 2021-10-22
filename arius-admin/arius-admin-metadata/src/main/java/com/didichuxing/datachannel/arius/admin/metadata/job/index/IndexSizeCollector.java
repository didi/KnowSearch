package com.didichuxing.datachannel.arius.admin.metadata.job.index;

import com.didichuxing.datachannel.arius.admin.common.bean.po.index.IndexSizePO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index.IndexSizeESDAO;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.stats.IndexNodes;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Callable;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;
import static com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils.indexExpMatch;

/**
 * @author: D10865
 * @description: 索引大小统计
 * @date: Create on 2019/1/24 上午11:21
 * @modified By D10865
 */
@Component
public class IndexSizeCollector extends AbstractMetaDataJob {

    /**
     * 操作索引大小类
     */
    @Autowired
    private IndexSizeESDAO indexSizeEsDao;

    @Autowired
    private TemplatePhyService templatePhyService;

    @Autowired
    private ESClusterPhyService phyClusterService;

    @Autowired
    private ESIndexService          esIndexService;

    private final static FutureUtil futureUtil = FutureUtil.init("QueryStatisticsCollector");

    /**
     * 处理采集任务
     *
     * @return
     */
    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=IndexSizeCollector||method=handleJobTask||params={}", params);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 获取所有查询模板
        Map<String, List<IndexTemplatePhyWithLogic>> indexTemplateMap =  new HashMap<>();

        List<IndexTemplatePhyWithLogic> indexTemplateList = templatePhyService.listTemplateWithLogic();

        for (IndexTemplatePhyWithLogic indexTemplate : indexTemplateList) {
            indexTemplateMap.computeIfAbsent(indexTemplate.getName(), templateName -> Lists.newArrayList()).add(indexTemplate);
        }

        List<ESClusterPhy> esClusterPhies = phyClusterService.listAllClusters();
        for(ESClusterPhy esClusterPhy : esClusterPhies){
            futureUtil.callableTask((Callable<List<IndexSizePO>>) () -> {
                Map<String, IndexNodes> indexStatsMap = esIndexService.getIndexNodes(esClusterPhy.getCluster(), null);
                return parseCatIndicesResult(esClusterPhy.getCluster(), indexStatsMap, indexTemplateMap);
            } );
        }

        List<IndexSizePO> indexSizePOS = futureUtil.waitResult();

        // 当前时间戳转换为当前日期，作为文档写入时间
        String sinkDate = DateTimeUtil.getDateStr(System.currentTimeMillis());
        for(IndexSizePO item : indexSizePOS){
            // 如果时间字段不符合
            if (!item.isVaildDate()) {
                LOGGER.error("class=IndexSizeCollector||method=handleJobTask||errMsg=invalid date {}, {}", item.getDate(), item);
                // iterator.remove();
            }
            // 设置写入时间
            item.setSinkDate(sinkDate);
        }

        boolean operatorResult = indexSizeEsDao.batchInsert(indexSizePOS);

        LOGGER.info("class=IndexSizeCollector||method=handleJobTask||msg=operatorResult {}, IndexSizePOList size {}, cost {}",
                operatorResult, indexSizePOS.size(), stopWatch.stop().toString());

        return JOB_SUCCESS;
    }

    /**
     * 解析cat indices结果
     *
     * @param clusterName
     * @param indexStatsMap
     * @return
     */
    public List<IndexSizePO> parseCatIndicesResult(String clusterName, Map<String, IndexNodes> indexStatsMap,
                                                   Map<String, List<IndexTemplatePhyWithLogic>> indexTemplateMap) {
        List<IndexSizePO> IndexSizePOList = Lists.newArrayList();

        if (indexStatsMap == null) {
            return IndexSizePOList;
        }

        LOGGER.info("class=IndexSizeCollector||method=parseCatIndicesResult||msg=clusterName -> {}, has {} index", clusterName, indexStatsMap.size());

        // 相同索引名称前缀的不同版本
        Map<String, List<IndexSizePO>> indexNameVersionMap = Maps.newHashMap();
        IndexSizePO IndexSizePO = null;
        String indexName = "";
        int versionPos = 0;
        IndexNodes indexNodes = null;

        for (Map.Entry<String, IndexNodes> entry : indexStatsMap.entrySet()) {

            indexName = entry.getKey();
            indexNodes = entry.getValue();

            // 忽略.kibana ..kibana_sec_test 和.marvel 开头的索引
            if ("_all".equals(indexName) || indexName.startsWith(".kibana") || indexName.startsWith(".marvel") || indexName.startsWith("..kibana")) {
                continue;
            }

            IndexSizePO = new IndexSizePO();

            IndexSizePO.setIndexName(indexName).setDocsCount(indexNodes.getPrimaries().getDocs().getCount()).setClusterName(clusterName);
            // 主shard大小，单位字节
            IndexSizePO.setPrimaryStoreSize(indexNodes.getPrimaries().getStore().getSizeInBytes());
            IndexSizePO.setUnitStoreSize(SizeUtil.getUnitSize(indexNodes.getPrimaries().getStore().getSizeInBytes()));
            // 索引大小，包括副本(如果有的话)，单位是字节
            IndexSizePO.setTotalStoreSize(indexNodes.getTotal().getStore().getSizeInBytes());
            IndexSizePO.setUnitTotalStoreSize(SizeUtil.getUnitSize(indexNodes.getTotal().getStore().getSizeInBytes()));

            String templateName = "";
            Set<String> matchIndexTemplateNameSet = matchIndexTemplateWithIndexName(indexName, indexTemplateMap);
            if (matchIndexTemplateNameSet.isEmpty()) {
                LOGGER.error("class=IndexSizeCollector||method=parseCatIndicesResult||errMsg=clusterName -> {}, indexName -> [{}] not match any indexTemplate or alias", clusterName, indexName);
            } else if (matchIndexTemplateNameSet.size() > 1) {
                // 匹配到多个索引模板时取索引模板字符串最长的
                templateName = Lists.newArrayList(matchIndexTemplateNameSet).get(0);
                LOGGER.error("class=IndexSizeCollector||method=parseCatIndicesResult||errMsg=clusterName -> {}, indexName -> [{}] match {} indexTemplate or alias, {}, match first {}",
                        clusterName, indexName, matchIndexTemplateNameSet.size(), matchIndexTemplateNameSet, templateName);
            } else {
                templateName = Lists.newArrayList(matchIndexTemplateNameSet).get(0);
            }

            if (StringUtils.isNotBlank(templateName)) {
                List<IndexTemplatePhyWithLogic> templates = indexTemplateMap.get(templateName);
                if (CollectionUtils.isEmpty(templates)) {
                    continue;
                }

                IndexTemplatePhyWithLogic templateInfoResponse = templates.get(0);

                String date = indexName.replaceAll(templateInfoResponse.getExpression().replaceAll("\\*", ""), "");
                // 如果日期中包含版本信息，需要与其他索引版本进行合并
                versionPos = date.lastIndexOf("_v");

                if (versionPos > 0) {
                    date = date.substring(0, versionPos);
                    List<IndexSizePO> IndexSizePOs = indexNameVersionMap.computeIfAbsent(templateName.concat(date), k -> new LinkedList<>());

                    IndexSizePO.setDate(DateTimeUtil.getIndexDate(indexName, date, templateInfoResponse.getLogicTemplate().getDateFormat()));
                    IndexSizePO.setTemplateName(templateName);
                    IndexSizePOs.add(IndexSizePO);

                } else {
                    IndexSizePO.setDate(DateTimeUtil.getIndexDate(indexName, date, templateInfoResponse.getLogicTemplate().getDateFormat()));
                    IndexSizePO.setTemplateName(templateName);
                    IndexSizePOList.add(IndexSizePO);
                }

            } else {
                // 没有找到索引模板以索引名称为索引模板名称
                IndexSizePO.setTemplateName(indexName);
                IndexSizePOList.add(IndexSizePO);
            }
        }


        // 存在不同版本的索引
        if (!indexNameVersionMap.isEmpty()) {
            Iterator<IndexSizePO> iterator = IndexSizePOList.iterator();
            while (iterator.hasNext()) {
                IndexSizePO item = iterator.next();
                // 如果在版本map中存在，则从原队列中移除，并加入到map中。例如 存在index_YYYY-MM-dd,index_YYYY-MM-dd_v1,index_YYYY-MM-dd_v2索引，需要把index_YYYY-MM-dd归入到index_YYYY-MM-dd这个key的list中进行合并大小
                if (indexNameVersionMap.containsKey(item.getIndexName())) {
                    indexNameVersionMap.get(item.getIndexName()).add(item);
                    iterator.remove();
                }
            }

            for (Map.Entry<String, List<IndexSizePO>> entry : indexNameVersionMap.entrySet()) {
                List<IndexSizePO> items = entry.getValue();
                IndexSizePO = new IndexSizePO();
                IndexSizePO.setDate(items.get(0).getDate());
                IndexSizePO.setTemplateName(items.get(0).getTemplateName());
                IndexSizePO.setClusterName(clusterName);
                IndexSizePO.setIndexName(entry.getKey());

                // 文档个数和大小进行合并
                Long indexDoc = 0L;
                Long storeSize = 0L;
                Long totalSize = 0L;

                for (IndexSizePO indexSize : items) {
                    indexDoc += indexSize.getDocsCount();
                    storeSize += indexSize.getPrimaryStoreSize();
                    totalSize += indexSize.getTotalStoreSize();
                }

                IndexSizePO.setDocsCount(indexDoc);
                // 主shard大小，单位字节
                IndexSizePO.setPrimaryStoreSize(storeSize);
                IndexSizePO.setUnitStoreSize(SizeUtil.getUnitSize(storeSize));
                // 索引大小，包括副本(如果有的话)，单位是字节
                IndexSizePO.setTotalStoreSize(totalSize);
                IndexSizePO.setUnitTotalStoreSize(SizeUtil.getUnitSize(totalSize));

                IndexSizePOList.add(IndexSizePO);
            }
        }

        return IndexSizePOList;
    }

    /**
     * 索引名称匹配索引模板或者别名
     *
     * @param indexName
     * @param indexTemplateMap
     * @return
     */
    private Set<String> matchIndexTemplateWithIndexName(String indexName, Map<String/*templateName*/, List<IndexTemplatePhyWithLogic>> indexTemplateMap) {

        Set<String> matchIndexTemplateNameSet = Sets.newTreeSet((o1, o2) -> {
            if (StringUtils.isBlank(o1)) {return -1;}
            if (StringUtils.isBlank(o2)) {return 1;}
            if (o1.length() == o2.length()) {return 0;}

            return o1.length() < o2.length() ? 1 : -1;
        } );

        List<IndexTemplatePhyWithLogic> indexTemplateList = null;
        for (Map.Entry<String/*templateName*/, List<IndexTemplatePhyWithLogic>> entry : indexTemplateMap.entrySet()) {

            indexTemplateList = entry.getValue();

            for (IndexTemplatePhyWithLogic indexTemplate : indexTemplateList) {
                // 从索引模板表达式集合中查找
                if (indexExpMatch(indexName, indexTemplate.getExpression())) {
                    matchIndexTemplateNameSet.add(entry.getKey());
                }

            }

        }

        return matchIndexTemplateNameSet;
    }
}
