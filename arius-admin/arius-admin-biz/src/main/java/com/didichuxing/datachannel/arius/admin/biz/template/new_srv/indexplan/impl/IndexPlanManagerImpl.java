package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.indexplan.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.indexplan.IndexPlanManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalUpgradeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.BYTE_TO_G;

/**
 * @author chengxiang, jiamin
 * @date 2022/5/11
 */
@Service
public class IndexPlanManagerImpl extends BaseTemplateSrvImpl implements IndexPlanManager {

    private final Integer singleShardMaxSize = 50;
    private final Integer singleShardRecommendSize = 30;

    /*(key, value) = (模板id, 该模版对应索引近七天某一天占用磁盘容量最大值)*/
    private final Map<Long, Long> indexMaxStoreMap = Maps.newConcurrentMap();

    @Autowired
    private ESIndexService esIndexService;

    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.INDEX_PLAN;
    }

    @Override
    public Result<Void> indexRollover(Integer logicTemplateId) {
        LOGGER.info("class=IndexPlanManagerImpl||method=indexRollover||logicTemplateId={}||msg=start indexRollover", logicTemplateId);
        if (!isTemplateSrvOpen(logicTemplateId)) {
            return Result.buildFail("指定索引模板未开启rollover能力");
        }

        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);
        if (CollectionUtils.isEmpty(templatePhyList)) {
            LOGGER.info("class=IndexPlanManagerImpl||method=indexRollover||logicTemplateId={}||msg=IndexRolloverTask no physical template", logicTemplateId);
            return Result.buildSucc();
        }

        IndexTemplate template = indexTemplateService.getLogicTemplateById(logicTemplateId);
        for (IndexTemplatePhy templatePhy : templatePhyList) {
            String phyClusterName = templatePhy.getCluster();
            // 根据索引分区规则，获取当天或当月或不分区带有版本信息的索引的名字
            String indexName = getIndexNameByDateFormat(template, templatePhy);

            // 获取indexNodes信息（该索引对应的元信息）
            IndexNodes indexNodes = getIndexNodes(indexName, phyClusterName);

            // 获取索引的主shard个数，这里不能直接从数据库获取，因为可能会被改变，所以从ES中获取
            Integer primaryShardCnt = esIndexService.syncGetIndexPrimaryShardNumber(phyClusterName, indexName);
            if(primaryShardCnt == null || indexNodes == null) {
                continue;
            }

            // 当天最高版本的索引占用磁盘的容量
            long curSizeInBytes = indexNodes.getPrimaries().getStore().getSizeInBytes();
            double curSizeInGb = curSizeInBytes * BYTE_TO_G;

            if(curSizeInGb >= primaryShardCnt * singleShardMaxSize) {
                // 如果大于（主shard个数 * 推荐的单个shard大小50G），直接升版本
                updateTemplateVersion(templatePhy);
            } else if(curSizeInGb >= primaryShardCnt * singleShardRecommendSize && TemplateUtils.isSaveByDay(template.getDateFormat())){
                // 如果大于（主shard个数 * 推荐的单个shard大小30G），并且索引模版是按天创建索引
                // 获取该索引模版对应索引近7天占用磁盘的最大值
                Long sizeInBytesMax = getMaxStoreInRecentSevenDayByTemplatePhyId(templatePhy.getId());
                // 比较两者大小，大于则升版本
                if(curSizeInBytes > sizeInBytesMax) {
                    updateTemplateVersion(templatePhy);
                }
            }
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Void> adjustShardCountByPhyClusterName(Integer logicTemplateId) {
        return Result.buildSucc();
    }

    @Override
    public void initShardRoutingAndAdjustShard(IndexTemplatePhyDTO param) {

    }

    //////////private method/////////////////////////////////////////////////////////////

    private String getIndexNameByDateFormat(IndexTemplate logiTemplate, IndexTemplatePhy phyTemplate) {
        if(TemplateUtils.isSaveByDay(logiTemplate.getDateFormat())) {
            // 按天分区则获取模版对应当天索引拼接版本信息
            return IndexNameUtils.genDailyIndexNameWithVersion(phyTemplate.getName(), 0, phyTemplate.getVersion());
        } else if(TemplateUtils.isSaveByMonth(logiTemplate.getDateFormat())) {
            // 按月分区则获取模版对应当月索引拼接版本信息
            return IndexNameUtils.genCurrentMonthlyIndexNameWithVersion(phyTemplate.getName(), phyTemplate.getVersion());
        } else {
            // 不分区则模版名拼接版本信息
            return IndexNameUtils.genIndexNameWithVersion(phyTemplate.getName(), phyTemplate.getVersion());
        }
    }

    /**
     * @param indexName 索引名字
     * @param clusterPhyName 物理集群名
     * @return IndexNodes 索引的元信息
     */
    private IndexNodes getIndexNodes(String indexName, String clusterPhyName) {
        // 获取该索引模版对应索引当天占用磁盘的大小
        Map<String, IndexNodes> indexNodeMap = esIndexService.syncGetIndexNodes(clusterPhyName, indexName);
        if(indexNodeMap == null || indexNodeMap.size() == 0) {
            LOGGER.warn("class=IndexPlanManagerImpl||method=getIndexNodes||index={}||errMsg={}",
                    indexName, "获取不到该索引的IndexNodes（元数据）信息，索引也许不存在");
            return null;
        }
        return indexNodeMap.get(indexName);
    }

    private void updateTemplateVersion(IndexTemplatePhy templatePhy) {
        TemplatePhysicalUpgradeDTO param = new TemplatePhysicalUpgradeDTO();
        param.setVersion(templatePhy.getVersion() + 1);
        param.setRack(templatePhy.getRack());
        param.setPhysicalId(templatePhy.getId());
        param.setShard(templatePhy.getShard());

        try {
            Result<Void> result = templatePhyManager.rolloverUpgradeTemplate(param, AriusUser.CAPACITY_PLAN.getDesc());
            if(result.failed()) {
                throw new ESOperateException(result.getMessage());
            }
            LOGGER.info("class=IndexPlanManagerImpl||method=updateTemplateVersion||template={}||upgradeResult={}",
                    templatePhy.getId(), result);
        } catch (ESOperateException e) {
            LOGGER.warn("class=IndexPlanManagerImpl||method=updateTemplateVersion||template={}||errMsg={}",
                    templatePhy.getId(), e);
        }
    }

    private Long getMaxStoreInRecentSevenDayByTemplatePhyId(Long templatePhyId) {
        IndexTemplatePhy templatePhy = indexTemplatePhyService.getTemplateById(templatePhyId);
        if(templatePhy == null) {
            // 该模版不存在
            return 0L;
        }
        // 如果templatePhyId不存在map，则调用getSizeInBytesMax，再把结果放入map
        return indexMaxStoreMap.computeIfAbsent(templatePhyId, x -> getSizeInBytesMax(templatePhy));
    }

    /**
     * 获取该索引模版对应索引近七天某一天占用磁盘容量最大值
     * @param templatePhy 物理模版
     * @return 该索引模版对应索引近七天某一天占用磁盘容量最大值
     */
    private long getSizeInBytesMax(IndexTemplatePhy templatePhy) {
        // 获取上一天起一周内的索引名称，今天是10-22，则获取10-15~10-21时间
        long lastWeekDayTimestamp = AriusDateUtils.getBeforeDays(new Date(), 6).getTime();
        long yesterdayTimestamp = AriusDateUtils.getBeforeDays(new Date(), 1).getTime();
        String indexNames = IndexNameUtils.genDailyIndexName(templatePhy.getName(), lastWeekDayTimestamp, yesterdayTimestamp);
        String[] indexNameSplits = indexNames.split(",");

        List<String> indexNameList = Lists.newArrayList();
        Map<String/*不包括版本的索引名字*/, Long/*索引占用磁盘的大小*/> indexStoreSizeInBytesMap = new HashMap<>();
        for (String indexName : indexNameSplits) {
            // 当天某个索引，可能有多个版本（xxx-2021-10-10、xxx-2021-10-10-v2），所以用*来匹配
            indexNameList.add(indexName + "*");
            indexStoreSizeInBytesMap.put(indexName, 0L);
        }

        // 获取索引的stats信息
        Map<String, IndexNodes> indexNodeMap =
                esIndexService.syncGetIndexNodes(templatePhy.getCluster(), StringUtils.join(indexNameList, ","));

        if (indexNodeMap == null || indexNodeMap.isEmpty()) {
            return 0;
        }

        long sizeInBytesMax = 0;
        for (Map.Entry<String, IndexNodes> entry : indexNodeMap.entrySet()) {
            // 移除索引的版本信息（如果有版本信息的话）
            String indexNameNoVersion = IndexNameUtils.removeVersion(entry.getKey());
            // 获取索引主分片占用磁盘大小
            long indexSizeInBytes = entry.getValue().getPrimaries().getStore().getSizeInBytes();
            // 当天某个索引，可能有多个版本（xxx-2021-10-10、xxx-2021-10-10-v2），所以要进行累加
            long indexSizeInBytesTotalOfDay = indexStoreSizeInBytesMap.get(indexNameNoVersion);
            indexSizeInBytesTotalOfDay += indexSizeInBytes;
            indexStoreSizeInBytesMap.put(indexNameNoVersion, indexSizeInBytesTotalOfDay);
            if (indexSizeInBytesTotalOfDay > sizeInBytesMax) {
                sizeInBytesMax = indexSizeInBytesTotalOfDay;
            }
        }
        return sizeInBytesMax;
    }

}
