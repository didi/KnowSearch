package com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.impl;

import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminESOpRetryConstants;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySettings;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.impl.TemplateLogicServiceImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.setting.TemplatePhySettingsManager;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.common.MappingConfig;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.template.TemplateConfig;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author wangshu
 * @date 2020/08/24
 */
@Service
public class TemplatePhySettingsManagerImpl implements TemplatePhySettingsManager {

    private static final ILog sLogger = LogFactory.getLog(TemplateLogicServiceImpl.class);

    @Autowired
    private ESTemplateService esTemplateService;

    @Override
    public boolean validTemplateSettings(String cluster, String template,
                                         IndexTemplatePhySettings settings) throws ESOperateException {
        if (StringUtils.isBlank(cluster) || StringUtils.isBlank(template) || settings == null) {
            return false;
        }

        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setMappings(new MappingConfig());
        templateConfig.setSetttings(settings.flatSettings());
        templateConfig.setTemplate(template);

        return esTemplateService.syncCheckTemplateConfig(cluster, template, templateConfig,
            AdminESOpRetryConstants.DEFAULT_RETRY_COUNT);
    }

    @Override
    public IndexTemplatePhySettings fetchTemplateSettings(String cluster, String template) {
        TemplateConfig templateConfig = esTemplateService.syncGetTemplateConfig(cluster, template);
        if (templateConfig != null) {
            return new IndexTemplatePhySettings(templateConfig.getSetttings());
        }

        return null;
    }

    @Override
    public boolean mergeTemplateSettings(String cluster, String template,
                                         Map<String, String> settings) throws AdminOperateException {

        TemplateConfig templateConfig = esTemplateService.syncGetTemplateConfig(cluster, template);
        if (templateConfig == null) {
            sLogger.info("class=TemplatePhySettingsManagerImpl||method=updateSetting||"
                         + "msg=templateNotExists||cluster={}||template={}",
                cluster, template);
            throw new AdminOperateException("模版不存在，template:" + template);
        }

        IndexTemplatePhySettings templateSettings = new IndexTemplatePhySettings(templateConfig.getSetttings());
        templateConfig.setSetttings(templateSettings.merge(settings));

        if (!esTemplateService.syncCheckTemplateConfig(cluster, fetchPreCreateTemplateName(template), templateConfig,
            AdminESOpRetryConstants.DEFAULT_RETRY_COUNT)) {
            sLogger.info("class=TemplatePhySettingsManagerImpl||method=updateSetting||"
                         + "msg=checkTemplateConfigFail||cluster={}||templateName={}||templateConfig={}",
                cluster, fetchPreCreateTemplateName(template), templateConfig);
            throw new AdminOperateException("非法模板settings: " + settings);
        }

        return esTemplateService.syncUpsertSetting(cluster, template, settings,
            AdminESOpRetryConstants.DEFAULT_RETRY_COUNT);
    }

    /**************************************** private method ****************************************************/
    /**
     * 获取预创建模板名称
     * @param templateName 原始模板名称
     * @return
     */
    private String fetchPreCreateTemplateName(String templateName) {
        if (StringUtils.isBlank(templateName)) {
            return AdminConstant.ES_CHECK_TEMPLATE_INDEX_PREFIX;
        }

        return templateName;
    }
}
