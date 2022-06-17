package com.didichuxing.datachannel.arius.admin.biz.page;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.constant.index.IndexBlockEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.index.IndexStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.IndexConfig;
import com.google.common.collect.Lists;

@Component
public class IndexPageSearchHandle extends AbstractPageSearchHandle<IndexQueryDTO, IndexCatCellVO> {
    private static final String DEFAULT_SORT_TERM = "timestamp";

    @Autowired
    private ESIndexCatService   esIndexCatService;

    @Autowired
    private ESIndexService      esIndexService;
    private static final FutureUtil<Void> INDEX_BUILD_FUTURE = FutureUtil.init("INDEX_BUILD_FUTURE", 10, 10, 100);


    @Override
    protected Result<Boolean> checkCondition(IndexQueryDTO condition, Integer appId) {

        if (StringUtils.isNotBlank(condition.getHealth()) && !IndexStatusEnum.isStatusExit(condition.getHealth())) {
            return Result.buildParamIllegal(String.format("健康状态%s非法", condition.getHealth()));
        }

        String indexName = condition.getIndex();
        if (!AriusObjUtils.isBlack(indexName) && (indexName.startsWith("*") || indexName.startsWith("?"))) {
            return Result.buildParamIllegal("索引名称不允许带类似*, ?等通配符查询");
        }

        return Result.buildSucc(true);
    }

    @Override
    protected void initCondition(IndexQueryDTO condition, Integer appId) {
        if (null == condition.getPage()) {
            condition.setPage(1L);
        }

        if (null == condition.getSize() || 0 == condition.getSize()) {
            condition.setSize(10L);
        }

        if (AriusObjUtils.isBlack(condition.getSortTerm())) {
            condition.setSortTerm(DEFAULT_SORT_TERM);
        }
    }

    /**
     * 获取索引Cat/index信息
     *
     * 业务上限制ES深分页(不考虑10000条之后的数据), 由前端限制
     */
    @Override
    protected PaginationResult<IndexCatCellVO> buildPageData(IndexQueryDTO condition, Integer appId) {
        try {
            Tuple<Long, List<IndexCatCell>> totalHitAndIndexCatCellListTuple;
            if (appService.isSuperApp(appId)) {
                totalHitAndIndexCatCellListTuple = esIndexCatService.syncGetCatIndexInfo(condition.getCluster(),
                        condition.getIndex(), condition.getHealth(), null, null,
                        (condition.getPage() - 1) * condition.getSize(), condition.getSize(), condition.getSortTerm(),
                        condition.getOrderByDesc());
            } else {
                totalHitAndIndexCatCellListTuple = esIndexCatService.syncGetCatIndexInfo(null, condition.getIndex(),
                        condition.getHealth(), appId, condition.getCluster(),
                        (condition.getPage() - 1) * condition.getSize(), condition.getSize(), condition.getSortTerm(),
                        condition.getOrderByDesc());
            }
            if (null == totalHitAndIndexCatCellListTuple) {
                LOGGER.warn("class=IndicesPageSearchHandle||method=getIndexCatCellsFromES||clusters={}||index={}||"
                                + "errMsg=get empty index cat info from es",
                        condition.getCluster(), condition.getIndex());
                return PaginationResult.buildSucc(Lists.newArrayList(), 0, condition.getPage(), condition.getSize());
            }

            //设置索引阻塞信息
            List<IndexCatCell> finalIndexCatCellList = batchFetchIndexRealTimeData(totalHitAndIndexCatCellListTuple.getV2());
            List<IndexCatCellVO> indexCatCellVOList = ConvertUtil.list2List(finalIndexCatCellList, IndexCatCellVO.class);

            return PaginationResult.buildSucc(indexCatCellVOList, totalHitAndIndexCatCellListTuple.getV1(),
                    condition.getPage(), condition.getSize());
        } catch (Exception e) {
            LOGGER.error(
                    "class=IndicesPageSearchHandle||method=getIndexCatCellsFromES||clusters={}||index={}||errMsg={}",
                    condition.getCluster(), condition.getIndex(), e.getMessage(), e);
            return PaginationResult.buildFail("获取分页索引列表失败");
        }
    }

    /**
     * 批量构建索引实时数据(包含block和aliases)
     * @param catCellList    索引cat/index基本信息
     * @return               List<IndexCatCell>
     */
    private List<IndexCatCell> batchFetchIndexRealTimeData(List<IndexCatCell> catCellList) {
        List<IndexCatCell> finalIndexCatCellList = Lists.newCopyOnWriteArrayList();
        Map<String, List<IndexCatCell>> cluster2IndexCatCellListMap = ConvertUtil.list2MapOfList(catCellList,
                IndexCatCell::getClusterPhy, indexCatCell -> indexCatCell);
        if (MapUtils.isEmpty(cluster2IndexCatCellListMap)) {
            return finalIndexCatCellList;
        }

        cluster2IndexCatCellListMap.forEach((cluster, indexCatCellList) -> {
            INDEX_BUILD_FUTURE.runnableTask(() -> {
                buildIndexRealTimeData(cluster, indexCatCellList);
                finalIndexCatCellList.addAll(indexCatCellList);
            });
        });
        INDEX_BUILD_FUTURE.waitExecute();

        return finalIndexCatCellList;
    }

