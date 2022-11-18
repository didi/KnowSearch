package com.didichuxing.datachannel.arius.admin.biz.page;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexLogsConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastDumpTaskLogVO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.fastdump.FastDumpMetricsDAO;
import com.google.common.collect.Lists;

@Component
public class FastIndexTaskLogPageSearchHandle extends
                                              AbstractPageSearchHandle<FastIndexLogsConditionDTO, FastDumpTaskLogVO> {
    private static final String DEFAULT_SORT_TERM     = "logMills";

    private static final Long   QUERY_COUNT_THRESHOLD = 10000L;

    @Autowired
    private FastDumpMetricsDAO  fastDumpMetricsDAO;

    @Override
    protected Result<Boolean> checkCondition(FastIndexLogsConditionDTO condition, Integer projectId) {

        String indexName = condition.getIndexName();
        if (!AriusObjUtils.isBlack(indexName) && (indexName.startsWith("*") || indexName.startsWith("?"))) {
            return Result.buildParamIllegal("索引名称不允许带类似*, ?等通配符查询");
        }

        // 只允许查询前10000条数据
        long startNum = (condition.getPage() - 1) * condition.getSize();
        if (startNum >= QUERY_COUNT_THRESHOLD) {
            return Result.buildParamIllegal(String.format("查询条数不能超过%d条", QUERY_COUNT_THRESHOLD));
        }
        return Result.buildSucc(true);
    }

    @Override
    protected void initCondition(FastIndexLogsConditionDTO condition, Integer projectId) {
        if (null == condition.getPage()) {
            condition.setPage(1L);
        }

        if (null == condition.getSize() || 0 == condition.getSize()) {
            condition.setSize(10L);
        }
        condition.setFrom((condition.getPage() - 1) * condition.getSize());

        if (AriusObjUtils.isBlack(condition.getSortTerm())) {
            condition.setSortTerm(DEFAULT_SORT_TERM);
        }
    }

    @Override
    protected PaginationResult<FastDumpTaskLogVO> buildPageData(FastIndexLogsConditionDTO condition,
                                                                Integer projectId) {

        try {
            Tuple<Long, List<FastDumpTaskLogVO>> totalHitAndTaskLogsListTuple = fastDumpMetricsDAO
                .getTaskLogs(condition);

            if (null == totalHitAndTaskLogsListTuple) {
                LOGGER.warn(
                    "class=FastIndexTaskLogPageSearchHandle||method=buildPageData||conditionDTO={}||errMsg=get empty Task Logs from es",
                    JSON.toJSONString(condition));
                return PaginationResult.buildSucc(Lists.newArrayList(), 0, condition.getPage(), condition.getSize());
            }
            // 构建index信息
            return PaginationResult.buildSucc(totalHitAndTaskLogsListTuple.getV2(),
                totalHitAndTaskLogsListTuple.getV1(), condition.getPage(), condition.getSize());
        } catch (Exception e) {
            LOGGER.error("class=FastIndexTaskLogPageSearchHandle||method=buildPageData||conditionDTO={}||errMsg={}",
                JSON.toJSONString(condition), e.getMessage(), e);
            return PaginationResult.buildFail("获取分页索引列表失败");
        }

    }
}