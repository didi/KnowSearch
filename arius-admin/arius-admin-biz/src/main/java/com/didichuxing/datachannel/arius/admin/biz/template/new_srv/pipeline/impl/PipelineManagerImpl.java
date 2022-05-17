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
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESPipelineDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhyDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESPipelineDAO.*;
import static com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESPipelineDAO.INDEX_VERSION;

/**
 * @author chengxiang, d06679
 * @date 2022/5/13
 */
@Service
public class PipelineManagerImpl extends BaseTemplateSrvImpl implements PipelineManager {

    private static final ILog LOGGER = LogFactory.getLog(PipelineManagerImpl.class);

    @Autowired
    private ESPipelineDAO esPipelineDAO;

    @Autowired
    private IndexTemplatePhyDAO indexTemplatePhyDAO;


    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.TEMPLATE_PIPELINE;
    }

    @Override
    public Result<Void> isTemplateSrvAvailable(Integer logicTemplateId) {
        return Result.buildSucc();
    }

    @Override
    public Result<Void> createPipeline(Integer logicTemplateId) {
        if (!isTemplateSrvOpen(logicTemplateId)) {
            return Result.buildFail("未开启pipeLine服务");
        }

        IndexTemplate indexTemplate = indexTemplateService.getLogicTemplateById(logicTemplateId);
        Integer rateLimit = getDynamicQuotaRateLimit(indexTemplate);
        return Result.buildFail();
    }

    @Override
    public Boolean syncPipeline(Integer logicTemplateId) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean deletePipeline(Integer logicTemplateId) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean editFromTemplateLogic(IndexTemplate oldTemplate, IndexTemplate newTemplate) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean editFromTemplatePhysical(IndexTemplatePhy oldTemplate, IndexTemplatePhy newTemplate,
                                            IndexTemplateWithPhyTemplates logicWithPhysical) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean editRateLimitByPercent(IndexTemplatePhy indexTemplatePhysicalInfo, Integer percent) {
        return Boolean.FALSE;
    }

    @Override
    public Integer getRateLimit(IndexTemplatePhy indexTemplatePhysicalMasterInfo) {
        return 0;
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
                (isDateFieldFormatChange(logicTemplate.getDateFieldFormat(), logicTemplate.getDateField(),
                        esPipelineProcessor.getIndexTemplate().getString(DATE_FIELD_FORMAT)))) {
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


    private boolean doCreatePipeline(IndexTemplatePhy indexTemplatePhy, IndexTemplate logicTemplate, Integer rateLimit) throws ESOperateException {
        String cluster = indexTemplatePhy.getCluster();
        String pipelineId = indexTemplatePhy.getName();
        String dateField = logicTemplate.getDateField();
        String dateFieldFormat = logicTemplate.getDateFieldFormat();
        String dateFormat = logicTemplate.getDateFormat();

        Integer version = indexTemplatePhy.getVersion();
        String idField = logicTemplate.getIdField();
        String routingField = logicTemplate.getRoutingField();
        Integer expireDay = logicTemplate.getHotTime() > 0 ? logicTemplate.getHotTime()
                : logicTemplate.getExpireTime();

        LOGGER.info("class=PipelineManagerImpl||method=createPipeline||cluster={}||pipelineId={}||dateField={}||dateFormat={}||expireDay={}||rateLimit={}||version={}", cluster, pipelineId, dateField, dateFormat, expireDay, rateLimit, version);

        // 保存限流值到DB
        saveRateLimitToDB(indexTemplatePhy, rateLimit);

        return ESOpTimeoutRetry.esRetryExecute("createPipeline", 3, () -> esPipelineDAO.save(cluster, pipelineId,
                dateField, dateFieldFormat, dateFormat, expireDay, rateLimit, version, idField, routingField));
    }

    private void saveRateLimitToDB(IndexTemplatePhy physical, Integer rateLimit) {
        // 保存数据库
        IndexTemplatePhysicalConfig physicalConfig = JSON.parseObject(physical.getConfig(),
                IndexTemplatePhysicalConfig.class);
        if (physicalConfig == null) {
            physicalConfig = new IndexTemplatePhysicalConfig();
        }

        physicalConfig.setPipeLineRateLimit(rateLimit);

        IndexTemplatePhyPO physicalPO = new IndexTemplatePhyPO();
        physicalPO.setId(physical.getId());
        physicalPO.setConfig(JSON.toJSONString(physicalConfig));

        // 避免出现死循环风险，这里直接使用DAO
        indexTemplatePhyDAO.update(physicalPO);
    }


    //todo: 等待yunan quota 开发
    private Integer getDynamicQuotaRateLimit(IndexTemplate template) {
        return 0;
    }
}
