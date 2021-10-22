package com.didichuxing.datachannel.arius.admin.core.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;

/**
 * @author didi
 */
@Component
public class CacheSwitch {

    private static final String    CONFIG_GROUP = "arius.cache.switch";

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    public boolean logicTemplateCacheEnable() {
        return ariusConfigInfoService.booleanSetting(CONFIG_GROUP, "logic.template.cache.enable", true);
    }

    public boolean logicTemplateQuotaUsageCacheEnable() {
        return ariusConfigInfoService.booleanSetting(CONFIG_GROUP, "logic.template.quota.usage.cache.enable", true);
    }

    public boolean physicalTemplateCacheEnable() {
        return ariusConfigInfoService.booleanSetting(CONFIG_GROUP, "physical.template.cache.enable", true);
    }

    public boolean clusterPhyCacheEnable() {
        return ariusConfigInfoService.booleanSetting(CONFIG_GROUP, "cluster.phy.cache.enable", true);
    }

    public boolean clusterLogicCacheEnable() {
        return ariusConfigInfoService.booleanSetting(CONFIG_GROUP, "cluster.logic.cache.enable", true);
    }
}
