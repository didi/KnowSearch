package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.event.template.physical.metadata.PhysicalMetaDataModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.physical.metadata.PhysicalTemplateAliasModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.physical.metadata.PhysicalTemplatePropertiesTypesModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.physical.metadata.PhysicalTemplateSettingsModifyEvent;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class TemplatePhyMetaChangedListener implements ApplicationListener<PhysicalMetaDataModifyEvent> {

    private static final ILog LOGGER = LogFactory.getLog( TemplatePhyMetaChangedListener.class);

    @Override
    public void onApplicationEvent(PhysicalMetaDataModifyEvent modifyEvent) {
        if (modifyEvent instanceof PhysicalTemplateAliasModifyEvent) {
            PhysicalTemplateAliasModifyEvent aliasModifyEvent = (PhysicalTemplateAliasModifyEvent) modifyEvent;
            LOGGER
                .info("class=PhysicalTemplateMetaChangedListener||method=onApplicationEvent||msg=templateAliasesChanged" +
                                "||cluster={}||templateName={}||beforeUpdateAliases={}||afterUpdateAliases={}",
                    aliasModifyEvent.getCluster(), aliasModifyEvent.getTemplateName(),
                    JSON.toJSONString(aliasModifyEvent.getBeforeUpdateAlias()),
                    JSON.toJSONString(aliasModifyEvent.getAfterUpdateAlias()));

        } else if (modifyEvent instanceof PhysicalTemplatePropertiesTypesModifyEvent) {
            PhysicalTemplatePropertiesTypesModifyEvent typesModifyModifyEvent = (PhysicalTemplatePropertiesTypesModifyEvent) modifyEvent;

            LOGGER
                .info("class=PhysicalTemplateMetaChangedListener||method=onApplicationEvent||msg=templateTypesChanged" +
                                "||cluster={}||templateName={}||beforeUpdateTypes={}||afterUpdateTypes={}",
                    typesModifyModifyEvent.getCluster(), typesModifyModifyEvent.getTemplateName(),
                    JSON.toJSONString(typesModifyModifyEvent.getBeforeUpdateTypes()),
                    JSON.toJSONString(typesModifyModifyEvent.getAfterUpdateTypes()));
        } else if (modifyEvent instanceof PhysicalTemplateSettingsModifyEvent) {
            PhysicalTemplateSettingsModifyEvent settingsModifyEvent = (PhysicalTemplateSettingsModifyEvent) modifyEvent;
            LOGGER
                .info("class=PhysicalTemplateMetaChangedListener||method=onApplicationEvent||msg=templateSettingsChanged" +
                                "||cluster={}||templateName={}||beforeChangedSettings={}||afterChangedSettings={}",
                    settingsModifyEvent.getCluster(), settingsModifyEvent.getTemplateName(),
                    JSON.toJSONString(settingsModifyEvent.getBeforeUpdateSettings()),
                    JSON.toJSONString(settingsModifyEvent.getAfterUpdateSettings()));
        }
    }

}