package com.didichuxing.datachannel.arius.admin.metadata.job.index;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.index.IndexCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.util.BatchProcessor;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
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
    @Value("${es.update.cluster.name}")
    private String            metadataClusterName;

    @Autowired
    private ClusterPhyService clusterPhyService;

    @Autowired
    private ESIndexService    esIndexService;

    @Autowired
    private IndexCatESDAO     indexCatESDAO;

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
                        .processor(this::getCatIndexInfoFromEs)
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

    private List<IndexCatCellPO> getCatIndexInfoFromEs(List<String> clusterNameList) {
        List<IndexCatCellPO> catIndexCellList = Lists.newArrayList();
        for (String clusterName : clusterNameList) {
            long timeMillis                      = System.currentTimeMillis();
            List<CatIndexResult> catIndexResults = esIndexService.syncCatIndex(clusterName, 2);
            List<IndexCatCellPO> indexCatCellPOS = buildIndexCatCellPOS(catIndexResults, clusterName, timeMillis);
            catIndexCellList.addAll(indexCatCellPOS);
        }

        return catIndexCellList;
    }

    private List<IndexCatCellPO> buildIndexCatCellPOS(List<CatIndexResult> catIndexResults, String clusterName, long timeMillis) {
        List<IndexCatCellPO> IndexCatCellPOList = Lists.newArrayList();
        for (CatIndexResult catIndexResult : catIndexResults) {
            IndexCatCellPO indexCatCellPO = new IndexCatCellPO();
            if (!AriusObjUtils.isBlack(catIndexResult.getStoreSize())) {
                indexCatCellPO.setStoreSize(SizeUtil.getUnitSize(catIndexResult.getStoreSize()));
            }
            if (!AriusObjUtils.isBlack(catIndexResult.getPriStoreSize())) {
                indexCatCellPO.setPriStoreSize(SizeUtil.getUnitSize(catIndexResult.getPriStoreSize()));
            }
            if (!AriusObjUtils.isBlack(catIndexResult.getDocsCount())) {
                indexCatCellPO.setDocsCount(Long.valueOf(catIndexResult.getDocsCount()));
            }
            if (!AriusObjUtils.isBlack(catIndexResult.getDocsDeleted())) {
                indexCatCellPO.setDocsDeleted(Long.valueOf(catIndexResult.getDocsDeleted()));
            }
            if (!AriusObjUtils.isBlack(catIndexResult.getRep())) {
                indexCatCellPO.setRep(Long.valueOf(catIndexResult.getRep()));
            }
            if (!AriusObjUtils.isBlack(catIndexResult.getPri())) {
                indexCatCellPO.setPri(Long.valueOf(catIndexResult.getPri()));
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
            indexCatCellPO.setTimestamp(timeMillis);
            IndexCatCellPOList.add(indexCatCellPO);
        }

        return IndexCatCellPOList;
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
