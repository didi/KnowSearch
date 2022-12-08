package com.didichuxing.datachannel.arius.admin.biz.task;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexLogsConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexRateLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySetting;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastDumpTaskLogVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastIndexBriefVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastIndexDetailVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;

import java.util.List;

/**
 * 任务 Service
 *
 * @author d06679
 * @date 2020/12/21
 */
public interface FastIndexManager {

    /**
     * 提交任务
     *
     * @param fastIndexDTO 任务数据
     * @param operator     操作人
     * @param projectId    项目id
     * @return Result
     * @throws NotFindSubclassException 没有发现异常子类
     */
    Result<WorkTaskVO> submitTask(FastIndexDTO fastIndexDTO, String operator,
                                  Integer projectId);

    Result<Void> refreshTask(Integer taskId);

    Result<Void> cancelTask(Integer taskId, Integer projectId);

    Result<Void> restartTask(Integer taskId, Integer projectId);

    Result<Void> modifyTaskRateLimit(Integer taskId, FastIndexRateLimitDTO fastIndexRateLimitDTO);

    Result<FastIndexDetailVO> getTaskDetail(Integer taskId);

    Result<Void> transferTemplate(Integer taskId);

    Result<Void> rollbackTemplate(Integer taskId);

    PaginationResult<FastDumpTaskLogVO> pageGetTaskLogs(Integer projectId,
                                                        FastIndexLogsConditionDTO queryDTO) throws NotFindSubclassException;

    /**
     * 获取模板和索引的下拉框值
     * @param taskId
     * @return
     */
    Result<List<FastIndexBriefVO>> getFastIndexBrief(Integer taskId);

    /**
     * 索引模式-查看setting,去掉es原生默认加上的字段
     * @param cluster
     * @param indexName
     * @param projectId
     * @return
     */
    Result<IndexSettingVO> getSetting(String cluster, String indexName, Integer projectId);

    /**
     * 模板模式-查看setting，替换分配节点
     * @param logicId
     * @param logicClusterId
     * @return
     */
    Result<IndexTemplatePhySetting> getTemplateSettings(Integer logicId, Long logicClusterId);

    /**
     * 获取当前支持数据迁移的集群版本,使用动态配置获取支持的版本
     * @return
     */
    List<ClusterPhyVO> clustersSupportedFastDump();
}