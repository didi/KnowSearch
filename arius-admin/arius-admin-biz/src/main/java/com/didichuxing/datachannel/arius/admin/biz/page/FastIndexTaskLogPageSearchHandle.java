package com.didichuxing.datachannel.arius.admin.biz.page;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.task.FastIndexTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpHostUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexLogsConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.FastIndexTaskInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastDumpTaskLogVO;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.task.OpTaskService;
import com.didichuxing.datachannel.arius.admin.core.service.task.fastindex.FastIndexTaskService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.fastdump.FastDumpMetricsDAO;
import com.didichuxing.datachannel.arius.admin.remote.fastindex.ESIndexMoveTaskService;
import com.google.common.collect.Lists;

@Component
public class FastIndexTaskLogPageSearchHandle extends
        AbstractPageSearchHandle<FastIndexLogsConditionDTO, FastDumpTaskLogVO> {
    private static final String DEFAULT_SORT_TERM = "timestamp";

    private static final Long QUERY_COUNT_THRESHOLD = 10000L;
    private static final String LOG_LEVEL_ERROR = "ERROR";

    private static final FutureUtil FUTURE_UTIL = FutureUtil.init("FastIndexTaskLogPageSearchHandle");
    @Autowired
    private FastIndexTaskService fastIndexTaskService;
    @Autowired
    private OpTaskService opTaskService;
    @Autowired
    private FastDumpMetricsDAO fastDumpMetricsDAO;

    @Override
    protected Result<Boolean> checkCondition(FastIndexLogsConditionDTO condition, Integer projectId) {

        OpTask opTask = opTaskService.getById(condition.getTaskId());
        if (null == opTask) {
            return Result.buildFail("获取数据迁移主任务失败");
        }
        if (!OpTaskTypeEnum.FAST_INDEX.getType().equals(opTask.getTaskType())) {
            return Result.buildFail("任务类型异常！");
        }

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

        if (AriusObjUtils.isBlack(condition.getSortTerm())) {
            condition.setSortTerm(DEFAULT_SORT_TERM);
        }

        List<FastIndexTaskInfo> taskInfoList = fastIndexTaskService.listByTaskId(condition.getTaskId());
        List<FastIndexTaskInfo> ret = taskInfoList;
        List<String> fastDumpTaskIdList = Lists.newArrayList("");
        if (StringUtils.isNotBlank(condition.getFastDumpTaskId())) {
            ret = taskInfoList.stream()
                    .filter(obj -> StringUtils.equals(condition.getFastDumpTaskId(), obj.getFastDumpTaskId()))
                    .collect(Collectors.toList());
        }

        if (StringUtils.isNotBlank(condition.getTemplateName())) {
            ret = taskInfoList.stream()
                    .filter(obj -> StringUtils.equals(condition.getTemplateName(), obj.getTemplateName()))
                    .collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(condition.getIndexName())) {
            ret = taskInfoList.stream().filter(obj -> StringUtils.equals(condition.getIndexName(), obj.getIndexName()))
                    .collect(Collectors.toList());
        }

        fastDumpTaskIdList.addAll(ret.stream().map(FastIndexTaskInfo::getFastDumpTaskId).filter(StringUtils::isNotBlank)
                .distinct().collect(Collectors.toList()));

        condition.setFastDumpTaskIdList(fastDumpTaskIdList);
    }

    @Override
    protected PaginationResult<FastDumpTaskLogVO> buildPageData(FastIndexLogsConditionDTO condition,
                                                                Integer projectId) {
        List<FastDumpTaskLogVO> fastDumpTaskLogVOS = Lists.newCopyOnWriteArrayList();
        AtomicLong totalCount = new AtomicLong(0L);
        Long size = condition.getSize();
        Long page = condition.getPage();
        FUTURE_UTIL.runnableTask(() -> {
            List<FastDumpTaskLogVO> listMysqlLogMessage = listMysqlLogMessage(condition);
            if (StringUtils.isNotBlank(condition.getExecutionNode())) {
                List<FastDumpTaskLogVO> filterListMysqlLogMessage = listMysqlLogMessage.stream()
                        .filter(fastDumpTaskLogVO -> fastDumpTaskLogVO.getIp().equals(condition.getExecutionNode())).collect(Collectors.toList());
                fastDumpTaskLogVOS.addAll(filterListMysqlLogMessage);
                totalCount.addAndGet(filterListMysqlLogMessage.size());
            } else {
                fastDumpTaskLogVOS.addAll(listMysqlLogMessage);
                totalCount.addAndGet(listMysqlLogMessage.size());
            }
        }).runnableTask(() -> {
            Tuple<Long, List<FastDumpTaskLogVO>> listESLogMessage = listESLogMessage(condition);
            fastDumpTaskLogVOS.addAll(listESLogMessage.getV2());
            totalCount.addAndGet(listESLogMessage.getV2().size());
        }).waitExecute();
        List<FastDumpTaskLogVO> pageFastDumpTaskLogList = fastDumpTaskLogVOS.subList(0,
                Math.min(fastDumpTaskLogVOS.size(), size.intValue()));
        return PaginationResult.buildSucc(pageFastDumpTaskLogList, totalCount.get(), page, size);
    }

    /**
     * 当内核校验通过，数据迁移失败时，从es查出异常日志
     *
     * @param condition
     * @return
     */
    private Tuple<Long, List<FastDumpTaskLogVO>> listESLogMessage(FastIndexLogsConditionDTO condition) {
        try {
            condition.setFrom(0L);
            condition.setSize(QUERY_COUNT_THRESHOLD);
            Tuple<Long, List<FastDumpTaskLogVO>> totalHitAndTaskLogsListTuple = fastDumpMetricsDAO
                    .getTaskLogs(condition);

            if (null == totalHitAndTaskLogsListTuple) {
                LOGGER.warn(
                        "class=FastIndexTaskLogPageSearchHandle||method=buildPageData||conditionDTO={}||errMsg=get empty Task Logs from es",
                        JSON.toJSONString(condition));
                return new Tuple<>(0L, Lists.newArrayList());
            }
            // 构建index信息
            return totalHitAndTaskLogsListTuple;
        } catch (Exception e) {
            LOGGER.error("class=FastIndexTaskLogPageSearchHandle||method=buildPageData||conditionDTO={}||errMsg={}",
                    JSON.toJSONString(condition), e.getMessage(), e);
            return new Tuple<>(0L, Lists.newArrayList());
        }
    }

    /**
     * 当内核校验不通过时，直接从返回信息中获取异常日志
     *
     * @param condition
     * @return
     */
    private List<FastDumpTaskLogVO> listMysqlLogMessage(FastIndexLogsConditionDTO condition) {
        OpTask opTask = opTaskService.getById(condition.getTaskId());
        if (null == opTask) {
            return new ArrayList<>();
        }
        FastIndexDTO fastIndexDTO = JSON.parseObject(opTask.getExpandData(), FastIndexDTO.class);
        if (AriusObjUtils.isNull(fastIndexDTO)) {
            return new ArrayList<>();
        }
        List<FastIndexTaskInfo> fastIndexTaskList = fastIndexTaskService.listFastIndexLogsByCondition(condition);
        //提交不成功，校验失败时
        List<FastIndexTaskInfo> fastDumpCheckErrorLogs = fastIndexTaskList.stream()
                .filter(fastIndexTaskInfo -> fastIndexTaskInfo.getTaskStatus().compareTo(FastIndexTaskStatusEnum.FAILED.getValue()) == 0
                        && StringUtils.isNotBlank(fastIndexTaskInfo.getTaskSubmitResult())
                        && StringUtils.isBlank(fastIndexTaskInfo.getFastDumpTaskId()))
                .collect(Collectors.toList());
        //提交成功，但是数据迁移失败
        List<FastIndexTaskInfo> fastDumpErrorLogs = fastIndexTaskList.stream().filter(fastIndexTaskInfo ->
                fastIndexTaskInfo.getTaskStatus().compareTo(FastIndexTaskStatusEnum.FAILED.getValue()) == 0
                        && StringUtils.isNoneBlank(fastIndexTaskInfo.getFastDumpTaskId())).collect(Collectors.toList());
        fastDumpErrorLogs.addAll(fastDumpCheckErrorLogs);
        //组装参数
        List<FastDumpTaskLogVO> fastDumpTaskLogList = fastDumpErrorLogs.stream().map(fastIndexTaskInfo -> {
            FastDumpTaskLogVO fastDumpTaskLogVO = new FastDumpTaskLogVO();
            fastDumpTaskLogVO.setLevel(LOG_LEVEL_ERROR);
            fastDumpTaskLogVO.setFailedLuceneDataPath("");
            fastDumpTaskLogVO.setSourceClusterName(fastIndexDTO.getSourceCluster());
            fastDumpTaskLogVO.setTargetClusterName(fastIndexDTO.getTargetCluster());
            fastDumpTaskLogVO.setIp(HttpHostUtil.getIpFromTransportAddress(fastIndexDTO.getTaskSubmitAddress()));
            fastDumpTaskLogVO.setTaskId(String.valueOf(opTask.getId()));
            fastDumpTaskLogVO.setSourceIndex(fastIndexTaskInfo.getIndexName());
            fastDumpTaskLogVO.setTargetIndex(fastIndexTaskInfo.getTargetIndexName());
            Optional.ofNullable(fastIndexTaskInfo.getTaskEndTime()).ifPresent(date -> fastDumpTaskLogVO.setTimestamp(date.getTime()));
            if (fastDumpCheckErrorLogs.size() != 0) {
                JSONObject jsonObject = JSONObject.parseObject(fastIndexTaskInfo.getTaskSubmitResult());
                fastDumpTaskLogVO.setMessage("cluster["+fastIndexDTO.getSourceCluster() + "]" +
                        ",indexName[" + fastIndexTaskInfo.getIndexName() + "],the reason is " + jsonObject.getString("message"));
            }else{
                JSONObject jsonObject = JSONObject.parseObject(fastIndexTaskInfo.getLastResponse());
                fastDumpTaskLogVO.setMessage("cluster["+fastIndexDTO.getSourceCluster() + "]" +
                        ",indexName[" + fastIndexTaskInfo.getIndexName() + "],the reason is " + jsonObject);
            }
            return fastDumpTaskLogVO;
        }).collect(Collectors.toList());
        return fastDumpTaskLogList;
    }
}