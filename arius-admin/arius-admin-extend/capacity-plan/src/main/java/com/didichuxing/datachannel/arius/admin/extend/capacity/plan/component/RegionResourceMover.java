package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.component;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.MILLIS_PER_DAY;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.TemplateColdManager;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateMetaMetric;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.common.util.RackUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common.CapacityPlanConfig;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common.CapacityPlanRegionContext;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author d06679
 * @date 2019-06-26
 */
@Component
public class RegionResourceMover {

    private static final ILog       LOGGER                           = LogFactory.getLog(RegionResourceMover.class);

    private static final String     INDEX_GROUP_NAME                 = "index.group.name";
    private static final String     INDEX_GROUP_FACTOR               = "index.group.factor";
    private static final String     INDEX_TEMPLATE_NAME              = "index.template";

    private static final String     CAPACITY_PLAN_CONFIG_GROUP       = "capacity.plan.config.group";
    private static final String     CAPACITY_PLAN_HAS_PLUGIN_CLUSTER = "capacity.plan.has.plugin.cluster";

    @Autowired
    private TemplateLogicService    templateLogicService;

    @Autowired
    private TemplatePhyService templatePhyService;

    @Autowired
    private TemplatePhyManager      templatePhyManager;

    @Autowired
    private TemplateColdManager templateColdManager;

    @Autowired
    private ESTemplateService       esTemplateService;

    @Autowired
    private AriusConfigInfoService  ariusConfigInfoService;

    @Autowired
    private RegionResourceManager   regionResourceManager;

    /**
     * 扩容
     * @param regionPlanContext 上下文
     * @param increaseRacks 扩容的rack
     * @return true/false
     */
    public boolean increase(CapacityPlanRegionContext regionPlanContext, List<String> increaseRacks) {
        String tgtRack = regionPlanContext.getRegion().getRacks() + "," + String.join(",", increaseRacks);
        return moveShardInner(tgtRack, regionPlanContext.getTemplateMetaMetrics(), true);
    }

    /**
     * 缩容
     * @param regionPlanContext 上下文
     * @param decreaseRacks 缩容的rack
     * @return true/false
     */
    public boolean decrease(CapacityPlanRegionContext regionPlanContext, List<String> decreaseRacks) {
        String tgtRack = RackUtils.removeRacks(regionPlanContext.getRegion().getRacks(),
            Sets.newHashSet(decreaseRacks));
        return moveShardInner(tgtRack, regionPlanContext.getTemplateMetaMetrics(), true);
    }

    /**
     * 调整每个模板的factor group
     * @param context 计算上下文
     */
    public boolean saveTemplateCapacityConfig(CapacityPlanRegionContext context) throws ESOperateException {

        // 更新模板setting
        Set<String> hasPluginClusterSet = Sets.newHashSet(ariusConfigInfoService
            .stringSetting(CAPACITY_PLAN_CONFIG_GROUP, CAPACITY_PLAN_HAS_PLUGIN_CLUSTER, "").split(","));
        if (!hasPluginClusterSet.contains(context.getRegion().getClusterName())) {
            LOGGER.info("method=saveTemplateCapacityConfig||cluster={}||msg=no plugin",
                context.getRegion().getClusterName());
            return true;
        }

        Date now = new Date();
        boolean succ = true;

        for (TemplateMetaMetric templateMetaMetric : context.getTemplateMetaMetrics()) {
            try {
                String group = "region-" + context.getRegion().getRegionId()
                               + (StringUtils.isNotBlank(templateMetaMetric.getDateFormat())
                                   ? AriusDateUtils.date2Str(now, templateMetaMetric.getDateFormat())
                                   : "");
                Double factor = computeTemplateFactor(templateMetaMetric, context.getRegion().getConfig());

                // 更新模板factor到数据库
                IndexTemplatePhy templatePhysical = templatePhyService
                    .getTemplateById(templateMetaMetric.getPhysicalId());
                templateLogicService.upsertTemplateShardFactor(templatePhysical.getLogicId(), factor,
                    AriusUser.CAPACITY_PLAN.getDesc());

                Map<String, String> setting = Maps.newHashMap();
                setting.put(INDEX_GROUP_NAME, group);
                setting.put(INDEX_GROUP_FACTOR, String.valueOf(factor));
                setting.put(INDEX_TEMPLATE_NAME, templateMetaMetric.getTemplateName());

                if (esTemplateService.syncUpsertSetting(templateMetaMetric.getCluster(),
                    templateMetaMetric.getTemplateName(), setting, 10)) {
                    LOGGER.info(
                        "method=saveTemplateCapacityConfig||template={}||group={}||factor={}||msg=save to es succ",
                        templateMetaMetric.getTemplateName(), group, factor);
                } else {
                    succ = false;
                    LOGGER.warn(
                        "method=saveTemplateCapacityConfig||template={}||group={}||factor={}||msg=save to es fail",
                        templateMetaMetric.getTemplateName(), group, factor);
                }
            } catch (Exception e) {
                succ = false;
                LOGGER.warn("method=saveTemplateCapacityConfig||template={}||errMsg={}",
                    templateMetaMetric.getTemplateName(), e.getMessage(), e);
            }
        }

        return succ;
    }

