package com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateAction;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ESPipelineProcessor;
import com.didichuxing.datachannel.arius.admin.client.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLogicPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplatePhysicalPO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESPipelineDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateLogicDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhysicalDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.TEMPLATE_CONFIG;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.PIPELINE_RATE_LIMIT_MAX_VALUE;
import static com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser.SYSTEM;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_PIPELINE;
import static com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESPipelineDAO.*;

/**
 * @author d06679
 * <p>
 * 关于RateLimit值的说明：
 * 1、在新建pipeline的时候初始化一个值
 * 2、只有动态限流会修改这个人值
 * 3、其他情况只涉及获取
 * <p>
 * 该方法中加事务主要是为了防止MySQL主从延时导致数据不一致，后期优化时需要将该方法中需要的数据通过spring事务机制传递过来
 * @date 2019-09-03
 */
@Service
public class TemplatePipelineManagerImpl extends BaseTemplateSrv implements TemplatePipelineManager {

    private static final ILog           LOGGER = LogFactory.getLog( TemplatePipelineManagerImpl.class);

    @Autowired
    private ESPipelineDAO               esPipelineDAO;

    @Autowired
    private IndexTemplatePhysicalDAO    indexTemplatePhysicalDAO;

    @Autowired
    private TemplateAction              templateAction;

