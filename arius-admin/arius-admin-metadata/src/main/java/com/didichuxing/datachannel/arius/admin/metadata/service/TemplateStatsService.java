package com.didichuxing.datachannel.arius.admin.metadata.service;

import static com.didichuxing.datachannel.arius.admin.common.util.CommonUtils.formatDouble;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateMetric;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.ProjectTemplateAccessCount;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.TemplateStatsInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.ProjectTemplateAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.TemplateAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateStatsInfoPO;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.app.ProjectTemplateAccessESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateAccessESDAO;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplateStatsService {

    protected static final ILog          LOGGER         = LogFactory.getLog(TemplateStatsService.class);

    @Autowired
    private AriusStatsIndexInfoESDAO     ariusStatsIndexInfoESDAO;

   

    @Autowired
    private TemplateAccessESDAO          templateAccessESDAO;

    @Autowired
    private ProjectTemplateAccessESDAO   projectTemplateAccessESDAO;

 

    

    @Autowired
    private IndexTemplateService         indexTemplateService;


    private static final Long            ONE_DAY        = 24 * 60 * 60 * 1000L;
    private static final Long            MINS_OF_15     = 15 * 60 * 1000L;
    private static final Long            ONE_GB         = 1024 * 1024 * 1024L;

   



    /**
     * 获取模板最近一段时间在访问的app列表
     *
     * @param logicTemplateId 模板id
     * @param days 结束日期
     * @return projectid列表
     */

    public Result<Map<Integer, Long>> getTemplateAccessProjectIds(Integer logicTemplateId, int days) {
        List<ProjectTemplateAccessCountPO> accessCountPos = projectTemplateAccessESDAO
            .getAccessProjectIdsInfoByTemplateId(logicTemplateId, days);
        if (CollectionUtils.isEmpty(accessCountPos)) {
            return Result.buildSucc();
        }

        Map<Integer, Long> ret = new HashMap<>();
        for (ProjectTemplateAccessCountPO accessCountPo : accessCountPos) {
            Integer projectId = accessCountPo.getProjectId();
            Long count = accessCountPo.getCount();

            if (null != ret.get(projectId)) {
                count += ret.get(projectId);
            }
            ret.put(projectId, count);
        }

        return Result.buildSucc(ret);
    }


    private void setMaxField(TemplateMetric templateMetric, Map<String, String> maxInfo) {
        if (StringUtils.isNotBlank(maxInfo.get("max_tps"))) {
            templateMetric.setMaxTps(Double.valueOf(maxInfo.get("max_tps")));
        } else {
            templateMetric.setMaxTps(0d);
        }
        if (StringUtils.isNotBlank(maxInfo.get("max_query_time"))) {
            templateMetric.setMaxQueryTime(Double.valueOf(maxInfo.get("max_query_time")));
        } else {
            templateMetric.setMaxQueryTime(0d);
        }
        if (StringUtils.isNotBlank(maxInfo.get("max_scroll_time"))) {
            templateMetric.setMaxScrollTime(Double.valueOf(maxInfo.get("max_scroll_time")));
        } else {
            templateMetric.setMaxScrollTime(0d);
        }
    }











    public Result<Map<Integer, Long>> getAccessStatsInfoByTemplateIdAndDays(int logicTemplateId, int days) {
        List<ProjectTemplateAccessCountPO> accessCountPos = projectTemplateAccessESDAO
            .getAccessProjectIdsInfoByTemplateId(logicTemplateId, days);
        if (CollectionUtils.isEmpty(accessCountPos)) {
            Result.build(ResultType.SUCCESS);
        }

        Map<Integer, Long> ret = Maps.newHashMap();
        for (ProjectTemplateAccessCountPO accessCountPo : accessCountPos) {
            Integer projectId = accessCountPo.getProjectId();
            Long count = accessCountPo.getCount();

            if (null != ret.get(projectId)) {
                count += ret.get(projectId);
            }
            ret.put(projectId, count);
        }

        return Result.buildSucc(ret);
    }

    public Result<TemplateStatsInfo> getTemplateBaseStatisticalInfoByLogicTemplateId(Long logicTemplateId) {
        long current = System.currentTimeMillis();

        IndexTemplate indexTemplate = indexTemplateService.getLogicTemplateById(logicTemplateId.intValue());

        if (null == indexTemplate) {
            return Result.buildFail("无法找到对应的模板");
        }

        TemplateStatsInfoPO templateStatsInfoPO = new TemplateStatsInfoPO();
        templateStatsInfoPO.setTemplateId(logicTemplateId);
        templateStatsInfoPO.setQutoa(indexTemplate.getQuota());

        FutureUtil.DEAULT_FUTURE.runnableTask(() -> {
            double totalSizeInBytes = ariusStatsIndexInfoESDAO.getLogicTemplateTotalSize(logicTemplateId);
            templateStatsInfoPO.setStoreBytes(totalSizeInBytes);
            templateStatsInfoPO.setStore(formatDouble(totalSizeInBytes / ONE_GB, 2));
        }).runnableTask(() -> {
            double maxTps = ariusStatsIndexInfoESDAO.getTemplateMaxTpsByTimeRange(logicTemplateId, current - ONE_DAY,
                current);
            templateStatsInfoPO.setWriteTps(formatDouble(maxTps, 2));
        }).runnableTask(() -> {
            long docNu = ariusStatsIndexInfoESDAO.getTemplateTotalDocNuByTimeRange(logicTemplateId, current - ONE_DAY,
                current);
            templateStatsInfoPO.setDocNu(docNu);
        }).runnableTask(() -> {
            String indexName = indexTemplate.getName();
            List<String> topics = Lists.newArrayList();
            templateStatsInfoPO.setTopics(topics);
            templateStatsInfoPO.setTemplateName(indexName);
        }).runnableTask(() -> {
            List<TemplateAccessCountPO> templateAccessCountPos = templateAccessESDAO
                .getTemplateAccessLastNDayByLogicTemplateId(logicTemplateId.intValue(), 7);
            if (CollectionUtils.isNotEmpty(templateAccessCountPos)) {
                Long count = 0L;
                for (TemplateAccessCountPO po : templateAccessCountPos) {
                    count += po.getCount();
                }

                templateStatsInfoPO.setAccessCountPreDay((double) count / templateAccessCountPos.size());
            }
        }).waitExecute();

        return Result.buildSucc(ConvertUtil.obj2Obj(templateStatsInfoPO, TemplateStatsInfo.class));
    }

    public Result<List<ProjectTemplateAccessCount>> getAccessAppInfos(int logicTemplateId, Long startDate,
                                                                      Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
            projectTemplateAccessESDAO.getAccessProjectIdsInfoByTemplateId(logicTemplateId, startDate, endDate),
            ProjectTemplateAccessCount.class));
    }

    public Result<List<ESIndexStats>> getIndexStatis(Long logicTemplateId, Long startDate, Long endDate) {
        return Result.buildSucc(
            ariusStatsIndexInfoESDAO.getTemplateRealStatis(logicTemplateId, startDate - 1 * 60 * 1000L, endDate));
    }
}