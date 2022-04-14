package com.didichuxing.datachannel.arius.admin.biz.template.srv.limit;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.TemplatePipelineManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.biz.extend.intfc.ExtendServiceFactory;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.common.*;
import com.didichuxing.datachannel.arius.admin.client.constant.quota.QuotaCtlStrategyEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.util.PercentUtils;
import com.didichuxing.datachannel.arius.admin.biz.extend.intfc.TemplateLimitStrategyProvider;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyStatisManager;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

import static com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateLimitStrategy.TPS_ADJUST_PERCENT_MAX;
import static com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateLimitStrategy.TPS_ADJUST_PERCENT_MIN;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_LIMIT_W;

/**
 * 招行内部去掉基于es节点负载的动态限流策略
 * @modifer zhaoqingrong
 * @date 2021-06-22
 */

/**
 * 实现基于es节点负载的动态限流
 * @author d06679
 * @date 2019-08-22
 */
@Service("dynamicLimitTemplateLimitService")
public class TemplateLimitManagerImpl extends BaseTemplateSrv
                                      implements TemplateLimitStrategyProvider, TemplateLimitManager {

    private static final ILog             LOGGER                    = LogFactory.getLog( TemplateLimitManagerImpl.class);

    private static final Double           RACK_CPU_USED_PERCENT_AVG = 80.0;

    @Autowired
    private TemplateQuotaManager            templateQuotaManager;

    @Autowired
    private ClusterNodeManager              clusterNodeManager;

    @Autowired
    private TemplatePhyStatisManager        templatePhyStatisManager;

    @Autowired
    private TemplatePipelineManager         templatePipelineManager;

    @Autowired
    private ExtendServiceFactory            extendServiceFactory;

    @Autowired
    private ESIndexService                  esIndexService;

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_LIMIT_W;
    }

    /**
     * 停止模板写入
     *
     * @param physicalId 模板ID
     * @return result
     */
    @Override
    public Result<Void> stopIndexWrite(Long physicalId) throws ESOperateException {
        IndexTemplatePhy templatePhysical = templatePhyService.getTemplateById(physicalId);
        if (templatePhysical == null) {
            return Result.buildNotExist("模板不存在");
        }

        if (!isTemplateSrvOpen(templatePhysical.getCluster())) {
            return Result.buildNotExist(templatePhysical.getCluster() + "集群没有开启" + templateServiceName());
        }

        return blockIndexWrite(templatePhysical.getCluster(),
            templatePhyService.getMatchNoVersionIndexNames(physicalId), true);
    }

    /**
     * 停止模板写入
     *
     * @param physicalId 模板ID
     * @return result
     */
    @Override
    public Result<Void> startIndexWrite(Long physicalId) throws ESOperateException {
        IndexTemplatePhy templatePhysical = templatePhyService.getTemplateById(physicalId);
        if (templatePhysical == null) {
            return Result.buildNotExist("模板不存在");
        }
        return blockIndexWrite(templatePhysical.getCluster(),
            templatePhyService.getMatchNoVersionIndexNames(physicalId), false);
    }

    @Override
    public Result<Void> blockIndexWrite(String cluster, List<String> indices, boolean block) throws ESOperateException {
        if (CollectionUtils.isEmpty(indices)) {
            return Result.buildNotExist("没有索引");
        }

        if (!isTemplateSrvOpen(cluster)) {
            return Result.buildNotExist(cluster + "集群没有开启" + templateService().getServiceName());
        }

        return Result.build(esIndexService.syncBatchBlockIndexWrite(cluster, indices, block, 3));
    }

    /**
     * 获取模板管控策略
     * <p>
     * 1、default-provider，负责quota管控
     * 2、RateLimit-provider 负责判断当前是否被限流
     * 3、Nodeload-provider 负责节点负载的限流
     * 4、每个provider的参数增加一个context参数，记录计算过程的参数，避免重复获取
     *
     * @param cluster  集群
     * @param template 模板
     * @param interval interval
     * @param context  计算上下文
     * @return result
     */
    @Override
    public TemplateLimitStrategy getTemplateLimitStrategy(String cluster, String template, long interval,
                                                          GetTemplateLimitStrategyContext context) {
        List<TemplateLimitStrategyProvider> strategyProviders = extendServiceFactory
            .getAll(TemplateLimitStrategyProvider.class);

        List<TemplateLimitStrategy> strategies = Lists.newArrayList();
        for (TemplateLimitStrategyProvider provider : strategyProviders) {
            TemplateLimitStrategy strategy = provider.provide(cluster, template, interval, context);
            if (strategy != null) {
                strategies.add(strategy);
            }
        }

        if (CollectionUtils.isEmpty(strategies)) {
            return TemplateLimitStrategy.buildDefault();
        }

        TemplateLimitStrategy mergedStrategy = TemplateLimitStrategy.merge(strategies);

        if (mergedStrategy.getTpsAdjustPercent() < TPS_ADJUST_PERCENT_MIN) {
            mergedStrategy.setTpsAdjustPercent(TPS_ADJUST_PERCENT_MIN);
        }

        if (mergedStrategy.getTpsAdjustPercent() > TPS_ADJUST_PERCENT_MAX) {
            mergedStrategy.setTpsAdjustPercent(TPS_ADJUST_PERCENT_MAX);
        }

        return mergedStrategy;
    }

    @Override
    public boolean adjustPipelineRateLimit(Integer logicId) {
        List<IndexTemplatePhy> physicals = templatePhyService.getTemplateByLogicId(logicId);
        if (CollectionUtils.isEmpty(physicals)) {
            LOGGER.info("method=adjustPipelineRateLimit||logicId={}||msg=adjustPipeLineRateLimit no template", logicId);
            return true;
        }

        long interval = 10 * 60 * 1000L;

        int succCount = 0;
        for (IndexTemplatePhy physical : physicals) {
            try {
                GetTemplateLimitStrategyContext getTemplateLimitStrategyContext = new GetTemplateLimitStrategyContext();
                TemplateLimitStrategy strategy;

                if(templateQuotaManager.enableClt(physical.getLogicId())){
                    strategy = getTemplateLimitStrategy(physical.getCluster(), physical.getName(),
                            interval, getTemplateLimitStrategyContext);

                    if (strategy.getTpsAdjustPercent() > 0 && !getTemplateLimitStrategyContext.isRateLimited()) {
                        PhysicalTemplateTpsMetric templateTpsMetric = getTemplateLimitStrategyContext
                                .getPhysicalTemplateTpsMetric();
                        if (templateTpsMetric == null) {
                            LOGGER.info(
                                    "class=ESClusterPhyServiceImpl||method=adjustPipeLineRateLimit||logicId={}||template={}||msg=not limit and templateTpsMetric is null",
                                    logicId, physical.getName());
                        } else {
                            LOGGER.info(
                                    "class=ESClusterPhyServiceImpl||method=adjustPipeLineRateLimit||logicId={}||template={}||currentTps={}||currentFailTps={}||msg=not limit",
                                    logicId, physical.getName(),
                                    getTemplateLimitStrategyContext.getPhysicalTemplateTpsMetric().getCurrentTps(),
                                    getTemplateLimitStrategyContext.getPhysicalTemplateTpsMetric().getCurrentFailCount());
                        }

                        succCount++;
                        continue;
                    }

                    if (templatePipelineManager.editRateLimitByPercent(physical, strategy.getTpsAdjustPercent())) {
                        succCount++;
                    } else {
                        LOGGER.warn(
                                "class=ESClusterPhyServiceImpl||method=adjustPipeLineRateLimit||logicId={}||template={}||msg=adjustPipeLineRateLimit fail",
                                logicId, physical.getName());
                    }
                }
            } catch (Exception e) {
                LOGGER.error(
                    "class=ESClusterPhyServiceImpl||method=adjustPipeLineRateLimit||errMsg={}||logicId={}||template={}||stackTrace={}",
                    e.getMessage(), logicId, physical.getName(), JSON.toJSONString(e.getStackTrace()), e);
            }
        }

        return succCount == physicals.size();
    }

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

        IndexTemplatePhy templatePhysical = templatePhyService.getTemplateByClusterAndName(cluster, template);

        if (templatePhysical == null || !templateQuotaManager.enableClt(templatePhysical.getLogicId())) {
            return TemplateLimitStrategy.buildDefault();
        }

        long now = System.currentTimeMillis();
        long startTime = now - interval;

        Result<PhysicalTemplateTpsMetric> templateTpsMetricResult = templatePhyStatisManager
            .getTemplateTpsMetric(cluster, template, startTime, now);
        if (templateTpsMetricResult.failed()) {
            LOGGER.warn(
                "method=TemplateLimitManagerImpl.provider||msg=getTemplateMetricsFail||cluster={}||template={}||failMsg={}",
                cluster, template, templateTpsMetricResult.getMessage());
            return TemplateLimitStrategy.buildDefault();
        }
        context.setPhysicalTemplateTpsMetric(templateTpsMetricResult.getData());

        Result<List<RackMetaMetric>> rackMetaMetricResult = clusterNodeManager.metaAndMetric(cluster,
            Lists.newArrayList(templatePhysical.getRack().split(",")));
        if (rackMetaMetricResult.failed()) {
            LOGGER.warn(
                "method=TemplateLimitManagerImpl.provider||msg=getRackMetaMetricFail||cluster={}||template={}||rack={}||failMsg={}",
                cluster, template, templatePhysical.getRack(), rackMetaMetricResult.getMessage());
            return TemplateLimitStrategy.buildDefault();
        }
        context.setRackMetaMetrics(rackMetaMetricResult.getData());

        Double templateTpsMax = templateTpsMetricResult.getData().getMaxTps();
        Double templateTpsCurrent = templateTpsMetricResult.getData().getCurrentTps();
        Double rackCpuUsedPercentAvg = getRackCpuUsedPercentAvg(rackMetaMetricResult.getData());

        if (templateTpsMax == null || templateTpsCurrent == null) {
            return TemplateLimitStrategy.buildDefault();
        }

        if (templateTpsMax <= 0 || templateTpsCurrent <= 0 || rackCpuUsedPercentAvg <= 0) {
            return TemplateLimitStrategy.buildDefault();
        }

        if (templateTpsCurrent <= templateTpsMax) {
            return TemplateLimitStrategy.buildDefault();
        }

        TemplateLimitStrategy strategy = new TemplateLimitStrategy();

        if (rackCpuUsedPercentAvg <= RACK_CPU_USED_PERCENT_AVG) {
            strategy.setAdjustStrategy(QuotaCtlStrategyEnum.INCREASE.getCode());
        } else {
            strategy.setAdjustStrategy(QuotaCtlStrategyEnum.DECREASE.getCode());
        }

        strategy.setTpsAdjustPercent(
            PercentUtils.get((RACK_CPU_USED_PERCENT_AVG - rackCpuUsedPercentAvg) / rackCpuUsedPercentAvg));

        LOGGER.info(
            "method=TemplateLimitManagerImpl.provider||cluster={}||template={}||templateTpsMax={}||templateTpsCurrent={}||rackCpuUsedPercentAvg={}||strategy={}",
            cluster, template, templateTpsMax, templateTpsCurrent, rackCpuUsedPercentAvg, strategy);

        return strategy;
    }

    /**************************************** private method ****************************************************/
    private Double getRackCpuUsedPercentAvg(List<RackMetaMetric> metaMetrics) {
        if (metaMetrics.isEmpty()) {
            return 0.0;
        }

        Double sum = 0.0;
        for (RackMetaMetric metaMetric : metaMetrics) {
            sum += metaMetric.getCpuUsedPercent();
        }

        return sum / metaMetrics.size();
    }
}
