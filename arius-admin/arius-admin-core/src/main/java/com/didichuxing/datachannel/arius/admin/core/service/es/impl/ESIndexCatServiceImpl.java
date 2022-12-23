package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.getShards2NodeInfoRequestContent;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.IndexShardInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.index.IndexCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.BatchProcessor;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index.IndexCatESDAO;
import com.didiglobal.knowframework.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * Created by linyunan on 2021-10-14
 */
@Service
public class ESIndexCatServiceImpl implements ESIndexCatService {

    private static final ILog LOGGER = LogFactory.getLog(ESIndexCatService.class);
    @Autowired
    private IndexCatESDAO indexCatESDAO;
   

    @Override
    public Tuple<Long, List<IndexCatCell>> syncGetCatIndexInfo(String cluster, String index, String health,
                                                               String status, Integer projectId, Long from, Long size,
                                                               String sortTerm, Boolean orderByDesc) {
        Tuple<Long, List<IndexCatCellPO>> hitTotal2catIndexInfoTuplePO = indexCatESDAO.getCatIndexInfo(cluster, index,
            health, status, projectId, from, size, sortTerm, orderByDesc);
        if (null == hitTotal2catIndexInfoTuplePO) {
            return null;
        }

        Tuple<Long, List<IndexCatCell>> hitTotal2catIndexInfoTuple = new Tuple<>();
        hitTotal2catIndexInfoTuple.setV1(hitTotal2catIndexInfoTuplePO.getV1());
        hitTotal2catIndexInfoTuple.setV2(ConvertUtil.list2List(hitTotal2catIndexInfoTuplePO.getV2(), IndexCatCell.class));
        return hitTotal2catIndexInfoTuple;
    }

    @Override
    public int syncUpdateCatIndexDeleteFlag(String cluster, List<String> indexNameList, int retryCount) {
        if (CollectionUtils.isEmpty(indexNameList)) {
            return 0;
        }

        BatchProcessor.BatchProcessResult<String, Boolean> result = new BatchProcessor<String, Boolean>()
            .batchList(indexNameList).batchSize(10)
            .processor(items -> indexCatESDAO.batchUpdateCatIndexDeleteFlag(cluster, items, retryCount))
            .succChecker(succ -> succ).process();

        if (!result.isSucc()) {
            LOGGER.warn(
                "class=ESIndexCatServiceImpl||method=syncUpdateCatIndexDeleteFlag||cluster={}||indexNameList={}||result={}",
                cluster, indexNameList, result);
        }

        return indexNameList.size() - result.getFailAndErrorCount();
    }

    @Override
    public int syncUpdateCatIndexStatus(String cluster, List<String> indexNameList, String status, int retryCount) {
        if (CollectionUtils.isEmpty(indexNameList)) {
            return 0;
        }

        BatchProcessor.BatchProcessResult<String, Boolean> result = new BatchProcessor<String, Boolean>()
            .batchList(indexNameList).batchSize(10)
            .processor(items -> indexCatESDAO.batchUpdateCatIndexStatus(cluster, items, status, retryCount))
            .succChecker(succ -> succ).process();

        if (!result.isSucc()) {
            LOGGER.warn(
                "class=ESIndexCatServiceImpl||method=syncUpdateCatIndexStatus||cluster={}||indexNameList={}||result={}",
                cluster, indexNameList, result);
        }

        return indexNameList.size() - result.getFailAndErrorCount();
    }

    @Override
    public List<IndexShardInfo> syncGetIndexShardInfo(String clusterPhyName, String indexName) throws ESOperateException {
        String shards2NodeInfoRequestContent = getShards2NodeInfoRequestContent(indexName, "20s");
        DirectResponse shardNodeResponse = indexCatESDAO.getDirectResponse(clusterPhyName, "Get",
            shards2NodeInfoRequestContent);

        if (shardNodeResponse.getRestStatus() == RestStatus.OK
            && StringUtils.isNoneBlank(shardNodeResponse.getResponseContent())) {
            return ConvertUtil.str2ObjArrayByJson(shardNodeResponse.getResponseContent(), IndexShardInfo.class);
        }

        return Lists.newArrayList();
    }

    @Override
    public Boolean syncInsertCatIndex(List<IndexCatCellDTO> params, int retryCount) {
        BatchProcessor.BatchProcessResult<IndexCatCellDTO, Boolean> result = new BatchProcessor<IndexCatCellDTO, Boolean>()
                .batchList(params).batchSize(5000)
                .processor(items -> indexCatESDAO.batchInsert(ConvertUtil.list2List(params, IndexCatCellPO.class), retryCount))
                .succChecker(succ -> succ).process();

        if (!result.isSucc()) {
            List<String> clusterList = params.stream().map(IndexCatCellDTO::getCluster).distinct().collect(Collectors.toList());
            List<String> indexList   = params.stream().map(IndexCatCellDTO::getIndex).distinct().collect(Collectors.toList());
            LOGGER.error("class=ESIndexCatServiceImpl||method=syncInsertCatIndex||cluster={}||indexNameList={}||errMsg=failed to batchInsert, batch total count = {}, batch failed count={}",
                    ListUtils.strList2String(clusterList), ListUtils.strList2String(indexList),
                    params.size(), result.getFailAndErrorCount());
        }

        return result.isSucc();
    }
    
