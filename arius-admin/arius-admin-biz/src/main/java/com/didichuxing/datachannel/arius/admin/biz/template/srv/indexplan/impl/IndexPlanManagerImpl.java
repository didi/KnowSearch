package com.didichuxing.datachannel.arius.admin.biz.template.srv.indexplan.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.BYTE_TO_G;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.G_PER_SHARD;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.indexplan.IndexPlanManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalUpgradeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author chengxiang, jiamin
 * @date 2022/5/11
 */
@Service("newIndexPlanManagerImpl")
public class IndexPlanManagerImpl extends BaseTemplateSrvImpl implements IndexPlanManager {

    private final static Integer  SINGLE_SHARD_MAX_SIZE       = 50;
    private final static Integer  SINGLE_SHARD_RECOMMEND_SIZE = 30;

    /**
     * (key, value) = (模板id, 该模版对应索引近七天某一天占用磁盘容量最大值)
     */
    private final Map<Long, Long> indexMaxStoreMap            = Maps.newConcurrentMap();

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @Autowired
    private ESIndexService        esIndexService;

    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.INDEX_PLAN;
    }

    @Override
    public Result<Void> indexRollover(Integer logicTemplateId) {
        LOGGER.info("class=IndexPlanManagerImpl||method=indexRollover||logicTemplateId={}||msg=start indexRollover",
            logicTemplateId);
        if (!isTemplateSrvOpen(logicTemplateId)) {
            return Result.buildFail("指定索引模板未开启rollover能力");
        }

        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);
        if (CollectionUtils.isEmpty(templatePhyList)) {
            LOGGER.info(
                "class=IndexPlanManagerImpl||method=indexRollover||logicTemplateId={}||msg=IndexRolloverTask no physical template",
                logicTemplateId);
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
            if (primaryShardCnt == null || indexNodes == null) {
                continue;
            }

            // 当天最高版本的索引占用磁盘的容量
            long curSizeInBytes = indexNodes.getPrimaries().getStore().getSizeInBytes();
            double curSizeInGb = curSizeInBytes * BYTE_TO_G;

            double rolloverThreshold = ariusConfigInfoService.doubleSetting(AriusConfigConstant.ARIUS_COMMON_GROUP,
                    AriusConfigConstant.INDEX_ROLLOVER_THRESHOLD, 50.0);

            if (curSizeInGb >= primaryShardCnt * rolloverThreshold) {
                // 如果大于（主shard个数 * 推荐的单个shard大小50G），直接升版本
                updateTemplateVersion(templatePhy);
            } else if (curSizeInGb >= primaryShardCnt * SINGLE_SHARD_RECOMMEND_SIZE
                       && TemplateUtils.isSaveByDay(template.getDateFormat())) {
                // 如果大于（主shard个数 * 推荐的单个shard大小30G），并且索引模版是按天创建索引
                // 获取该索引模版对应索引近7天占用磁盘的最大值
                Long sizeInBytesMax = getMaxStoreInRecentSevenDayByTemplatePhyId(templatePhy.getId());
                // 比较两者大小，大于则升版本
                if (curSizeInBytes > sizeInBytesMax) {
                    updateTemplateVersion(templatePhy);
                }
            }
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Boolean> adjustShardNum(Integer logicTemplateId) throws ESOperateException {
        LOGGER.info(
            "class=IndexPlanManagerImpl||method=adjustShardCount||logicTemplateId={}||msg=start adjustShardCount",
            logicTemplateId);
        if (!isTemplateSrvOpen(logicTemplateId)) {
            return Result.buildFail(logicTemplateId + "没有开启" + templateSrvName());
        }

        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);
        if (CollectionUtils.isEmpty(templatePhyList)) {
            LOGGER.info(
                "class=IndexPlanManagerImpl||method=adjustShardCount||logicTemplateId={}||msg=IndexRolloverTask no physical template",
                logicTemplateId);
            return Result.build(Boolean.TRUE);
        }

        governPerTemplate(templatePhyList);
          return Result.build(Boolean.TRUE);
    }

    @Override
    public void initShardRoutingAndAdjustShard(IndexTemplatePhyDTO param) {
        int shard = param.getShard();
        if (shard >= 320) {
            param.setShardRouting(32);
            param.setShard(calculateShardByShardRouting(shard, 32));
        } else if (shard >= 80) {
            param.setShardRouting(16);
            param.setShard(calculateShardByShardRouting(shard, 16));
        } else if (shard >= 16) {
            param.setShardRouting(8);
            param.setShard(calculateShardByShardRouting(shard, 8));
        } else if (shard >= 4) {
            param.setShardRouting(4);
            param.setShard(calculateShardByShardRouting(shard, 4));
        } else {
            param.setShardRouting(1);
        }
    }

    //////////private method/////////////////////////////////////////////////////////////

    private String getIndexNameByDateFormat(IndexTemplate logiTemplate, IndexTemplatePhy phyTemplate) {
        if (TemplateUtils.isSaveByDay(logiTemplate.getDateFormat())) {
            // 按天分区则获取模版对应当天索引拼接版本信息
            return IndexNameUtils.genDailyIndexNameWithVersion(phyTemplate.getName(), 0, phyTemplate.getVersion());
        } else if (TemplateUtils.isSaveByMonth(logiTemplate.getDateFormat())) {
            // 按月分区则获取模版对应当月索引拼接版本信息
            return IndexNameUtils.genCurrentMonthlyIndexNameWithVersion(phyTemplate.getName(),
                phyTemplate.getVersion());
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
        if (indexNodeMap == null || indexNodeMap.size() == 0) {
            LOGGER.warn("class=IndexPlanManagerImpl||method=getIndexNodes||index={}||errMsg={}", indexName,
                "获取不到该索引的IndexNodes（元数据）信息，索引也许不存在");
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
            if (result.failed()) {
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
        if (templatePhy == null) {
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
        String indexNames = IndexNameUtils.genDailyIndexName(templatePhy.getName(), lastWeekDayTimestamp,
            yesterdayTimestamp);
        String[] indexNameSplits = indexNames.split(",");

        List<String> indexNameList = Lists.newArrayList();
        Map<String/*不包括版本的索引名字*/, Long/*索引占用磁盘的大小*/> indexStoreSizeInBytesMap = Maps.newHashMap();
        for (String indexName : indexNameSplits) {
            // 当天某个索引，可能有多个版本（xxx-2021-10-10、xxx-2021-10-10-v2），所以用*来匹配
            indexNameList.add(indexName + "*");
            indexStoreSizeInBytesMap.put(indexName, 0L);
        }

        // 获取索引的stats信息
        Map<String, IndexNodes> indexNodeMap = esIndexService.syncGetIndexNodes(templatePhy.getCluster(),
            StringUtils.join(indexNameList, ","));

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

    private void governPerTemplate(Collection<IndexTemplatePhy> templatePhyList) throws ESOperateException {
        // 就一个模板 直接改
        if (templatePhyList.size() == 1) {
            List<IndexTemplatePhy> list = Lists.newArrayList(templatePhyList);
            Result<String> result = adjustShardCount(list.get(0));
            if (result.failed()) {
                LOGGER.warn(
                    "class=IndexPlanManagerImpl||method=governPerTemplate||template={}||msg=adjust shard count fail={}",
                    list.get(0).getName(), result.getMessage());
            }
            return;
        }

        // 先改主再改从
        List<IndexTemplatePhy> masterTemplatePhyList = templatePhyList.stream()
            // 只保留主角色的物理模版
            .filter(x -> x.getRole().equals(TemplateDeployRoleEnum.MASTER.getCode())).collect(Collectors.toList());

        if (masterTemplatePhyList.isEmpty()) {
            return;
        }

        for (IndexTemplatePhy masterTemplatePhy : masterTemplatePhyList) {
            // 修改主
            Result<String> masterResult = adjustShardCount(masterTemplatePhy);
            if (masterResult.failed()) {
                LOGGER.warn(
                    "class=IndexPlanManagerImpl||method=governPerTemplate||masterTemplate={}||msg=adjust shard count fail={}",
                    masterTemplatePhy.getName(), masterResult.getMessage());
                return;
            }

            // 获取从物理模版
            List<IndexTemplatePhy> slaveTemplatePhyList = templatePhyList.stream()
                // 只保留从角色的物理模版
                .filter(x -> x.getRole().equals(TemplateDeployRoleEnum.SLAVE.getCode())).collect(Collectors.toList());

            for (IndexTemplatePhy slaveTemplatePhy : slaveTemplatePhyList) {
                // 修改从
                Result<String> slaveResult = adjustShardCount(slaveTemplatePhy);
                if (slaveResult.failed()) {
                    LOGGER.warn(
                        "class=IndexPlanManagerImpl||method=governPerTemplate||slaveTemplate={}||msg=adjust shard count fail={}",
                        masterTemplatePhy.getName(), masterResult.getMessage());
                }
            }
        }
    }

    /**
     * shard个数调整
     * 非按天滚动，无需调整主shard个数
     * @param templatePhy 物理模版
     * @return result 结果
     * @throws ESOperateException e
     */
    private Result<String> adjustShardCount(IndexTemplatePhy templatePhy) throws ESOperateException {
        IndexTemplate logicTemplate = indexTemplateService.getLogicTemplateById(templatePhy.getLogicId());
        if (!TemplateUtils.isSaveByDay(logicTemplate.getDateFormat())) {
            // 非按天滚动，无需调整主shard个数
            return Result.buildSucc();
        }
        int shardCount = calculateShardCount(templatePhy);
        if (shardCount < 1) {
            return Result.buildFail("计算shard个数失败");
        }

        if (templatePhy.getShard() != shardCount) {
            // 不等于old的shard，则可能是缩shard、或扩shard
            Result<Void> result = editTemplateWithoutCheck(templatePhy, shardCount, AriusUser.CAPACITY_PLAN.getDesc());
            if (result.success()) {
                LOGGER.info("class=IndexPlanManagerImpl||method=adjustShardCount||template={}||shardCount={}->{}",
                    templatePhy.getName(), templatePhy.getShard(), shardCount);
            }
        }

        return Result.buildSucc();
    }

    private int calculateShardCount(IndexTemplatePhy templatePhy) {
        long sizeInBytesMax = getSizeInBytesMax(templatePhy);
        // 放进缓存（主要提供给IndexRolloverTask功能作数据参考）
        indexMaxStoreMap.put(templatePhy.getId(), sizeInBytesMax);
        return (int) (sizeInBytesMax * BYTE_TO_G / G_PER_SHARD) + 1;
    }

    private Result<Void> editTemplateWithoutCheck(IndexTemplatePhy templatePhy, Integer shardNum,
                                                  String operator) throws ESOperateException {
        // 计算 ShardRouting，并通过 ShardRouting 再计算 shard
        IndexTemplatePhyDTO param = new IndexTemplatePhyDTO();
        param.setShard(templatePhy.getShard());
        param.setShardRouting(templatePhy.getShardRouting());
        initShardRoutingAndAdjustShard(param);
        templatePhy.setShard(param.getShard());
        templatePhy.setShardRouting(param.getShardRouting());

        // 获取明天的索引名
        String indexName = IndexNameUtils.genDailyIndexNameWithVersion(templatePhy.getName(), -1,
            templatePhy.getVersion());
        if (esIndexService.syncIsIndexExist(templatePhy.getCluster(), indexName)) {
            // 如果明天的索引已经存在，则删除
            List<String> indexNameList = new ArrayList<>();
            indexNameList.add(indexName);
            esIndexService.syncBatchDeleteIndices(templatePhy.getCluster(), indexNameList, 1);
        }

        // 更新
        return indexTemplatePhyService.updateTemplateShardNum(templatePhy, shardNum, operator);
    }

    private Integer calculateShardByShardRouting(int shard, int shardRouting) {
        if (shard % shardRouting == 0) {
            return shard;
        }
        return (shard / shardRouting + 1) * shardRouting;
    }

    /////////////////////////SRV
    @Override
    public boolean indexRollover(String phyClusterName) {
        LOGGER.info("class=CapacityPlanManagerImpl||method=indexRollover||cluster={}||msg=start indexRollover",
            phyClusterName);
        // 判断指定物理集群是否开启了当前索引服务
        //if (!isTemplateSrvOpen(phyClusterName)) {
        //    return false;
        //}

        // 获取所有的索引物理模版
        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getNormalTemplateByCluster(phyClusterName);
        if (CollectionUtils.isEmpty(templatePhyList)) {
            LOGGER.info(
                "class=CapacityPlanManagerImpl||method=indexRollover||cluster={}||msg=IndexRolloverTask no template",
                phyClusterName);
            return true;
        }

        for (IndexTemplatePhy phyTemplate : templatePhyList) {
            // 判断该索引模版是否开启当前索引服务
            IndexTemplateConfig config = indexTemplateService.getTemplateConfig(phyTemplate.getLogicId());
            
            if (config == null || config.getDisableIndexRollover()) {
                LOGGER.info(
                    "class=CapacityPlanManagerImpl||method=indexRollover||cluster={}||template={}||msg=skip indexRollover",
                    phyClusterName, phyTemplate.getName());
                continue;
            }
            //判断集群的模版是否开启了索引规划rollover
            if (!isTemplateSrvOpen(phyTemplate.getLogicId())){
                continue;
            }
            // 获取逻辑模版信息
            IndexTemplate logiTemplate = indexTemplateService.getLogicTemplateById(phyTemplate.getLogicId());

            // 根据索引分区规则，获取当天或当月或不分区带有版本信息的索引的名字
            String indexName = getIndexNameByDateFormat(logiTemplate, phyTemplate);

            // 获取indexNodes信息（该索引对应的元信息）
            IndexNodes indexNodes = getIndexNodes(indexName, phyClusterName);
            // 获取索引的主shard个数，这里不能直接从数据库获取，因为可能会被改变，所以从ES中获取
            Integer primaryShardCnt = esIndexService.syncGetIndexPrimaryShardNumber(phyClusterName, indexName);
            if (primaryShardCnt == null || indexNodes == null) {
                continue;
            }

            // 当天最高版本的索引占用磁盘的容量
            long curSizeInBytes = indexNodes.getPrimaries().getStore().getSizeInBytes();
            double curSizeInGb = curSizeInBytes * BYTE_TO_G;

            double rolloverThreshold = ariusConfigInfoService.doubleSetting(AriusConfigConstant.ARIUS_COMMON_GROUP,
                    AriusConfigConstant.INDEX_ROLLOVER_THRESHOLD, 50.0);

            if (curSizeInGb >= primaryShardCnt * rolloverThreshold) {
                // 如果大于（主shard个数 * 推荐的单个shard大小50G），直接升版本
                updateTemplateVersion(phyTemplate);
            } else if (curSizeInGb >= primaryShardCnt * 30 && TemplateUtils.isSaveByDay(logiTemplate.getDateFormat())) {
                // 如果大于（主shard个数 * 推荐的单个shard大小30G），并且索引模版是按天创建索引
                // 获取该索引模版对应索引近7天占用磁盘的最大值
                Long sizeInBytesMax = getMaxStoreInRecentSevenDayByTemplatePhyId((phyTemplate.getId()));
                // 比较两者大小，大于则升版本
                if (curSizeInBytes > sizeInBytesMax) {
                    updateTemplateVersion(phyTemplate);
                }
            }
        }
        return true;
    }

    @Override
    public Result<Void> adjustShardCountByPhyClusterName(String phyClusterName) {
        LOGGER.info(
            "class=CapacityPlanManagerImpl||method=adjustShardCountByPhyClusterName||cluster={}||msg=start adjustShardCount",
            phyClusterName);
        //物理集群侧不在判读
        //if (!isTemplateSrvOpen(phyClusterName)) {
        //    return Result.buildFail(phyClusterName + "没有开启" + templateServiceName());
        //}

        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.listTemplate();
       
        if (AriusObjUtils.isEmptyList(templatePhyList)) {
            return Result.buildSucc();
        }

        Multimap<Integer/*逻辑模版id*/, IndexTemplatePhy/*物理模版*/> multimap = ConvertUtil.list2MulMap(templatePhyList,
            IndexTemplatePhy::getLogicId);

        for (Integer templateLogicId : multimap.keySet()) {
            try {
                if (!isTemplateSrvOpen(templateLogicId)){
                    continue;
                }
                governPerTemplate(multimap.get(templateLogicId));
            } catch (Exception e) {
                LOGGER.warn(
                    "class=CapacityPlanManagerImpl||method=adjustShardCountByPhyClusterName||templateLogicId={}||errMsg={}",
                    templateLogicId, e.getMessage(), e);
            }
        }
        return Result.buildSucc();
    }

}