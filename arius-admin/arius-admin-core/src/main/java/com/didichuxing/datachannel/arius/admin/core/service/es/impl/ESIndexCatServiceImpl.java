package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.getShards2NodeInfoRequestContent;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.IndexShardInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.index.IndexCatCellPO;
import com.didichuxing.datachannel.arius.admin.common.util.BatchProcessor;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index.IndexCatESDAO;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * Created by linyunan on 2021-10-14
 */
@Service
public class ESIndexCatServiceImpl implements ESIndexCatService {

    private static final ILog LOGGER = LogFactory.getLog(ESIndexCatService.class);
    @Autowired
    private IndexCatESDAO     indexCatESDAO;

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
    public List<IndexShardInfo> syncGetIndexShardInfo(String clusterPhyName, String indexName) {
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
        boolean succ = indexCatESDAO.batchInsert(ConvertUtil.list2List(params, IndexCatCellPO.class), retryCount);
        if (succ) {
            List<String> clusterList = params.stream().map(IndexCatCellDTO::getCluster).distinct().collect(Collectors.toList());
            List<String> indexList   = params.stream().map(IndexCatCellDTO::getIndex).distinct().collect(Collectors.toList());
            LOGGER.error("class=ESIndexCatServiceImpl||method=syncInsertCatIndex||cluster={}||indexNameList={}||errMsg=failed to batchInsert",
                    clusterList, indexList);
        }

        return succ;
    }

    @Override
    public List<IndexCatCell> syncGetHasProjectIdButNotTemplateIdCatIndexList() {
        try {
            return indexCatESDAO.getHasProjectIdButNotTemplateIdCatIndexList();
        } catch (Exception e) {
            LOGGER.error("class=ESIndexCatServiceImpl||method=syncGetHasProjectIdButNotTemplateIdCatIndexList||" +
                    "errMsg=failed to get syncGetHasProjectIdButNotTemplateIdCatIndexList", e);
        }
        return Lists.newArrayList();
    }


    /*************************************************private*******************************************************/
}