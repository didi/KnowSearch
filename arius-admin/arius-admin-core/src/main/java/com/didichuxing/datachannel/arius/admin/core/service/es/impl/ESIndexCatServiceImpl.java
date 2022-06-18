package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.getShards2NodeInfoRequestContent;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
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
    private IndexCatESDAO indexCatESDAO;

    @Override
    public Tuple<Long, List<IndexCatCell>> syncGetCatIndexInfo(String cluster, String index, String health, Integer appId,
                                                               Long from, Long size, String sortTerm, Boolean orderByDesc) {
        Tuple<Long, List<IndexCatCellPO>> hitTotal2catIndexInfoTuplePO = indexCatESDAO.getCatIndexInfo(cluster, index,
            health, appId, from, size, sortTerm, orderByDesc);
        if (null == hitTotal2catIndexInfoTuplePO) {
           return null;
        }

        Tuple<Long, List<IndexCatCell>> hitTotal2catIndexInfoTuple = new Tuple<>();
        hitTotal2catIndexInfoTuple.setV1(hitTotal2catIndexInfoTuplePO.getV1());
        hitTotal2catIndexInfoTuple.setV2(buildIndexCatCell(hitTotal2catIndexInfoTuplePO.getV2()));
        return hitTotal2catIndexInfoTuple;
    }

    @Override
    public int syncUpdateCatIndexDeleteFlag(String cluster, List<String> indexNameList, int retryCount) {
        if (CollectionUtils.isEmpty(indexNameList)) {
            return 0;
        }

        BatchProcessor.BatchProcessResult<String, Boolean> result = new BatchProcessor<String, Boolean>()
                .batchList(indexNameList)
                .batchSize(10)
                .processor(items -> indexCatESDAO.batchUpdateCatIndexDeleteFlag(cluster, items, retryCount))
                .succChecker(succ -> succ)
                .process();
        
        if (!result.isSucc()) {
            LOGGER.warn("class=ESIndexCatServiceImpl||method=syncUpdateCatIndexDeleteFlag||cluster={}||indexNameList={}||result={}", cluster, indexNameList,
                    result);
        }

        return indexNameList.size() - result.getFailAndErrorCount();
    }

    @Override
    public int syncUpdateCatIndexStatus(String cluster, List<String> indexNameList, String status, int retryCount) {
        if (CollectionUtils.isEmpty(indexNameList)) {
            return 0;
        }

        BatchProcessor.BatchProcessResult<String, Boolean> result = new BatchProcessor<String, Boolean>()
                .batchList(indexNameList)
                .batchSize(10)
                .processor(items -> indexCatESDAO.batchUpdateCatIndexStatus(cluster, items, status, retryCount))
                .succChecker(succ -> succ)
                .process();

        if (!result.isSucc()) {
            LOGGER.warn("class=ESIndexCatServiceImpl||method=syncUpdateCatIndexStatus||cluster={}||indexNameList={}||result={}", cluster, indexNameList,
                    result);
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

    private List<IndexCatCell> buildIndexCatCell(List<IndexCatCellPO> indexCatCellPOList) {
        List<IndexCatCell> indexCatCellList = Lists.newArrayList();
        for (IndexCatCellPO indexCatCellPO : indexCatCellPOList) {
            IndexCatCell indexCatCell = ConvertUtil.obj2Obj(indexCatCellPO,IndexCatCell.class);
            indexCatCell.setKey(indexCatCellPO.getKey());
            indexCatCell.setClusterPhy(indexCatCellPO.getCluster());
            indexCatCell.setIndex(indexCatCellPO.getIndex());
            indexCatCell.setStoreSize(SizeUtil.getUnitSizeAndFormat(indexCatCellPO.getStoreSize() ,2));
            indexCatCell.setPriStoreSize(SizeUtil.getUnitSizeAndFormat(indexCatCellPO.getPriStoreSize(), 2));
            indexCatCell.setDocsCount(String.valueOf(indexCatCellPO.getDocsCount()));
            indexCatCell.setDocsDeleted(String.valueOf(indexCatCellPO.getDocsDeleted()));
            indexCatCell.setHealth(indexCatCellPO.getHealth());
            indexCatCell.setStatus(indexCatCellPO.getStatus());
            indexCatCell.setPri(String.valueOf(indexCatCellPO.getPri()));
            indexCatCell.setRep(String.valueOf(indexCatCellPO.getRep()));

            indexCatCellList.add(indexCatCell);
        }

        return indexCatCellList;
    }
}