    /**
     * 向上调整模板的factor
     * @param context 模板列表
     */
    public void raiseTemplateFactor(CapacityPlanRegionContext context) {
        for (TemplateMetaMetric templateMetaMetric : context.getTemplateMetaMetrics()) {
            IndexTemplatePhy templatePhysical = templatePhyService
                .getTemplateById(templateMetaMetric.getPhysicalId());
            templateLogicService.updateTemplateShardFactorIfGreater(templatePhysical.getLogicId(),
                computeTemplateFactor(templateMetaMetric, context.getRegion().getConfig()),
                AriusUser.CAPACITY_PLAN.getDesc());

            // TODO ZHZ 修改今天索引的factor
        }
    }

    /**
     * moveShard
     * @param region region
     * @param moveIndex
     * @return true/false
     */
    public boolean moveShard(CapacityPlanRegion region, boolean moveIndex) {
        CapacityPlanConfig regionConfig = region.getConfig();
        List<TemplateMetaMetric> templateMetaMetrics = regionResourceManager.getRegionTemplateMetrics(region,
            regionConfig.getPlanRegionResourceDays() * MILLIS_PER_DAY, regionConfig);
        return moveShardInner(region.getRacks(), templateMetaMetrics, moveIndex);
    }

    /***************************************** private method ****************************************************/

    /**
     * moveshard
     * @param tgtRack rack
     * @param templateMetaMetrics 模板
     * @param moveIndex
     * @return true/false
     */
    private boolean moveShardInner(String tgtRack, List<TemplateMetaMetric> templateMetaMetrics, boolean moveIndex) {
        boolean success = true;
        for (TemplateMetaMetric templateMetaMetric : templateMetaMetrics) {

            try {
                Result updateTemplateResult = templatePhyManager.editTemplateRackWithoutCheck(
                    templateMetaMetric.getPhysicalId(), tgtRack, AriusUser.CAPACITY_PLAN.getDesc(), 20);
                if (updateTemplateResult.failed()) {
                    LOGGER.error(
                        "class=RegionResourceMover||method=moveShardInner||errMsg={}||physicalId={}||tgtRack={}",
                        updateTemplateResult.getMessage(), templateMetaMetric.getPhysicalId(), tgtRack);
                    success = false;
                } else {
                    LOGGER.info("method=moveShard||physicalId={}||tgtRack={}", templateMetaMetric.getPhysicalId(),
                        tgtRack);
                }
            } catch (Exception e) {
                LOGGER.error("class=RegionResourceMover||method=moveShardInner||errMsg={}||physicalId={}||tgtRack={}",
                    e.getMessage(), templateMetaMetric.getPhysicalId(), tgtRack, e);
                success = false;
            }

            if (moveIndex) {
                // 更新索引的rack
                try {
                    if (templateColdManager.updateHotIndexRack(templateMetaMetric.getPhysicalId(), tgtRack, 20)) {
                        LOGGER.info("method=moveShard||physicalId={}||tgtRack={}||msg=update succ",
                            templateMetaMetric.getPhysicalId(), tgtRack);
                    } else {
                        LOGGER.error(
                            "class=RegionResourceMover||method=moveShardInner||errMsg=fail||physicalId={}||tgtRack={}",
                            templateMetaMetric.getPhysicalId(), tgtRack);
                        success = false;
                    }
                } catch (Exception e) {
                    LOGGER.error(
                        "class=RegionResourceMover||method=moveShardInner||errMsg={}||physicalId={}||tgtRack={}||expression={}",
                        e.getMessage(), templateMetaMetric.getPhysicalId(), tgtRack, templateMetaMetric.getExpression(),
                        e);
                    success = false;
                }
            }

        }

        return success;
    }

    /**
     * 计算每个模板的factor
     * @param templateMetaMetric 模板指标
     * @param config 配置
     * @return factor
     */
    private Double computeTemplateFactor(TemplateMetaMetric templateMetaMetric, CapacityPlanConfig config) {

        // TODO ZHZ 需要考虑quota的量

        double factorByDisk = templateMetaMetric.getMaxIndexSizeG() / templateMetaMetric.getShardNum()
                              / templateMetaMetric.getReplicaNum() / config.getCostDiskPerShardG();
        double factorByCpu = templateMetaMetric.getCombinedCpuCount() / templateMetaMetric.getShardNum()
                             / templateMetaMetric.getReplicaNum() / config.getCostCpuPerShard();

        LOGGER.info("method=computeShardFactor||templateName={}||factorByDisk={}||factorByCpu={}",
            templateMetaMetric.getTemplateName(), factorByDisk, factorByCpu);

        double factor = Math.max(factorByDisk, factorByCpu);

        if (factor > 5.0) {
            factor = 5.0;
        }

        if (factor < 0.01) {
            factor = 0.01;
        }

        return factor;
    }

    /**
     * 集群模板的shard个数
     * @param templateMetaMetric 指标
     * @param config 配置
     * @return shardNum
     */
    private Integer computeTemplateShardNum(TemplateMetaMetric templateMetaMetric, CapacityPlanConfig config) {
        int shardByDisk = (int) Math.ceil(templateMetaMetric.getMaxIndexSizeG() / AdminConstant.G_PER_SHARD);
        int shardByDocCount = (int) Math.ceil(templateMetaMetric.getMaxIndexDocCount() / config.getDocCountPerShard());
        int shard = Math.max(shardByDisk, shardByDocCount);

        if (shard < 1) {
            shard = 1;
        }

        return shard;
    }
}
