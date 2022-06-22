package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.pipeline.PipelineManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ESPipelineProcessor;
import com.didichuxing.datachannel.arius.admin.common.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePhyPO;
import com.didichuxing.datachannel.arius.admin.common.constant.ESClusterVersionEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.NewTemplateSrvEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESPipelineDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhyDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.PIPELINE_RATE_LIMIT_MAX_VALUE;
import static com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESPipelineDAO.*;
import static com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESPipelineDAO.INDEX_VERSION;

/**
 * @author chengxiang, d06679
 * @date 2022/5/13
 */
@Service
public class PipelineManagerImpl extends BaseTemplateSrvImpl implements PipelineManager {

    private static final ILog LOGGER = LogFactory.getLog(PipelineManagerImpl.class);
    private static final Integer RETRY_TIMES = 3;

    @Autowired
    private ESPipelineDAO esPipelineDAO;

    @Autowired
    private IndexTemplatePhyDAO indexTemplatePhyDAO;


    @Override
    public NewTemplateSrvEnum templateSrv() {
        return NewTemplateSrvEnum.TEMPLATE_PIPELINE;
    }

    @Override
    public Result<Void> createPipeline(Integer templatePhyId) {
        IndexTemplatePhy indexTemplatePhy = indexTemplatePhyService.getTemplateById(templatePhyId.longValue());
        if (null == indexTemplatePhy) {
            return Result.buildFail("物理模板不存在");
        }

        IndexTemplate indexTemplate = indexTemplateService.getLogicTemplateById(indexTemplatePhy.getLogicId());
        if (null == indexTemplate) {
            return Result.buildFail("逻辑模板不存在");
        }

        if (!isTemplateSrvOpen(indexTemplate.getId())) {
            return Result.buildFail("未开启pipeLine服务");
        }

        Integer rateLimit = getDynamicRateLimit(indexTemplatePhy);
        return doCreatePipeline(indexTemplatePhy, indexTemplate, rateLimit);
    }

    @Override
    public Result<Void> syncPipeline(Integer templatePhyId) {
        IndexTemplatePhy indexTemplatePhy = indexTemplatePhyService.getTemplateById(templatePhyId.longValue());
        if (null == indexTemplatePhy) {
            return Result.buildFail("物理模板不存在");
        }

        IndexTemplate indexTemplate = indexTemplateService.getLogicTemplateById(indexTemplatePhy.getLogicId());
        if (null == indexTemplate) {
            return Result.buildFail("逻辑模板不存在");
        }

        if (!isTemplateSrvOpen(indexTemplate.getId())) {
            return Result.buildFail("未开启pipeLine服务");
        }

        try {
            ESPipelineProcessor esPipelineProcessor = esPipelineDAO.get(indexTemplatePhy.getCluster(), indexTemplatePhy.getName());
            if (esPipelineProcessor == null) {
                // pipeline processor不存在，创建
                LOGGER.info("class=TemplatePipelineManagerImpl||method=syncPipeline||template={}||msg=pipeline not exist, recreate", indexTemplatePhy.getName());
                return createPipeline(templatePhyId);
            }
                // pipeline processor不一致（有变化），以新元数据创建
            if (notConsistent(indexTemplatePhy, indexTemplate, esPipelineProcessor)) {
                LOGGER.info("class=TemplatePipelineManagerImpl||method=syncPipeline||template={}||msg=doCreatePipeline", indexTemplatePhy.getName());
                return doCreatePipeline(indexTemplatePhy, indexTemplate, esPipelineProcessor.getThrottle().getInteger("rate_limit"));
            }

            return Result.buildSucc();
        } catch (Exception e) {
            LOGGER.warn("class=TemplatePipelineManagerImpl||method=syncPipeline||template={}||errMsg={}", indexTemplatePhy.getCluster(), e.getMessage(), e);
            return Result.buildFail("sync fail");
        }
    }

