package com.didichuxing.datachannel.arius.admin.metadata.job.index;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.index.IndexCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index.IndexCatESDAO;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

/**
 * @author lyn
 * @date 2021/09/30
 **/
@Component
public class IndexCatInfoCollector extends AbstractMetaDataJob {

    private static final Integer RETRY_TIMES = 3;
    @Autowired
    private ClusterPhyService clusterPhyService;

    @Autowired
    private ESIndexService    esIndexService;

    @Autowired
    private IndexCatESDAO     indexCatESDAO;

    @Autowired
    private ESClusterService esClusterService;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Autowired
    private ClusterRegionService clusterRegionService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private ClusterLogicService clusterLogicService;

    //key: cluster@indexName  value: indexName
    private Cache<String, Object> notCollectorIndexNameCache = CacheBuilder.newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES).maximumSize(10000).build();

    @Override
    public Object handleJobTask(String params) {
        List<IndexCatCellPO> indexCatInfoList     =   Lists.newArrayList();
        List<ClusterPhy>     clusterPhyList       =   clusterPhyService.listAllClusters();
        List<String>         clusterPhyNameList   =   clusterPhyList.stream().map(ClusterPhy::getCluster).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(clusterPhyNameList)) {
            return JOB_SUCCESS;
        }

        long currentTimeMillis = System.currentTimeMillis();
        BatchProcessor.BatchProcessResult<String, List<IndexCatCellPO>> batchResult
                        = new BatchProcessor<String, List<IndexCatCellPO>>()
                        .batchList(clusterPhyNameList)
                        .batchSize(5)
                        .processor(this::getIndexInfoFromEs)
                        .process();
        LOGGER.info("class=IndexCatInfoCollector||method=handleJobTask||timeOut={}", System.currentTimeMillis() - currentTimeMillis);

        if (!batchResult.isSucc() && (batchResult.getErrorMap().size() > 0)) {
            LOGGER.warn(
                "class=IndexCatInfoCollector||method=handleJobTask||clusterList={}||errMsg=batch result error:{}",
                ListUtils.strList2String(clusterPhyNameList), batchResult.getErrorMap());
        }
        
        List<List<IndexCatCellPO>> resultList = batchResult.getResultList();
        for (List<IndexCatCellPO> indexCatCellPOS : resultList) {
            indexCatInfoList.addAll(indexCatCellPOS);
        }

        //延迟2s, 等待 updateNotCollectorIndexNames 方法更新删除标识
        sleep(2000L);
        //移除已删除索引, 不采集
        List<IndexCatCellPO> finalSaveIndexCatList = indexCatInfoList.stream()
            .filter(this::filterNotCollectorIndexCat).collect(Collectors.toList());
        indexCatESDAO.batchInsert(finalSaveIndexCatList);
        return JOB_SUCCESS;
    }

    public void updateNotCollectorIndexNames(String cluster, List<String> notCollectorIndexNameList){
        for (String indexName : notCollectorIndexNameList) {
            notCollectorIndexNameCache.put(cluster + "@" + indexName, indexName);
        }
    }


    private List<IndexCatCellPO> getIndexInfoFromEs(List<String> clusterNameList) {
        List<IndexCatCellPO> catIndexCellList = Lists.newArrayList();
        List<ClusterLogic> logicClusterList = clusterLogicService.listAllClusterLogics();
        Map<Long, String> logicClusterId2NameMap = ConvertUtil.list2Map(logicClusterList, ClusterLogic::getId, ClusterLogic::getName);
        for (String clusterName : clusterNameList) {
            long timeMillis                      = System.currentTimeMillis();
            List<CatIndexResult> catIndexResults = esIndexService.syncCatIndex(clusterName, RETRY_TIMES);
            if (CollectionUtils.isEmpty(catIndexResults)) {
                LOGGER.warn("class=IndexCatInfoCollector||method=getIndexInfoFromEs||clusterName={}||index empty",
                    clusterName);
                continue;
            }

            List<ClusterRegion> clusterRegionList = clusterRegionService.listPhyClusterRegions(clusterName);
            if (CollectionUtils.isEmpty(clusterRegionList)) {
                LOGGER.warn("class=IndexCatInfoCollector||method=getIndexCatInfo||clusterName={}||cluster region empty",
                    clusterName);
                continue;
            }
            List<Long> logicClusterIdList = new ArrayList<>();
            for (ClusterRegion clusterRegion : clusterRegionList) {
                logicClusterIdList.addAll(ListUtils.string2LongList(clusterRegion.getLogicClusterIds()));
            }

            // 2. 获取逻辑集群下所有逻辑模板
            List<IndexTemplate> indexTemplateList = indexTemplateService.listByResourceIds(logicClusterIdList);
            if (CollectionUtils.isEmpty(indexTemplateList)) {
                LOGGER.warn("class=IndexCatInfoCollector||method=getIndexCatInfo||clusterName={}||index template empty",
                    clusterName);
                continue;
            }

            List<IndexCatCellPO> indexCatCellPOS = buildIndexCatCellPOS(catIndexResults, logicClusterId2NameMap,
                indexTemplateList, clusterName, timeMillis);
            catIndexCellList.addAll(indexCatCellPOS);
        }

        return catIndexCellList;
    }
    
    private List<IndexCatCellPO> buildIndexCatCellPOS(List<CatIndexResult> catIndexResults,Map<Long, String> logicClusterId2NameMap,List<IndexTemplate> indexTemplateList, String clusterName, long timeMillis) {
        List<IndexCatCellPO> indexCatCellList = Lists.newArrayList();
        for (CatIndexResult catIndexResult : catIndexResults) {
            IndexCatCellPO indexCatCellPO = new IndexCatCellPO();
            if (!AriusObjUtils.isBlack(catIndexResult.getStoreSize())) {
                indexCatCellPO.setStoreSize(SizeUtil.getUnitSize(catIndexResult.getStoreSize()));
            }
            if (!AriusObjUtils.isBlack(catIndexResult.getPriStoreSize())) {
                indexCatCellPO.setPriStoreSize(SizeUtil.getUnitSize(catIndexResult.getPriStoreSize()));
            }
            if (!AriusObjUtils.isBlack(catIndexResult.getDocsCount())) {
                indexCatCellPO.setDocsCount(Long.parseLong(catIndexResult.getDocsCount()));
            }
            if (!AriusObjUtils.isBlack(catIndexResult.getDocsDeleted())) {
                indexCatCellPO.setDocsDeleted(Long.parseLong(catIndexResult.getDocsDeleted()));
            }
            if (!AriusObjUtils.isBlack(catIndexResult.getRep())) {
                indexCatCellPO.setRep(Long.parseLong(catIndexResult.getRep()));
            }
            if (!AriusObjUtils.isBlack(catIndexResult.getPri())) {
                indexCatCellPO.setPri(Long.parseLong(catIndexResult.getPri()));
            }
            if (!AriusObjUtils.isBlack(catIndexResult.getStatus())) {
                indexCatCellPO.setStatus(catIndexResult.getStatus());
            }
            if (!AriusObjUtils.isBlack(catIndexResult.getHealth())) {
                indexCatCellPO.setHealth(catIndexResult.getHealth());
            }
            if (!AriusObjUtils.isBlack(catIndexResult.getIndex())) {
                indexCatCellPO.setIndex(catIndexResult.getIndex());
            }

            indexCatCellPO.setCluster(clusterName);
            indexCatCellPO.setClusterPhy(clusterName);
            indexCatCellPO.setTimestamp(timeMillis);
            indexCatCellPO.setClusterLogic("");
            if (CollectionUtils.isNotEmpty(indexTemplateList)) {
                indexTemplateList.stream()
                    .filter(indexTemplate -> IndexNameUtils.indexExpMatch(catIndexResult.getIndex(),
                        indexTemplate.getExpression()))
                    .map(IndexTemplate::getResourceId).map(logicClusterId2NameMap::get).filter(StringUtils::isNotBlank)
                    .findFirst().ifPresent(indexCatCellPO::setClusterLogic);
            }
            
            indexCatCellList.add(indexCatCellPO);
        }

        return indexCatCellList;
    }

    private boolean filterNotCollectorIndexCat(IndexCatCellPO indexCatCellPO) {
        return notCollectorIndexNameCache.getIfPresent(indexCatCellPO.getKey()) == null;
    }

    private void sleep(Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