    private void buildIndexRealTimeData(String cluster, List<IndexCatCell> indexCatCellList) {
        if (CollectionUtils.isNotEmpty(indexCatCellList)) {
            List<String> indexNameList = indexCatCellList.stream().map(IndexCatCell::getIndex).collect(Collectors.toList());
            Map<String, IndexConfig> name2IndexConfigMap = esIndexService.syncGetIndexSetting(cluster, indexNameList, 3);

            Map<String, List<String>> aliasMap = esIndexService.syncGetIndexAliasesByIndices(cluster, indexNameList.toArray(new String[0]));
            indexCatCellList.forEach(indexCatCell -> {
                indexCatCell.setAliases(aliasMap.getOrDefault(indexCatCell.getIndex(), Lists.newArrayList()));
                
                IndexConfig indexConfig = name2IndexConfigMap.get(indexCatCell.getIndex());
                Tuple<Boolean, Boolean> writeAndReadBlockFromMerge = getWriteAndReadBlock(indexConfig);

                indexCatCell.setReadFlag(writeAndReadBlockFromMerge.getV1() != null && writeAndReadBlockFromMerge.getV1());
                indexCatCell.setWriteFlag(writeAndReadBlockFromMerge.getV2() != null && writeAndReadBlockFromMerge.getV2());
            });
        } else {
            LOGGER.warn("class=IndicesPageSearchHandle||method=buildBlockInfo||cluster={}||index={}||errMsg=index is empty", cluster);
        }
    }

    @NotNull
    private Tuple<Boolean, Boolean> getWriteAndReadBlock(IndexConfig indexConfig) {
        Tuple<Boolean, Boolean> writeAndReadBlockFromMerge = new Tuple<>();
        //build from es setUp settings
        Tuple<Boolean, Boolean> writeAndReadBlockFromSetUpSettingTuple = new Tuple<>();
        writeAndReadBlockFromSetUpSettingTuple.setV1(null);
        writeAndReadBlockFromSetUpSettingTuple.setV2( null);
        Optional.ofNullable(indexConfig.getSettings()).filter(MapUtils::isNotEmpty).map(JSON::toJSONString).map(JSON::parseObject).ifPresent(settingsObj -> {
            writeAndReadBlockFromSetUpSettingTuple.setV1(settingsObj.getBoolean(READ));
            writeAndReadBlockFromSetUpSettingTuple.setV2(settingsObj.getBoolean(WRITE));
        });

        //build from es default settings
        Tuple<Boolean, Boolean> writeAndReadBlockFromDefaultSettingTuple = new Tuple<>();
        writeAndReadBlockFromDefaultSettingTuple.setV1(null);
        writeAndReadBlockFromDefaultSettingTuple.setV2(null);
        Optional.ofNullable(indexConfig.getOther(DEFAULTS)).map(Object::toString).map(JSON::parseObject).map(defaultObj -> defaultObj.getJSONObject(INDEX))
                .map(indexSettings -> indexSettings.getJSONObject(BLOCKS)).ifPresent(blocksObj -> {
                    if (null != blocksObj.get(IndexBlockEnum.READ.getType())) {
                        writeAndReadBlockFromDefaultSettingTuple
                                .setV1(blocksObj.getBoolean(IndexBlockEnum.READ.getType()));
                    }
                    if (null != blocksObj.get(IndexBlockEnum.WRITE.getType())) {
                        writeAndReadBlockFromDefaultSettingTuple
                                .setV2(blocksObj.getBoolean(IndexBlockEnum.WRITE.getType()));
                    }
                });

        //set read block info
        if (null != writeAndReadBlockFromSetUpSettingTuple.getV1()) {
            writeAndReadBlockFromMerge.setV1(writeAndReadBlockFromSetUpSettingTuple.getV1());
        } else if (null != writeAndReadBlockFromDefaultSettingTuple.getV1()) {
            writeAndReadBlockFromMerge.setV1(writeAndReadBlockFromDefaultSettingTuple.getV1());
        }

        //set write block info
        if (null != writeAndReadBlockFromSetUpSettingTuple.getV2()) {
            writeAndReadBlockFromMerge.setV2(writeAndReadBlockFromSetUpSettingTuple.getV2());
        } else if (null != writeAndReadBlockFromDefaultSettingTuple.getV2()) {
            writeAndReadBlockFromMerge.setV2(writeAndReadBlockFromDefaultSettingTuple.getV2());
        }

        return writeAndReadBlockFromMerge;
    }
}
