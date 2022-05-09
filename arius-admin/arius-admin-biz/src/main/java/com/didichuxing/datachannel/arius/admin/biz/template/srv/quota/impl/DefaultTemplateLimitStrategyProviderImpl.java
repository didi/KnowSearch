package com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.impl;

import static com.didichuxing.datachannel.arius.admin.common.bean.common.LogicResourceConfig.QUOTA_CTL_ALL;
import static com.didichuxing.datachannel.arius.admin.common.bean.common.LogicResourceConfig.QUOTA_CTL_DISK;
import static com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateLimitStrategy.TPS_ADJUST_PERCENT_MIN;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfoWithLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.common.GetTemplateLimitStrategyContext;
import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateLimitStrategy;
import com.didichuxing.datachannel.arius.admin.common.constant.quota.QuotaCtlStrategyEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.PhysicalTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.util.PercentUtils;
import com.didichuxing.datachannel.arius.admin.biz.extend.foctory.TemplateLimitStrategyProvider;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author d06679
 * @date 2019-08-22
 */
@Service("defaultTemplateLimitStrategyProvider")
public class DefaultTemplateLimitStrategyProviderImpl implements TemplateLimitStrategyProvider {

    private static final ILog       LOGGER = LogFactory.getLog(DefaultTemplateLimitStrategyProviderImpl.class);

    @Autowired
    private TemplateQuotaManager templateQuotaManager;

    @Autowired
    private TemplatePhyService templatePhyService;

    /**
     * 扩展模板限流接口
     *
     * @param cluster  集群
     * @param template 模板名字
     * @param interval 时间间隔
     * @param context 计算上下文
     * @return 限流策略
     */
    @Override
    public TemplateLimitStrategy provide(String cluster, String template, long interval,
                                         GetTemplateLimitStrategyContext context) {

        IndexTemplatePhyInfoWithLogic templatePhysicalWithLogic = templatePhyService
            .getTemplateWithLogicByClusterAndName(cluster, template);
        if (templatePhysicalWithLogic == null) {
            return TemplateLimitStrategy.buildDefault();
        }

        if (!templateQuotaManager.enableClt(templatePhysicalWithLogic.getLogicId())) {
            return TemplateLimitStrategy.buildDefault();
        }

        PhysicalTemplateQuotaUsage usage = templateQuotaManager.getPhyTemplateQuotaUsage(cluster, template, interval,
            context);

        if (usage == null) {
            return TemplateLimitStrategy.buildDefault();
        }

        String ctlRange = templateQuotaManager.getCtlRange(templatePhysicalWithLogic.getCluster(),
            templatePhysicalWithLogic.getRack());

        TemplateLimitStrategy strategy = new TemplateLimitStrategy();
        if (usage.getActualDiskG() > usage.getQuotaDiskG()
            && (QUOTA_CTL_DISK.equals(ctlRange) || QUOTA_CTL_ALL.equals(ctlRange))) {
            // 如果磁盘超标禁止写入
            strategy.setAdjustStrategy(QuotaCtlStrategyEnum.FORBID_SINK.getCode());
            strategy.setTpsAdjustPercent(TPS_ADJUST_PERCENT_MIN);
        } else if (usage.getActualCpuCount() > usage.getQuotaCpuCount() && QUOTA_CTL_ALL.equals(ctlRange)) {
            // 限流
            strategy.setAdjustStrategy(QuotaCtlStrategyEnum.DECREASE.getCode());
            strategy.setTpsAdjustPercent(
                PercentUtils.get((usage.getQuotaCpuCount() - usage.getActualCpuCount()) / usage.getActualCpuCount()));
        } else if (usage.getActualCpuCount() < usage.getQuotaCpuCount()) {
            // 涨流
            strategy.setAdjustStrategy(QuotaCtlStrategyEnum.INCREASE.getCode());
            strategy.setTpsAdjustPercent(
                PercentUtils.get((usage.getQuotaCpuCount() - usage.getActualCpuCount()) / usage.getActualCpuCount()));
        } else {
            strategy.setAdjustStrategy(QuotaCtlStrategyEnum.NOT_ADJUST.getCode());
            strategy.setTpsAdjustPercent(0);
        }

        LOGGER.info("class=DefaultTemplateLimitStrategyProviderImpl||method=defaultTemplateLimitStrategyProvider.provide||cluster={}||template={}||strategy={}",
            cluster, template, strategy);

        return strategy;
    }
}