    @Autowired
    private IndexTemplateLogicDAO       indexTemplateLogicDAO;

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_PIPELINE;
    }

    @Override
    public Result<Void> repairPipeline(Integer logicId) throws ESOperateException {
        IndexTemplateLogicWithPhyTemplates logicWithPhysical = templateLogicService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (logicWithPhysical == null) {
            return Result.buildFail("索引模板不存在");
        }

        if (!isTemplateSrvOpen(logicWithPhysical.getPhysicals())) {
            return Result.buildFail("物理集群没有开启" + templateService().getServiceName());
        }

        for (IndexTemplatePhy templatePhysical : logicWithPhysical.getPhysicals()) {
            boolean result = createPipeline(templatePhysical, logicWithPhysical);
            if (!result) {
                return Result.buildFail(String.format("更新pipeline失败，name=%s, cluster=%s", templatePhysical.getName(),
                        templatePhysical.getCluster()));
            }
        }

        TemplateLogicPO editTemplate = indexTemplateLogicDAO.getById(logicId);
        editTemplate.setIngestPipeline(logicWithPhysical.getName());

        int row = indexTemplateLogicDAO.update(editTemplate);
        if (row != 1) {
            return Result.buildFail(String.format("更新模板pipeline字段失败，row=%d", row));
        }

        return Result.build(true);
    }

    /**
     * 同步pipeline
     *
     * @param indexTemplatePhysical 物理模板
     * @param logicWithPhysical     逻辑模板
     */
    @Override
    public void syncPipeline(IndexTemplatePhy indexTemplatePhysical,
                             IndexTemplateLogicWithPhyTemplates logicWithPhysical) {
        if (!isTemplateSrvOpen(indexTemplatePhysical.getCluster())) {
            return;
        }

        try {
            ESPipelineProcessor esPipelineProcessor = esPipelineDAO.get(indexTemplatePhysical.getCluster(),
                indexTemplatePhysical.getName());

            if (esPipelineProcessor == null) {
                // pipeline processor不存在，创建
                LOGGER.info("class=TemplatePipelineManagerImpl||method=syncPipeline||template={}||msg=pipeline not exist, recreate",
                    indexTemplatePhysical.getName());
                createPipeline(indexTemplatePhysical, logicWithPhysical);
            } else {
                // pipeline processor不一致（有变化），以新元数据创建
                if (notConsistent(indexTemplatePhysical, logicWithPhysical, esPipelineProcessor)) {
                    LOGGER.info("class=TemplatePipelineManagerImpl||method=syncPipeline||template={}||msg=doCreatePipeline",
                        indexTemplatePhysical.getName());
                    doCreatePipeline(indexTemplatePhysical, logicWithPhysical,
                        esPipelineProcessor.getThrottle().getInteger("rate_limit"));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("class=TemplatePipelineManagerImpl||method=syncPipeline||template={}||errMsg={}", indexTemplatePhysical.getName(), e.getMessage(),
                e);
        }
    }

    /**
     * 创建
     *
     * @param indexTemplatePhysical 物理模板
     * @param logicWithPhysical     逻辑模板
     * @return true/false
     */
    @Override
    public boolean createPipeline(IndexTemplatePhy indexTemplatePhysical,
                                  IndexTemplateLogicWithPhyTemplates logicWithPhysical) throws ESOperateException {
        if (!isTemplateSrvOpen(indexTemplatePhysical.getCluster())) {
            return false;
        }

        Integer rateLimit = getDynamicQuotaRateLimit(indexTemplatePhysical);

        return doCreatePipeline(indexTemplatePhysical, logicWithPhysical, rateLimit);
    }

    /**
     * 删除
     *
     * @param indexTemplatePhysical 物理模板
     * @return true/false
     */
    @Override
    public boolean deletePipeline(IndexTemplatePhy indexTemplatePhysical) throws ESOperateException {
        if (!isTemplateSrvOpen(indexTemplatePhysical.getCluster())) {
            return false;
        }

        return ESOpTimeoutRetry.esRetryExecute("deletePipeline", 3,
            () -> esPipelineDAO.delete(indexTemplatePhysical.getCluster(), indexTemplatePhysical.getName()));
    }

    /**
     * 修改逻辑字段
     *
     * @param oldTemplate 逻辑模板
     * @param newTemplate 逻辑模板
     * @return true/false
     */
    @Override
    public boolean editFromTemplateLogic(IndexTemplateLogic oldTemplate, IndexTemplateLogic newTemplate) {

        boolean changed = AriusObjUtils.isChanged(newTemplate.getDateField(), oldTemplate.getDateField())
                          || AriusObjUtils.isChanged(newTemplate.getDateFieldFormat(), oldTemplate.getDateFieldFormat())
                          || AriusObjUtils.isChanged(newTemplate.getDateFormat(), oldTemplate.getDateFormat())
                          || AriusObjUtils.isChanged(newTemplate.getExpireTime(), oldTemplate.getExpireTime())
                          || AriusObjUtils.isChanged(newTemplate.getQuota(), oldTemplate.getQuota())
                          || AriusObjUtils.isChanged(newTemplate.getIdField(), oldTemplate.getIdField())
                          || AriusObjUtils.isChanged(newTemplate.getRoutingField(), oldTemplate.getRoutingField())
                          || AriusObjUtils.isChanged(newTemplate.getWriteRateLimit(), oldTemplate.getWriteRateLimit());

        boolean cyclicalRollChanged = oldTemplate.getExpression().endsWith("*")
                                      && !newTemplate.getExpression().endsWith("*");

        if (!changed && !cyclicalRollChanged) {
            LOGGER.info("class=TemplatePipelineManagerImpl||method=editFromTemplateLogic||msg=no changed||pipelineId={}", oldTemplate.getName());
            return true;
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

        LOGGER.info("class=TemplatePipelineManagerImpl||method=editFromTemplateLogic||msg=no changed||pipelineId={}||"
                    + "oldHotTime={}||newHotTime={}||expireDay={}",
            oldTemplate.getName(), oldTemplate.getHotTime(), newTemplate.getHotTime(), expireDay);

        if (cyclicalRollChanged) {
            dateField = "";
            dateFieldFormat = "";
            dateFormat = "";
            expireDay = -1;
        }

        List<IndexTemplatePhy> templatePhysicals = templatePhyService.getTemplateByLogicId(oldTemplate.getId());

        if (!isTemplateSrvOpen(templatePhysicals)) {
            return false;
        }

        return handleTemplatePhysicals(newTemplate, dateField, dateFieldFormat, dateFormat, expireDay, templatePhysicals);
    }

    /**
     * 修改物理字段
     *
     * @param oldTemplate 物理模板
     * @return true/false
     */
    @Override
    public boolean editFromTemplatePhysical(IndexTemplatePhy oldTemplate, IndexTemplatePhy newTemplate,
                                            IndexTemplateLogicWithPhyTemplates logicWithPhysical) throws ESOperateException {
        boolean changed = AriusObjUtils.isChanged(newTemplate.getVersion(), oldTemplate.getVersion());

        if (!changed) {
            LOGGER.info("class=TemplatePipelineManagerImpl||method=editFromTemplatePhysical||msg=no changed||pipelineId={}||version={}",
                oldTemplate.getName(), oldTemplate.getVersion());
            return true;
        }

        LOGGER.info("class=TemplatePipelineManagerImpl||method=editFromTemplatePhysical||cluster={}||pipelineId={}||version={}", newTemplate.getCluster(),
            newTemplate.getName(), newTemplate.getVersion());

        Integer rateLimit = getManualRateLimit(newTemplate);

        return ESOpTimeoutRetry.esRetryExecute("editFromTemplatePhysical", 3,
            () -> esPipelineDAO.save(newTemplate.getCluster(), newTemplate.getName(), logicWithPhysical.getDateField(),
                logicWithPhysical.getDateFieldFormat(), logicWithPhysical.getDateFormat(),
                logicWithPhysical.getHotTime() > 0 ? logicWithPhysical.getHotTime() : logicWithPhysical.getExpireTime(),
                rateLimit, newTemplate.getVersion(), logicWithPhysical.getIdField(),
                logicWithPhysical.getRoutingField()));
    }

    @Override
    public boolean editRateLimitByPercent(IndexTemplatePhy templatePhysical,
                                          Integer percent) throws ESOperateException {
        if (!isTemplateSrvOpen(templatePhysical.getCluster())) {
            return false;
        }

        if (percent == 0) {
            return true;
        }

        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
                .getLogicTemplateWithPhysicalsById(templatePhysical.getLogicId());

        Integer manualRateLimit = getManualRateLimit(templatePhysical);
        Integer rateLimitOld    = getDynamicQuotaRateLimit(templatePhysical);

        int rateLimitNew = 1 + (int) (rateLimitOld * ((100.0 + percent) / 100.0));

        rateLimitNew = (rateLimitNew < 1) ? 1 : rateLimitNew;
        rateLimitNew = (rateLimitNew > manualRateLimit) ? manualRateLimit : rateLimitNew;

        LOGGER.info("class=TemplatePipelineManagerImpl||method=editRateLimitByPercent||cluster={}||pipelineId={}||percent={}||rateLimit={}->{}",
            templatePhysical.getCluster(), templatePhysical.getName(), percent, rateLimitOld, rateLimitNew);

        int finalRateLimitNew = rateLimitNew;

        if (rateLimitOld != rateLimitNew) {
            // 保存到DB
            saveRateLimitToDB(templatePhysical, finalRateLimitNew);

            boolean esSuccess = ESOpTimeoutRetry.esRetryExecute("editFromTemplatePhysical", 3,
                () -> esPipelineDAO.save(templatePhysical.getCluster(), templatePhysical.getName(),
                    templateLogicWithPhysical.getDateField(), templateLogicWithPhysical.getDateFieldFormat(),
                    templateLogicWithPhysical.getDateFormat(),
                    templateLogicWithPhysical.getHotTime() > 0 ? templateLogicWithPhysical.getHotTime()
                        : templateLogicWithPhysical.getExpireTime(),
                    finalRateLimitNew, templatePhysical.getVersion(), templateLogicWithPhysical.getIdField(),
                    templateLogicWithPhysical.getRoutingField()));

            operateRecordService.save(TEMPLATE_CONFIG, EDIT, templatePhysical.getId(),
                    "rateLimitOld:" + rateLimitOld + ",rateLimitNew:" + rateLimitNew, SYSTEM.getDesc());

            return esSuccess;
        }

        return true;
    }

    @Override
    public Integer getRateLimit(IndexTemplatePhy indexTemplatePhysicalMaster) {
        ESPipelineProcessor esPipelineProcessor = esPipelineDAO.get(indexTemplatePhysicalMaster.getCluster(), indexTemplatePhysicalMaster.getName());
        return null != esPipelineProcessor ? esPipelineProcessor.getThrottle().getInteger(RATE_LIMIT) : 0;
    }

    /**************************************** private method ****************************************************/
    private void saveRateLimitToDB(IndexTemplatePhy physical, Integer rateLimit) {
        // 保存数据库
        IndexTemplatePhysicalConfig physicalConfig = JSON.parseObject(physical.getConfig(),
            IndexTemplatePhysicalConfig.class);
        if (physicalConfig == null) {
            physicalConfig = new IndexTemplatePhysicalConfig();
        }

        physicalConfig.setPipeLineRateLimit(rateLimit);

        TemplatePhysicalPO physicalPO = new TemplatePhysicalPO();
        physicalPO.setId(physical.getId());
        physicalPO.setConfig(JSON.toJSONString(physicalConfig));

        // 避免出现死循环风险，这里直接使用DAO
        indexTemplatePhysicalDAO.update(physicalPO);
    }

    private Integer getManualRateLimit(IndexTemplatePhysicalConfig physicalConfig) {
        Integer rateLimit;

        if(null == physicalConfig.getManualPipeLineRateLimit() || physicalConfig.getManualPipeLineRateLimit() < 0){
            rateLimit = PIPELINE_RATE_LIMIT_MAX_VALUE;
        }else {
            rateLimit = physicalConfig.getManualPipeLineRateLimit();
        }

        return rateLimit;
    }

    private Integer getManualRateLimit(IndexTemplatePhy templatePhysical) {
        Integer rateLimit = PIPELINE_RATE_LIMIT_MAX_VALUE;

        if (StringUtils.isNotBlank(templatePhysical.getConfig())) {
            IndexTemplatePhysicalConfig physicalConfig = JSON.parseObject(templatePhysical.getConfig(),
                    IndexTemplatePhysicalConfig.class);

            rateLimit = getManualRateLimit(physicalConfig);
        }

        return rateLimit;
    }

    private Integer getDynamicQuotaRateLimit(IndexTemplatePhy templatePhysical) {
        Integer rateLimit = PIPELINE_RATE_LIMIT_MAX_VALUE;

        if (StringUtils.isNotBlank(templatePhysical.getConfig())) {
            IndexTemplatePhysicalConfig physicalConfig = JSON.parseObject(templatePhysical.getConfig(),
                    IndexTemplatePhysicalConfig.class);
            rateLimit = getManualRateLimit(templatePhysical);

            if (physicalConfig.getPipeLineRateLimit() != null) {
                rateLimit = (physicalConfig.getPipeLineRateLimit() < rateLimit) ? physicalConfig.getPipeLineRateLimit(): rateLimit;
            }
        }

        return rateLimit;
    }

    private boolean doCreatePipeline(IndexTemplatePhy indexTemplatePhysical,
                                     IndexTemplateLogicWithPhyTemplates logicWithPhysical,
                                     Integer rateLimit) throws ESOperateException {
        String cluster = indexTemplatePhysical.getCluster();
        String pipelineId = indexTemplatePhysical.getName();
        String dateField = logicWithPhysical.getDateField();
        String dateFieldFormat = logicWithPhysical.getDateFieldFormat();
        String dateFormat = logicWithPhysical.getDateFormat();

        Integer version = indexTemplatePhysical.getVersion();
        String idField = logicWithPhysical.getIdField();
        String routingField = logicWithPhysical.getRoutingField();
        Integer expireDay = logicWithPhysical.getHotTime() > 0 ? logicWithPhysical.getHotTime()
            : logicWithPhysical.getExpireTime();

        LOGGER.info(
            "class=TemplatePipelineManagerImpl||method=createPipeline||cluster={}||pipelineId={}||dateField={}||dateFormat={}||expireDay={}||rateLimit={}||version={}",
            cluster, pipelineId, dateField, dateFormat, expireDay, rateLimit, version);

        // 保存限流值到DB
        saveRateLimitToDB(indexTemplatePhysical, rateLimit);

        return ESOpTimeoutRetry.esRetryExecute("createPipeline", 3, () -> esPipelineDAO.save(cluster, pipelineId,
            dateField, dateFieldFormat, dateFormat, expireDay, rateLimit, version, idField, routingField));
    }

    private boolean notConsistent(IndexTemplatePhy indexTemplatePhysical,
                                  IndexTemplateLogicWithPhyTemplates logicWithPhysical,
                                  ESPipelineProcessor esPipelineProcessor) {

        if (StringUtils.isNotEmpty(logicWithPhysical.getDateField()) &&
                (!isDateFieldEqual(logicWithPhysical.getDateField(), esPipelineProcessor.getIndexTemplate().getString(DATE_FIELD)))) {
            LOGGER.info(
                "class=TemplatePipelineManagerImpl||method=notConsistent||msg=dateField change||pipelineId={}||templateDateField={}||pipelineDateField={}",
                logicWithPhysical.getName(), logicWithPhysical.getDateField(),
                esPipelineProcessor.getIndexTemplate().getString(DATE_FIELD));
            return true;
        }

        if (StringUtils.isNotEmpty(logicWithPhysical.getDateFieldFormat()) &&
                (isDateFieldFormatChange(logicWithPhysical.getDateFieldFormat(), logicWithPhysical.getDateField(),
                        esPipelineProcessor.getIndexTemplate().getString(DATE_FIELD_FORMAT)))) {
            LOGGER.info(
                "class=TemplatePipelineManagerImpl||method=notConsistent||msg=dateFieldFormat change||pipelineId={}||dateFieldFormat={}||dateField={}"
                        + "||pipelineDateFieldFormat={}",
                logicWithPhysical.getName(), logicWithPhysical.getDateFieldFormat(),
                logicWithPhysical.getDateField(),
                esPipelineProcessor.getIndexTemplate().getString(DATE_FIELD_FORMAT));
            return true;
        }

        if (StringUtils.isNotEmpty(logicWithPhysical.getDateFormat()) &&
                (!logicWithPhysical.getDateFormat().equals(esPipelineProcessor.getIndexTemplate().getString(INDEX_NAME_FORMAT)))) {
            LOGGER.info("class=TemplatePipelineManagerImpl||method=notConsistent||msg=date format change||pipelineId={}||dateFormat={}"
                        + "||pipelineDateFormat={}",
                logicWithPhysical.getName(), logicWithPhysical.getDateFormat(),
                esPipelineProcessor.getIndexTemplate().getString(INDEX_NAME_FORMAT));
            return true;
        }

        if (isExpireDayChange(logicWithPhysical.getExpireTime(), logicWithPhysical.getHotTime(),
            esPipelineProcessor.getIndexTemplate().getInteger(EXPIRE_DAY))) {
            LOGGER.info("class=TemplatePipelineManagerImpl||method=notConsistent||msg=expireDay change||pipelineId={}||expireTime={}"
                        + "||hotTime={}||pipelineExpireDay={}",
                logicWithPhysical.getName(), logicWithPhysical.getExpireTime(), logicWithPhysical.getHotTime(),
                esPipelineProcessor.getIndexTemplate().getInteger(EXPIRE_DAY));
            return true;
        }

        if (!indexTemplatePhysical.getVersion()
            .equals(esPipelineProcessor.getIndexTemplate().getInteger(INDEX_VERSION))) {
            LOGGER.info("class=TemplatePipelineManagerImpl||method=notConsistent||msg=version change||pipelineId={}||version={}" + "||pipelineVersion={}",
                logicWithPhysical.getName(), indexTemplatePhysical.getVersion(),
                esPipelineProcessor.getIndexTemplate().getInteger(INDEX_VERSION));
            return true;
        }

        if (isRateLimitNoConsistent(indexTemplatePhysical.fetchConfig(), esPipelineProcessor.getThrottle())) {
            LOGGER.info("class=TemplatePipelineManagerImpl||method=notConsistent||msg=rateLimit change||pipelineId={}||physicalConfig={}||throttle={}",
                logicWithPhysical.getName(), indexTemplatePhysical.getConfig(), esPipelineProcessor.getThrottle());
            return true;
        }

        return false;
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
     * 校验日期字段格式是否改变
     *
     * @param dateFieldFormat         日期字段格式
     * @param dateField               日期格式
     * @param pipelineDateFieldFormat pipeline日期字段格式
     * @return
     */
    private boolean isDateFieldFormatChange(String dateFieldFormat, String dateField, String pipelineDateFieldFormat) {
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

    private boolean handleTemplatePhysicals(IndexTemplateLogic newTemplate, String dateField,
                                            String dateFieldFormat, String dateFormat,
                                            Integer expireDay, List<IndexTemplatePhy> templatePhysicals) {
        boolean succ = true;
        for (IndexTemplatePhy physical : templatePhysicals) {

            String cluster      = physical.getCluster();
            String name         = physical.getName();
            Integer rateLimit   = getManualRateLimit(physical);

            try {
                String  finalDateField          = dateField;
                String  finalDateFieldFormat    = dateFieldFormat;
                String  finalDateFormat         = dateFormat;
                Integer finalExpireDay          = expireDay;

                if (ESOpTimeoutRetry.esRetryExecute("editFromTemplateLogic", 5,
                        () -> esPipelineDAO.save(cluster, name, finalDateField, finalDateFieldFormat, finalDateFormat,
                                finalExpireDay, rateLimit, physical.getVersion(), newTemplate.getIdField(),
                                newTemplate.getRoutingField()))) {
                    LOGGER.info(
                            "class=TemplatePipelineManagerImpl||method=editFromTemplateLogic||msg=succ||cluster={}||pipelineId={}||dateField={}||dateFormat={}||expireDay={}",
                            cluster, name, dateField, dateFormat, expireDay);
                } else {
                    LOGGER.info(
                            "class=TemplatePipelineManagerImpl||method=editFromTemplateLogic||msg=fail||cluster={}||pipelineId={}||dateField={}||dateFormat={}||expireDay={}",
                            cluster, name, dateField, dateFormat, expireDay);
                    succ = false;
                }
            } catch (Exception e) {
                LOGGER.error(
                        "class=TemplatePipelineManagerImpl||method=editFromTemplateLogic||errMsg={}||cluster={}||pipelineId={}||dateField={}||dateFormat={}||expireDay={}",
                        e.getMessage(), cluster, name, dateField, dateFormat, expireDay, e);
                succ = false;
            }
        }
        return succ;
    }
}