    @Override
    public Result<Void> deletePipeline(Integer templatePhyId) {
        IndexTemplatePhy indexTemplatePhy = indexTemplatePhyService.getTemplateById(templatePhyId.longValue());
        if (null == indexTemplatePhy) {
            return Result.buildFail("物理模板不存在");
        }

        if (!isTemplateSrvOpen(indexTemplatePhy.getLogicId())) {
            return Result.buildFail("未开启pipeLine服务");
        }

        try {
            return Result.build(ESOpTimeoutRetry.esRetryExecute("deletePipeline", RETRY_TIMES,
                    () -> esPipelineDAO.delete(indexTemplatePhy.getCluster(), indexTemplatePhy.getName())));
        } catch (Exception e) {
            LOGGER.error("class=PipelineManagerImpl||method=deletePipeline||template={}||errMsg={}", indexTemplatePhy.getName(), e.getMessage(), e);
            return Result.buildFail("delete fail");
        }
    }

    @Override
    public Result<Void> editFromTemplateLogic(IndexTemplate oldTemplate, IndexTemplate newTemplate) {
        if (!isTemplateSrvOpen(oldTemplate.getId())) {
            return Result.buildFail("未开启pipeLine服务");
        }

        boolean changed = AriusObjUtils.isChanged(newTemplate.getDateField(), oldTemplate.getDateField())
                || AriusObjUtils.isChanged(newTemplate.getDateFieldFormat(), oldTemplate.getDateFieldFormat())
                || AriusObjUtils.isChanged(newTemplate.getDateFormat(), oldTemplate.getDateFormat())
                || AriusObjUtils.isChanged(newTemplate.getExpireTime(), oldTemplate.getExpireTime())
                || AriusObjUtils.isChanged(newTemplate.getWriteRateLimit(), oldTemplate.getWriteRateLimit());

        boolean cyclicalRollChanged = oldTemplate.getExpression().endsWith("*")
                && !newTemplate.getExpression().endsWith("*");

        if (!changed && !cyclicalRollChanged) {
            LOGGER.info("class=PipelineManagerImpl||method=editFromTemplateLogic||msg=no changed||pipelineId={}", oldTemplate.getName());
            return Result.buildSucc();
        }

        String dateField = newTemplate.getDateField();
        String dateFieldFormat = newTemplate.getDateFieldFormat();
        String dateFormat = newTemplate.getDateFormat();

        Integer expireDay = newTemplate.getExpireTime();
        if (newTemplate.getHotTime() != null && newTemplate.getHotTime() > 0) {
            expireDay = newTemplate.getHotTime();
        } else if (oldTemplate.getHotTime() != null && oldTemplate.getHotTime() > 0) {
            expireDay = oldTemplate.getHotTime();
        }

        if (cyclicalRollChanged) {
            dateField = "";
            dateFieldFormat = "";
            dateFormat = "";
            expireDay = -1;
        }

        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getTemplateByLogicId(oldTemplate.getId());
        if (CollectionUtils.isEmpty(templatePhysicals)) {
            return Result.buildFail("物理模板不存在");
        }

        for (IndexTemplatePhy physical : templatePhysicals) {
            Integer rateLimit   = getDynamicRateLimit(physical);
            try {
                if (!esPipelineDAO.save(physical.getCluster(), physical.getName(),
                        dateField, dateFieldFormat, dateFormat, expireDay, rateLimit,
                        physical.getVersion(), newTemplate.getIdField(), newTemplate.getRoutingField())) {
                    return Result.buildFail("edit fail");
                }
            } catch (Exception e) {
                LOGGER.error("class=PipelineManagerImpl||method=editFromTemplateLogic||template={}||errMsg={}", physical.getName(), e.getMessage(), e);
                return Result.buildFail("edit fail");
            }
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Void> editFromTemplatePhysical(IndexTemplatePhy oldTemplate, IndexTemplatePhy newTemplate) {
        if (!isTemplateSrvOpen(oldTemplate.getLogicId())) {
            return Result.buildFail("未开启pipeLine服务");
        }

        if (!AriusObjUtils.isChanged(newTemplate.getVersion(), oldTemplate.getVersion())) {
            return Result.buildSucc();
        }

        IndexTemplate indexTemplate = indexTemplateService.getLogicTemplateById(oldTemplate.getLogicId());
        if (null == indexTemplate) {
            return Result.buildFail("逻辑模板不存在");
        }

        Integer rateLimit = getDynamicRateLimit(newTemplate);
        try {
            return Result.build(ESOpTimeoutRetry.esRetryExecute("editFromTemplatePhysical", RETRY_TIMES,
                    () -> esPipelineDAO.save(newTemplate.getCluster(), newTemplate.getName(), indexTemplate.getDateField(),
                            indexTemplate.getDateFieldFormat(), indexTemplate.getDateFormat(),
                            indexTemplate.getHotTime() > 0 ? indexTemplate.getHotTime() : indexTemplate.getExpireTime(),
                            rateLimit, newTemplate.getVersion(), indexTemplate.getIdField(),
                            indexTemplate.getRoutingField())));
        } catch (Exception e) {
            LOGGER.error("class=PipelineManagerImpl||method=editFromTemplatePhysical||template={}||errMsg={}", oldTemplate.getName(), e.getMessage(), e);
            return Result.buildFail("edit fail");
        }
    }

    @Override
    public Integer getRateLimit(Integer templatePhyId) {
        IndexTemplatePhy indexTemplatePhy = indexTemplatePhyService.getTemplateById(templatePhyId.longValue());
        if (null == indexTemplatePhy) {
            return null;
        }

        ESPipelineProcessor esPipelineProcessor = esPipelineDAO.get(indexTemplatePhy.getCluster(), indexTemplatePhy.getName());
        return null != esPipelineProcessor ? esPipelineProcessor.getThrottle().getInteger(RATE_LIMIT) : 0;
    }



    ///////////////////////////private method/////////////////////////////////////////////

    private boolean notConsistent(IndexTemplatePhy indexTemplatePhy, IndexTemplate logicTemplate, ESPipelineProcessor esPipelineProcessor) {
        if (StringUtils.isNotEmpty(logicTemplate.getDateField()) &&
                (!isDateFieldEqual(logicTemplate.getDateField(), esPipelineProcessor.getIndexTemplate().getString(DATE_FIELD)))) {
            LOGGER.info(
                    "class=PipelineManagerImpl||method=notConsistent||msg=dateField change||pipelineId={}||templateDateField={}||pipelineDateField={}",
                    logicTemplate.getName(), logicTemplate.getDateField(),
                    esPipelineProcessor.getIndexTemplate().getString(DATE_FIELD));
            return true;
        }

        if (StringUtils.isNotEmpty(logicTemplate.getDateFieldFormat()) &&
                (isDateFieldFormatChange(logicTemplate.getDateFieldFormat(), esPipelineProcessor.getIndexTemplate().getString(DATE_FIELD_FORMAT)))) {
            LOGGER.info(
                    "class=PipelineManagerImpl||method=notConsistent||msg=dateFieldFormat change||pipelineId={}||dateFieldFormat={}||dateField={}"
                            + "||pipelineDateFieldFormat={}",
                    logicTemplate.getName(), logicTemplate.getDateFieldFormat(),
                    logicTemplate.getDateField(),
                    esPipelineProcessor.getIndexTemplate().getString(DATE_FIELD_FORMAT));
            return true;
        }

        if (StringUtils.isNotEmpty(logicTemplate.getDateFormat()) &&
                (!logicTemplate.getDateFormat().equals(esPipelineProcessor.getIndexTemplate().getString(INDEX_NAME_FORMAT)))) {
            LOGGER.info("class=PipelineManagerImpl||method=notConsistent||msg=date format change||pipelineId={}||dateFormat={}"
                            + "||pipelineDateFormat={}",
                    logicTemplate.getName(), logicTemplate.getDateFormat(),
                    esPipelineProcessor.getIndexTemplate().getString(INDEX_NAME_FORMAT));
            return true;
        }

        if (isExpireDayChange(logicTemplate.getExpireTime(), logicTemplate.getHotTime(),
                esPipelineProcessor.getIndexTemplate().getInteger(EXPIRE_DAY))) {
            LOGGER.info("class=PipelineManagerImpl||method=notConsistent||msg=expireDay change||pipelineId={}||expireTime={}"
                            + "||hotTime={}||pipelineExpireDay={}",
                    logicTemplate.getName(), logicTemplate.getExpireTime(), logicTemplate.getHotTime(),
                    esPipelineProcessor.getIndexTemplate().getInteger(EXPIRE_DAY));
            return true;
        }

        if (!indexTemplatePhy.getVersion()
                .equals(esPipelineProcessor.getIndexTemplate().getInteger(INDEX_VERSION))) {
            LOGGER.info("class=PipelineManagerImpl||method=notConsistent||msg=version change||pipelineId={}||version={}" + "||pipelineVersion={}",
                    logicTemplate.getName(), indexTemplatePhy.getVersion(),
                    esPipelineProcessor.getIndexTemplate().getInteger(INDEX_VERSION));
            return true;
        }

        if (isRateLimitNoConsistent(indexTemplatePhy.fetchConfig(), esPipelineProcessor.getThrottle())) {
            LOGGER.info("class=PipelineManagerImpl||method=notConsistent||msg=rateLimit change||pipelineId={}||physicalConfig={}||throttle={}",
                    logicTemplate.getName(), indexTemplatePhy.getConfig(), esPipelineProcessor.getThrottle());
            return true;
        }

        return false;
    }

    /**
     * 比较日期字段是否一致
     *
     * @param logicWithPhysicalDateField 逻辑模板日期字段
     * @param pipelineDateField          pipeline日期字段
     * @return
     */
    private boolean isDateFieldEqual(String logicWithPhysicalDateField, String pipelineDateField) {
        if (StringUtils.isNotBlank(logicWithPhysicalDateField) && StringUtils.isNotBlank(pipelineDateField)) {
            return logicWithPhysicalDateField.equals(pipelineDateField);
        }
        return false;
    }

    /**
     * 校验日期字段格式是否改变
     *
     * @param dateFieldFormat         日期字段格式
     * @return
     */
    private boolean isDateFieldFormatChange(String dateFieldFormat, String pipelineDateFieldFormat) {
        if (MS_TIME_FIELD_PLATFORM_FORMAT.equals(dateFieldFormat)) {
            dateFieldFormat = MS_TIME_FIELD_ES_FORMAT;
        } else if (SECOND_TIME_FIELD_PLATFORM_FORMAT.equals(dateFieldFormat)) {
            dateFieldFormat = SECOND_TIME_FIELD_ES_FORMAT;
        }

        return !dateFieldFormat.equals(pipelineDateFieldFormat);
    }

    /**
     * 校验expire day是否改变
     *
     * @param expireTime        过期时间
     * @param hotTime           热保存天数
     * @param pipelineExpireDay pipeline过期天数
     * @return
     */
    private boolean isExpireDayChange(Integer expireTime, Integer hotTime, Integer pipelineExpireDay) {
        return ((hotTime > 0 && !pipelineExpireDay.equals(hotTime))
                || (hotTime <= 0 && !pipelineExpireDay.equals(expireTime)));
    }

    /**
     * 索引模板流控ES集群和MySQL元数据是否一致
     * @param config 物理模板配置
     * @param throttle 流控相关信息
     * @return
     */
    private boolean isRateLimitNoConsistent(IndexTemplatePhysicalConfig config, JSONObject throttle) {
        if (config == null || throttle == null) {
            return false;
        }

        return (config.getPipeLineRateLimit() != null
                && !config.getPipeLineRateLimit().equals(throttle.getInteger("rate_limit")));
    }


    /**
     * 根据逻辑模板更新物理模板的pipeline 配置
     * @param indexTemplatePhy
     * @param logicTemplate
     * @param rateLimit
     * @return
     */
    private Result<Void> doCreatePipeline(IndexTemplatePhy indexTemplatePhy, IndexTemplate logicTemplate, Integer rateLimit) {
        String cluster = indexTemplatePhy.getCluster();
        String pipelineId = indexTemplatePhy.getName();
        String dateField = logicTemplate.getDateField();
        String dateFieldFormat = logicTemplate.getDateFieldFormat();
        String dateFormat = logicTemplate.getDateFormat();

        Integer version = indexTemplatePhy.getVersion();
        String idField = logicTemplate.getIdField();
        String routingField = logicTemplate.getRoutingField();
        Integer expireDay = logicTemplate.getHotTime() > 0 ? logicTemplate.getHotTime() : logicTemplate.getExpireTime();

        LOGGER.info("class=PipelineManagerImpl||method=doCreatePipeline||cluster={}||pipelineId={}||dateField={}||dateFormat={}||expireDay={}||rateLimit={}||version={}", cluster, pipelineId, dateField, dateFormat, expireDay, rateLimit, version);

        // 保存限流值到DB
        saveRateLimitToDB(indexTemplatePhy, rateLimit);

        try {
            return Result.build(ESOpTimeoutRetry.esRetryExecute("doCreatePipeline", 3, () -> esPipelineDAO.save(cluster, pipelineId,
                    dateField, dateFieldFormat, dateFormat, expireDay, rateLimit, version, idField, routingField)));
        } catch (Exception e) {
            LOGGER.error("class=PipelineManagerImpl||method=doCreatePipeline||error", e);
            return Result.buildFail();
        }
    }

    private void saveRateLimitToDB(IndexTemplatePhy physical, Integer rateLimit) {
        // 保存数据库
        IndexTemplatePhysicalConfig physicalConfig = JSON.parseObject(physical.getConfig(), IndexTemplatePhysicalConfig.class);
        if (null == physicalConfig) {
            physicalConfig = new IndexTemplatePhysicalConfig();
        }

        physicalConfig.setPipeLineRateLimit(rateLimit);
        IndexTemplatePhyPO physicalPO = new IndexTemplatePhyPO();
        physicalPO.setId(physical.getId());
        physicalPO.setConfig(JSON.toJSONString(physicalConfig));

        // 避免出现死循环风险，这里直接使用DAO，历史原因
        indexTemplatePhyDAO.update(physicalPO);
    }


    private Integer getDynamicRateLimit(IndexTemplatePhy indexTemplatePhy) {
        Integer rateLimit = PIPELINE_RATE_LIMIT_MAX_VALUE;

        if (StringUtils.isNotBlank(indexTemplatePhy.getConfig())) {
            IndexTemplatePhysicalConfig physicalConfig = JSON.parseObject(indexTemplatePhy.getConfig(), IndexTemplatePhysicalConfig.class);
            if (null == physicalConfig) {
                return rateLimit;
            }

            if (null != physicalConfig.getManualPipeLineRateLimit() && physicalConfig.getManualPipeLineRateLimit() > 0) {
                rateLimit = physicalConfig.getManualPipeLineRateLimit();
            }

            if (null != physicalConfig.getPipeLineRateLimit()) {
                rateLimit = (physicalConfig.getPipeLineRateLimit() < rateLimit) ? physicalConfig.getPipeLineRateLimit() : rateLimit;
            }
        }

        return rateLimit;
    }
}
