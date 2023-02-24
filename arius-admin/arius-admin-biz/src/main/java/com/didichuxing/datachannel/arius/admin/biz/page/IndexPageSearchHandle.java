package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.index.IndexStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.MetadataControlUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IndexPageSearchHandle extends AbstractPageSearchHandle<IndexQueryDTO, IndexCatCellVO> {
    private static final String           DEFAULT_SORT_TERM  = "timestamp";

    private static final Long             QUERY_COUNT_THRESHOLD = 10000L;

    @Autowired
    private ESIndexCatService             esIndexCatService;

    @Autowired
    private ESIndexService                esIndexService;
    private static final FutureUtil<List<IndexCatCell>> INDEX_BUILD_FUTURE = FutureUtil.init("INDEX_BUILD_FUTURE", 10, 10, 100);
    @Override
    protected Result<Boolean> checkCondition(IndexQueryDTO condition, Integer projectId) {

        if (StringUtils.isNotBlank(condition.getHealth()) && !IndexStatusEnum.isStatusExit(condition.getHealth())) {
            return Result.buildParamIllegal(String.format("健康状态%s非法", condition.getHealth()));
        }

        String indexName = condition.getIndex();
        if (!AriusObjUtils.isBlack(indexName) && (indexName.startsWith("*") || indexName.startsWith("?"))) {
            return Result.buildParamIllegal("索引名称不允许带类似*, ?等通配符查询");
        }

        // 只允许查询前10000条数据
        long startNum = (condition.getPage() - 1) * condition.getSize();
        if(startNum >= QUERY_COUNT_THRESHOLD) {
            return Result.buildParamIllegal(String.format("查询条数不能超过%d条", QUERY_COUNT_THRESHOLD));
        }
        return Result.buildSucc(true);
    }

    @Override
    protected void initCondition(IndexQueryDTO condition, Integer projectId) {
        if (null == condition.getPage()) {
            condition.setPage(1L);
        }

        if (null == condition.getSize() || 0 == condition.getSize()) {
            condition.setSize(10L);
        }

        if (AriusObjUtils.isBlack(condition.getSortTerm())) {
            condition.setSortTerm(DEFAULT_SORT_TERM);
        }
        //默认展示元数据集群的信息
        if(AriusObjUtils.isNull(condition.getShowMetadata())){
            condition.setShowMetadata(true);
        }
    }

    /**
     * 获取索引Cat/index信息
     *
     * 业务上限制ES深分页(不考虑10000条之后的数据), 由前端限制
     */
    @Override
    protected PaginationResult<IndexCatCellVO> buildPageData(IndexQueryDTO condition, Integer projectId) {
        try {
            String queryCluster = condition.getCluster();
            // 使用超级项目访问时，queryProjectId为null
            Integer queryProjectId = null;
            Boolean showMetadata = MetadataControlUtils.showMetadataInfo(condition.getShowMetadata(),projectId);
            if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
                queryProjectId = projectId;
                showMetadata = true;
            }

            Tuple<Long, List<IndexCatCell>> totalHitAndIndexCatCellListTuple = esIndexCatService.syncGetCatIndexInfo(queryCluster,
                    condition.getIndex(), condition.getHealth(), condition.getStatus(), queryProjectId,
                    (condition.getPage() - 1) * condition.getSize(), condition.getSize(), condition.getSortTerm(),
                    condition.getOrderByDesc(), showMetadata);

            if (null == totalHitAndIndexCatCellListTuple) {
                LOGGER.warn("class=IndexPageSearchHandle||method=buildPageData||clusters={}||index={}||"
                                + "errMsg=get empty index cat info from es",
                        condition.getCluster(), condition.getIndex());
                return PaginationResult.buildSucc(Lists.newArrayList(), 0, condition.getPage(), condition.getSize());
            }
            // 构建index信息
            List<IndexCatCellVO> indexCatCellVOList = buildExtraIndexInfo(totalHitAndIndexCatCellListTuple.getV2());
            return PaginationResult.buildSucc(indexCatCellVOList, totalHitAndIndexCatCellListTuple.getV1(),
                condition.getPage(), condition.getSize());
        } catch (Exception e) {
            LOGGER.error(
                "class=IndexPageSearchHandle||method=buildPageData||clusters={}||index={}||errMsg={}",
                condition.getCluster(), condition.getIndex(), e.getMessage(), e);
            return PaginationResult.buildFail("获取分页索引列表失败");
        }
    }

    /**
     * 批量构建索引实时数据(包含block、aliases、translog、恢复优先级)
     * @param catCellList    索引cat/index基本信息
     * @return               List<IndexCatCell>
     */
    private List<IndexCatCellVO> buildExtraIndexInfo(List<IndexCatCell> catCellList) {
        List<IndexCatCell> finalIndexCatCellList = Lists.newCopyOnWriteArrayList(catCellList);
        Map<String, List<IndexCatCell>> cluster2IndexCatCellListMap = ConvertUtil.list2MapOfList(finalIndexCatCellList,
            IndexCatCell::getCluster, indexCatCell -> indexCatCell);
        if (MapUtils.isEmpty(cluster2IndexCatCellListMap)) {
            return ConvertUtil.list2List(finalIndexCatCellList, IndexCatCellVO.class);
        }

        cluster2IndexCatCellListMap.forEach((cluster, indexCatCellList) -> {
            INDEX_BUILD_FUTURE.runnableTask(() -> {
                esIndexService.buildIndexAliasesAndBlockInfo(cluster, indexCatCellList);
                esIndexService.buildIndexSettingsInfo(cluster, indexCatCellList);
            });
        });
        INDEX_BUILD_FUTURE.waitExecute();
        List<IndexCatCellVO> indexCatCellVOList = ConvertUtil.list2List(finalIndexCatCellList, IndexCatCellVO.class);
        return indexCatCellVOList;
    }
}