    @Override
    public Boolean syncUpsertCatIndex(List<IndexCatCellDTO> params, int retryCount) {
        BatchProcessor.BatchProcessResult<IndexCatCellDTO, Boolean> result = new BatchProcessor<IndexCatCellDTO, Boolean>().batchList(
                        params).batchSize(5000).processor(
                        items -> indexCatESDAO.batchUpsert(ConvertUtil.list2List(params, IndexCatCellPO.class), retryCount))
                .succChecker(succ -> succ).process();
        
        if (!result.isSucc()) {
            List<String> clusterList = params.stream().map(IndexCatCellDTO::getCluster).distinct()
                    .collect(Collectors.toList());
            List<String> indexList = params.stream().map(IndexCatCellDTO::getIndex).distinct()
                    .collect(Collectors.toList());
            LOGGER.error(
                    "class=ESIndexCatServiceImpl||method=syncUpsertCatIndex||cluster={}||indexNameList={}||errMsg=failed to batchInsert, batch total count = {}, batch failed count={}",
                    ListUtils.strList2String(clusterList), ListUtils.strList2String(indexList), params.size(),
                    result.getFailAndErrorCount());
        }
        
        return result.isSucc();
    }

    @Override
    public List<IndexCatCell> syncGetPlatformCreateCatIndexList(Integer searchSize) {
        try {
            return indexCatESDAO.getPlatformCreateCatIndexList(searchSize);
        } catch (Exception e) {
            LOGGER.error("class=ESIndexCatServiceImpl||method=syncGetHasProjectIdButNotTemplateIdCatIndexList||" +
                    "errMsg=failed to get syncGetHasProjectIdButNotTemplateIdCatIndexList", e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<IndexCatCellDTO> syncGetIndexByCluster(String clusterLogicName, Integer projectId) {
        Tuple<Long, List<IndexCatCellPO>> totalHitAndIndexCatCellListTuple = indexCatESDAO.getIndexListByTerms(clusterLogicName,projectId);
        if (totalHitAndIndexCatCellListTuple == null){
            return new ArrayList<>();
        }
        return ConvertUtil.list2List(totalHitAndIndexCatCellListTuple.v2(),IndexCatCellDTO.class);
    }

    /**
     * @param cluster
     * @param indexList
     * @return
     */
    @Override
    public Result<List<IndexCatCellDTO>> syncGetSegmentsIndexList(String cluster, Collection<String> indexList) {
        BatchProcessor.BatchProcessResult<String, List<IndexCatCellDTO>> result = new BatchProcessor<String, List<IndexCatCellDTO>>().batchList(
                        indexList)
            
                .batchSize(50).processor(items -> indexCatESDAO.syncGetSegmentsIndexList(cluster, items))
                .succChecker(CollectionUtils::isNotEmpty).process();
        if (!result.isSucc() && MapUtils.isNotEmpty(result.getErrorMap()) && CollectionUtils.isNotEmpty(
                result.getErrorMap().values())) {
            LOGGER.error("class={}||method=syncGetSegmentsIndexList||errMsg=failed to get syncGetSegmentsIndexList",
                    result.getErrorMap().values().stream().findFirst().get());
        }
        
        
        List<IndexCatCellDTO> indexCatCellDTOList = result.getResultList().stream().filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream).collect(Collectors.toList());
        return Result.buildSucc(indexCatCellDTOList);
    }
    
    /**
     * @param projectId
     * @return
     */
    @Override
    public List<String> syncGetIndexListByProjectId(Integer projectId, String clusterLogic) {
        return indexCatESDAO.syncGetIndexListByProjectIdAndClusterLogic(projectId,
                clusterLogic);
    }
    
    @Override
    public List<String> syncGetIndexListByProjectIdAndFuzzyIndexAndClusterLogic(Integer projectId, String clusterLogicName,
                                                                                String index) {
       return indexCatESDAO.syncGetIndexListByProjectIdAndFuzzyIndexAndClusterLogic(projectId,
                clusterLogicName,index);
    }
    
    @Override
    public List<String> syncGetIndexListByProjectIdAndFuzzyIndexAndClusterPhy(String clusterPhyName,
                                                                              String index) {
         return indexCatESDAO.syncGetIndexListByProjectIdAndFuzzyIndexAndClusterPhy( clusterPhyName,index);
    }

    /**
     * @param clusterPhyList
     * @return
     */
    @Override
    public Map<String, Integer> syncGetByClusterPhyList(List<String> clusterPhyList) {
        return indexCatESDAO.syncGetByClusterPhyList(clusterPhyList);
    }
    
    /**
     * @param clusterPhy
     * @param index
     */
    @Override
    public IndexCatCell syncGetCatIndexInfoById(String clusterPhy, String index) {
        return indexCatESDAO.syncGetCatIndexInfoById(clusterPhy,index);
    }
    /*************************************************private*******************************************************/
}