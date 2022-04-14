package com.didichuxing.datachannel.arius.admin.biz.listener;

import static com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum.TEMPLATE_ALIASES_CHANGED;
import static com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum.TEMPLATE_SETTINGS_CHANGED;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.event.template.physical.metadata.PhysicalMetaDataModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.physical.metadata.PhysicalTemplateAliasModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.physical.metadata.PhysicalTemplatePropertiesTypesModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.physical.metadata.PhysicalTemplateSettingsModifyEvent;
import com.didichuxing.datachannel.arius.admin.core.notify.info.template.TemplateAliasesChangedNotifyInfo;
import com.didichuxing.datachannel.arius.admin.core.notify.info.template.TemplatePropertiesTypesChangedNotifyInfo;
import com.didichuxing.datachannel.arius.admin.core.notify.info.template.TemplateSettingsChangedNotifyInfo;
import com.didichuxing.datachannel.arius.admin.core.notify.service.NotifyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

@Component
public class TemplatePhyMetaChangedListener implements ApplicationListener<PhysicalMetaDataModifyEvent> {

    private static final ILog LOGGER = LogFactory.getLog( TemplatePhyMetaChangedListener.class);

    @Autowired
    private NotifyService     notifyService;

    @Override
    public void onApplicationEvent(PhysicalMetaDataModifyEvent modifyEvent) {
        if (modifyEvent instanceof PhysicalTemplateAliasModifyEvent) {
            PhysicalTemplateAliasModifyEvent aliasModifyEvent = (PhysicalTemplateAliasModifyEvent) modifyEvent;
            TemplateAliasesChangedNotifyInfo changedNotifyInfo = new TemplateAliasesChangedNotifyInfo(
                aliasModifyEvent.getAppId(), aliasModifyEvent.getTemplateName(),
                aliasModifyEvent.getBeforeUpdateAlias(), aliasModifyEvent.getAfterUpdateAlias());

            LOGGER
                .info("class=PhysicalTemplateMetaChangedListener||method=onApplicationEvent||msg=templateAliasesChanged" +
                                "||cluster={}||templateName={}||beforeUpdateAliases={}||afterUpdateAliases={}",
                    aliasModifyEvent.getCluster(), aliasModifyEvent.getTemplateName(),
                    JSON.toJSONString(aliasModifyEvent.getBeforeUpdateAlias()),
                    JSON.toJSONString(aliasModifyEvent.getAfterUpdateAlias()));

            notifyService.send(TEMPLATE_ALIASES_CHANGED, changedNotifyInfo, aliasModifyEvent.getReceivers());

        } else if (modifyEvent instanceof PhysicalTemplatePropertiesTypesModifyEvent) {
            PhysicalTemplatePropertiesTypesModifyEvent typesModifyModifyEvent = (PhysicalTemplatePropertiesTypesModifyEvent) modifyEvent;

            TemplatePropertiesTypesChangedNotifyInfo changedNotifyInfo = new TemplatePropertiesTypesChangedNotifyInfo(
                typesModifyModifyEvent.getAppId(), typesModifyModifyEvent.getTemplateName(),
                typesModifyModifyEvent.getBeforeUpdateTypes(), typesModifyModifyEvent.getAfterUpdateTypes());

            LOGGER
                .info("class=PhysicalTemplateMetaChangedListener||method=onApplicationEvent||msg=templateTypesChanged" +
                                "||cluster={}||templateName={}||beforeUpdateTypes={}||afterUpdateTypes={}",
                    typesModifyModifyEvent.getCluster(), typesModifyModifyEvent.getTemplateName(),
                    JSON.toJSONString(typesModifyModifyEvent.getBeforeUpdateTypes()),
                    JSON.toJSONString(typesModifyModifyEvent.getAfterUpdateTypes()));

            notifyService.send(TEMPLATE_SETTINGS_CHANGED, changedNotifyInfo, typesModifyModifyEvent.getReceivers());

        } else if (modifyEvent instanceof PhysicalTemplateSettingsModifyEvent) {
            PhysicalTemplateSettingsModifyEvent settingsModifyEvent = (PhysicalTemplateSettingsModifyEvent) modifyEvent;
            TemplateSettingsChangedNotifyInfo notifyInfo = new TemplateSettingsChangedNotifyInfo(
                settingsModifyEvent.getAppId(), settingsModifyEvent.getTemplateName(),
                settingsModifyEvent.getBeforeUpdateSettings(), settingsModifyEvent.getAfterUpdateSettings());

            LOGGER
                .info("class=PhysicalTemplateMetaChangedListener||method=onApplicationEvent||msg=templateSettingsChanged" +
                                "||cluster={}||templateName={}||beforeChangedSettings={}||afterChangedSettings={}",
                    settingsModifyEvent.getCluster(), settingsModifyEvent.getTemplateName(),
                    JSON.toJSONString(settingsModifyEvent.getBeforeUpdateSettings()),
                    JSON.toJSONString(settingsModifyEvent.getAfterUpdateSettings()));

            notifyService.send(TEMPLATE_SETTINGS_CHANGED, notifyInfo, settingsModifyEvent.getReceivers());
        }
    }

}
