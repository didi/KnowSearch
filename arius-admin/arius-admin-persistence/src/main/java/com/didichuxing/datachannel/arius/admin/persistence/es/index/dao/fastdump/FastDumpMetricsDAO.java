package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.fastdump;

import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexLogsConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastDumpTaskLogVO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.DSLSearchUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.google.common.collect.Lists;

import lombok.NoArgsConstructor;

/**
 * @author didi
 */
@Component
@NoArgsConstructor
public class FastDumpMetricsDAO extends BaseESDAO {

    /**
     * gateway join索引
     */
    private String       indexName;
    /**
     * type名称
     */
    private final String typeName = "_doc";

    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getFastDumpMetrics();
    }

    public Tuple<Long, List<FastDumpTaskLogVO>> getTaskLogs(FastIndexLogsConditionDTO logsConditionDTO) throws ESOperateException {
        // 排序条件，默认根据使用时间排序 desc
        String sortTerm = "timeStamp";
        String sortOrder = "desc";
        String realName;
        if (StringUtils.isNotBlank(indexName) && !StringUtils.endsWith(indexName, "*")) {
            realName = indexName + "*";
        }else {
            realName = indexName;
        }
        if (!StringUtils.isEmpty(logsConditionDTO.getSortTerm())) {
            // 根据用户自定义条件排序
            sortOrder = BooleanUtils.isTrue(logsConditionDTO.getOrderByDesc()) ? "desc" : "asc";
            sortTerm = logsConditionDTO.getSortTerm();
        }
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.BASE_PAGE_SEARCH,
            buildQueryTermDsl(logsConditionDTO), sortTerm, sortOrder, logsConditionDTO.getFrom(),
            logsConditionDTO.getSize());
        return ESOpTimeoutRetry.esRetryExecute("getTaskLogs", 3, () -> gatewayClient
            .performRequestListAndGetTotalCount(null, realName, typeName, dsl, FastDumpTaskLogVO.class),
            Objects::isNull);
    }
    

    private String buildQueryTermDsl(FastIndexLogsConditionDTO logsConditionDTO) {
        Long startTime = logsConditionDTO.getStartTime() != null ? logsConditionDTO.getStartTime().getTime() : null;
        Long endTime = logsConditionDTO.getEndTime() != null ? logsConditionDTO.getEndTime().getTime() : null;

        return "[" + buildTermCell(logsConditionDTO.getFastDumpTaskIdList(),logsConditionDTO.getExecutionNode(),
            logsConditionDTO.getIndexName(), logsConditionDTO.getLogLevel(),startTime,endTime) + "]";
    }

    private String buildTermCell(List<String> fastDumpTaskIdList, String executionNode, String indexName,
                                 String logLevel, Long startTime, Long endTime) {
        List<String> termCellList = Lists.newArrayList();
        //get fastDumpTaskId term
        termCellList.add(DSLSearchUtils.getTermCellsForExactSearch(fastDumpTaskIdList, "taskId"));

        termCellList.add(DSLSearchUtils.getTermCellForWildcardSearch(executionNode, "ip"));
        termCellList.add(DSLSearchUtils.getTermCellForExactSearch(indexName, "sourceIndex"));

        //get level term
        termCellList.add(DSLSearchUtils.getTermCellForExactSearch(logLevel, "level"));

        termCellList.add(DSLSearchUtils.getTermCellForRangeSearch(startTime, endTime, "timestamp"));

        return ListUtils.strList2String(termCellList);
    }
}
