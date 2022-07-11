package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyQuickCommandShardsQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.ShardDistributionVO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESShardCatService;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 快捷命令shard分布.
 *
 * @ClassName QuickCommandShardsDistributionPageSearchHandle
 * @Author gyp
 * @Date 2022/7/6
 * @Version 1.0
 */
@Component
public class QuickCommandShardsDistributionPageSearchHandle extends AbstractPageSearchHandle<ClusterPhyQuickCommandShardsQueryDTO, ShardDistributionVO> {
    private static final String DEFAULT_SORT_TERM = "timestamp";

    @Autowired
    private ESShardCatService esShardCatService;

    @Override
    protected Result<Boolean> checkCondition(ClusterPhyQuickCommandShardsQueryDTO condition, Integer projectId) {
        if (StringUtils.isBlank(condition.getCluster())) {
            return Result.buildParamIllegal(String.format("集群名称不能为空"));
        }
        return Result.buildSucc(true);
    }

    @Override
    protected void initCondition(ClusterPhyQuickCommandShardsQueryDTO condition, Integer projectId) {
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

    @Override
    protected PaginationResult<ShardDistributionVO> buildPageData(ClusterPhyQuickCommandShardsQueryDTO condition, Integer projectId) {
        try {
            String queryCluster = condition.getCluster();
            // 使用超级项目访问时，queryProjectId为null
            Integer queryProjectId = null;
            Tuple<Long, List<ShardDistributionVO>> totalHitAndIndexCatCellListTuple = esShardCatService.syncGetCatShardInfo(
                    queryCluster, condition.getIndex(), queryProjectId,
                    (condition.getPage() - 1) * condition.getSize(), condition.getSize(), condition.getSortTerm(),
                    condition.getOrderByDesc());
            if (null == totalHitAndIndexCatCellListTuple) {
                LOGGER.warn("class=QuickCommandShardsDistributionPageSearchHandle||method=buildPageData||clusters={}||index={}||"
                                + "errMsg=get empty index cat info from es",
                        condition.getCluster(), condition.getIndex());
                return PaginationResult.buildSucc(Lists.newArrayList(), 0, condition.getPage(), condition.getSize());
            }

            List<ShardDistributionVO> indexCatCellVOList = ConvertUtil.list2List(totalHitAndIndexCatCellListTuple.getV2(), ShardDistributionVO.class);

            return PaginationResult.buildSucc(indexCatCellVOList, totalHitAndIndexCatCellListTuple.getV1(),
                    condition.getPage(), condition.getSize());
        } catch (Exception e) {
            LOGGER.error(
                    "class=QuickCommandShardsDistributionPageSearchHandle||method=buildPageData||clusters={}||index={}||errMsg={}",
                    condition.getCluster(), condition.getIndex(), e.getMessage(), e);
            return PaginationResult.buildFail("获取分页shard列表失败");
        }
    }
}