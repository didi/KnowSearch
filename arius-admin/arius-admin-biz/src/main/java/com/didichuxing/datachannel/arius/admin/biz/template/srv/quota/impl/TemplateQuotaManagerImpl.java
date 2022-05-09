package com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplateAction;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyStatisManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import com.didichuxing.datachannel.arius.admin.common.constant.quota.NodeSpecifyEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.common.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.ESTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.LogicTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.PhysicalTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.quota.ESTemplateQuotaUsagePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.quota.ESTemplateQuotaUsageRecordPO;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.event.quota.TemplateCpuOutOfQuotaEvent;
import com.didichuxing.datachannel.arius.admin.common.event.quota.TemplateDiskOutOfQuotaEvent;
import com.didichuxing.datachannel.arius.admin.common.event.quota.TemplateDiskUsageWarnEvent;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.CacheSwitch;
import com.didichuxing.datachannel.arius.admin.core.component.QuotaTool;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.quota.ESTemplateQuotaUsageDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.quota.ESTemplateQuotaUsageRecordDAO;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.bean.common.LogicResourceConfig.QUOTA_CTL_ALL;
import static com.didichuxing.datachannel.arius.admin.common.bean.common.LogicResourceConfig.QUOTA_CTL_DISK;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_COMMON_GROUP;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_QUOTA;
import static com.didichuxing.datachannel.arius.admin.core.component.QuotaTool.QUOTA_DISK_WARN_THRESHOLD;
import static com.didichuxing.datachannel.arius.admin.core.component.QuotaTool.TEMPLATE_QUOTA_MIN;

/**
 * 索引quota服务实现
 * @author d06679
 * @date 2019/4/25
 */
@Service
public class TemplateQuotaManagerImpl extends BaseTemplateSrv implements TemplateQuotaManager {

    private static final int                          ODIN_STEP                        = 15 * 60;

    @Autowired
    private QuotaTool                                 quotaTool;

    @Autowired
    private TemplatePhyStatisManager                  templatePhyStatisManager;

    @Autowired
    private TemplateAction                            templateAction;

    @Autowired
    private TemplateQuotaManager                      templateQuotaManager;

    @Autowired
    private AriusConfigInfoService                    ariusConfigInfoService;

    @Autowired
    private ESTemplateQuotaUsageDAO                   esTemplateQuotaUsageDAO;

    @Autowired
    private ESTemplateQuotaUsageRecordDAO             esTemplateQuotaUsageRecordDAO;

    @Autowired
    private CacheSwitch                               cacheSwitch;

    private Cache<String, List<ESTemplateQuotaUsage>> templateLogicQuotaUsageListCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(2).build();

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_QUOTA;
    }

    /**
     * 获取模板quota 及当前quota使用量
     * @param cluster  集群
     * @param template 模板
     * @param interval interval
     * @param context 上下文
     * @return result
     */
    @Override
    public PhysicalTemplateQuotaUsage getPhyTemplateQuotaUsage(String cluster, String template, Long interval,
                                                               GetTemplateQuotaUsageContext context) {
        IndexTemplatePhy templatePhysical = templatePhyService.getTemplateByClusterAndName(cluster, template);

        if (templatePhysical == null) {
            LOGGER.warn(
                "class=TemplateQuotaManagerImpl||method=getWithUsage||cluster={}||template={}||msg=template not exist!",
                cluster, template);
            return null;
        }

        IndexTemplateConfig templateConfig = indexTemplateInfoService.getTemplateConfig(templatePhysical.getLogicId());
        if (templateConfig != null && Objects.equals(templateConfig.getDynamicLimitEnable(), AdminConstant.NO)) {
            LOGGER.warn(
                "class=TemplateQuotaManagerImpl||method=getWithUsage||cluster={}||template={}||msg=template config not limit!",
                cluster, template);
            return null;
        }

        return getPhyTemplateQuotaUsageInner(templatePhysical, interval, context);
    }

    /**
     * 获取模板quota 及当前quota使用量
     *
     * @param logicId id
     * @param interval interval
     * @return result
     */
    @Override
    public LogicTemplateQuotaUsage getLogicTemplateQuotaUsage(Integer logicId, Long interval) {
        IndexTemplateInfoWithPhyTemplates logicWithPhysical = this.indexTemplateInfoService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (logicWithPhysical == null) {
            return null;
        }

        LogicTemplateQuotaUsage quotaUsage = new LogicTemplateQuotaUsage();
        quotaUsage.setLogicId(logicId);
        quotaUsage.setTemplate(logicWithPhysical.getName());
        quotaUsage.setActualCpuCount(0.0);
        quotaUsage.setActualDiskG(0.0);
        quotaUsage.setQuotaCpuCount(0.0);
        quotaUsage.setQuotaDiskG(0.0);

        for (IndexTemplatePhy physical : logicWithPhysical.getPhysicals()) {
            PhysicalTemplateQuotaUsage usage = getPhyTemplateQuotaUsageInner(physical, interval,
                new GetTemplateQuotaUsageContext());

            quotaUsage.setActualCpuCount(quotaUsage.getActualCpuCount() + usage.getActualCpuCount());
            quotaUsage.setQuotaCpuCount(quotaUsage.getQuotaCpuCount() + usage.getQuotaCpuCount());

            quotaUsage.setActualDiskG(quotaUsage.getActualDiskG() + usage.getActualDiskG());
            quotaUsage.setQuotaDiskG(quotaUsage.getQuotaDiskG() + usage.getQuotaDiskG());
        }

        return quotaUsage;
    }

    /**
     * 根据模板quota的使用情况发布spring事件
     *
     * @param logicId logicId
     * @return true/false
     */
    @Override
    public boolean controlAndPublish(Integer logicId) {
        // 获取逻辑模板的quota和使用情况
        LogicTemplateQuotaUsage logicTemplateQuotaUsage = getLogicTemplateQuotaUsage(logicId, ODIN_STEP * 1000L);
        if (logicTemplateQuotaUsage == null) {
            return false;
        }

        // 利用率保存es
        templateQuotaManager.save(buildESTemplateQuotaUsage(logicTemplateQuotaUsage));

        // 发布事件
        publishQuotaEvent(logicTemplateQuotaUsage);

        return true;
    }

    /**
     * 计算模板的费用
     *
     * @param nodeSpecify 节点规格
     * @param quota       quota
     * @return 费用 元/月
     */
    @Override
    public Double computeCostByQuota(Integer nodeSpecify, Double quota) {
        return quotaTool.computeCostByQuota(nodeSpecify, quota);
    }

    /**
     * 根据磁盘计算成本
     *
     * @param diskG      磁盘
     * @param resourceId resourceId
     * @return 成本
     */
    @Override
    public Double computeCostByDisk(Double diskG, Long resourceId) {
        if (diskG == null) {
            return 0.0;
        }

        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(resourceId);
        if (clusterLogic == null) {
            return 0.0;
        }

        LogicResourceConfig resourceConfig = clusterLogicService
            .genClusterLogicConfig(clusterLogic.getConfigJson());

        double quota = quotaTool.getQuotaCountByDisk(NodeSpecifyEnum.DOCKER.getCode(), diskG, TEMPLATE_QUOTA_MIN)
                       * resourceConfig.getReplicaNum();

        return quotaTool.computeCostByQuota(NodeSpecifyEnum.DOCKER.getCode(), quota);
    }

    @Override
    public boolean enableClt(Integer logicId) {
        IndexTemplateInfoWithPhyTemplates templateLogicWithPhysical = indexTemplateInfoService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null || templateLogicWithPhysical.getMasterPhyTemplate() == null) {
            LOGGER.info("class=TemplateQuotaManagerImpl||method=ctlSwitch||logicId={}||msg=notExist", logicId);
            return false;
        }

        // appid黑名单控制
        Set<String> disableAppIdSet = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
            QUOTA_DYNAMIC_LIMIT_BLACK_APP_IDS, "none", ",");
        if (disableAppIdSet.contains(String.valueOf(templateLogicWithPhysical.getAppId()))
            || disableAppIdSet.contains("all")) {
            LOGGER.info("class=TemplateQuotaManagerImpl||method=ctlSwitch||logicId={}||msg=black.appIds", logicId);
            return false;
        }

        IndexTemplatePhy templatePhysicalMaster = templateLogicWithPhysical.getMasterPhyTemplate();

        if (!isTemplateSrvOpen(templatePhysicalMaster.getCluster())) {
            LOGGER.info("class=TemplateQuotaManagerImpl||method=ctlSwitch||logicId={}||cluster={}||msg=没有开启容量管控的索引服务", logicId,
                templatePhysicalMaster.getCluster());

            return false;
        }

        // resource按着灰度
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByRack(templatePhysicalMaster.getCluster(),
            templatePhysicalMaster.getRack());
        LogicResourceConfig resourceConfig = clusterLogicService
            .genClusterLogicConfig(clusterLogic.getConfigJson());
        if (!QUOTA_CTL_DISK.equals(resourceConfig.getQuotaCtl())
            && !QUOTA_CTL_ALL.equals(resourceConfig.getQuotaCtl())) {
            LOGGER.info("class=TemplateQuotaManagerImpl||method=ctlSwitch||logicId={}||msg=resourceConfig.off", logicId);
            return false;
        }

        // cluster控制
        Set<String> disableClusterSet = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
            QUOTA_DYNAMIC_LIMIT_BLACK_CLUSTER, "none", ",");
        if (disableClusterSet.contains(templatePhysicalMaster.getCluster()) || disableClusterSet.contains("all")) {
            LOGGER.info("class=TemplateQuotaManagerImpl||method=ctlSwitch||logicId={}||msg=black.cluster", logicId);
            return false;
        }

        // 模板控制
        Set<String> disableLogicIdSet = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
            QUOTA_DYNAMIC_LIMIT_BLACK_LOGIC_ID, "none", ",");
        if (disableLogicIdSet.contains(String.valueOf(logicId)) || disableLogicIdSet.contains("all")) {
            LOGGER.info("class=TemplateQuotaManagerImpl||method=ctlSwitch||logicId={}||msg=black.logicId", logicId);
            return false;
        }

        return true;
    }

    /**
     * 管控维度
     *
     * @param cluster 集群
     * @param racks   racks
     * @return QuotaCtlRangeEnum
     */
    @Override
    public String getCtlRange(String cluster, String racks) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByRack(cluster, racks);
        LogicResourceConfig resourceConfig = clusterLogicService
            .genClusterLogicConfig(clusterLogic.getConfigJson());
        return resourceConfig.getQuotaCtl();
    }

    @Override
    public String getCtlRange(Integer logicId) {
        IndexTemplateInfoWithPhyTemplates logicWithPhysical = this.indexTemplateInfoService
            .getLogicTemplateWithPhysicalsById(logicId);

        return getCtlRange(logicWithPhysical.getMasterPhyTemplate().getCluster(),
            logicWithPhysical.getMasterPhyTemplate().getRack());
    }

    /**
     * 保存利用率
     *
     * @param usage po
     * @return true/false
     */
    @Override
    public boolean save(ESTemplateQuotaUsage usage) {
        IndexTemplateInfoWithCluster templateLogicWithResource = indexTemplateInfoService
            .getLogicTemplateWithCluster(usage.getLogicId());

        ESTemplateQuotaUsagePO usagePO = ConvertUtil.obj2Obj(usage, ESTemplateQuotaUsagePO.class);
        usagePO.setDataCenter(templateLogicWithResource.getDataCenter());
        usagePO.setQuota(templateLogicWithResource.getQuota());
        usagePO.setAppId(templateLogicWithResource.getAppId());
        usagePO.setLogicClusters(getLogicClusters(templateLogicWithResource.getLogicClusters()));

        boolean succ1 = esTemplateQuotaUsageDAO.insert(usagePO);
        LOGGER.info("class=TemplateQuotaManagerImpl||method=save||succ={}||logicId={}||msg=save usage", succ1, usagePO.getLogicId());

        ESTemplateQuotaUsageRecordPO recordPO = ConvertUtil.obj2Obj(usagePO, ESTemplateQuotaUsageRecordPO.class);
        recordPO.setTimestamp(new Date());

        boolean succ2 = esTemplateQuotaUsageRecordDAO.insert(recordPO);
        LOGGER.info("class=TemplateQuotaManagerImpl||method=save||succ={}||logicId={}||msg=save record", succ2, usagePO.getLogicId());

        return true;
    }

    /**
     * 查询全部
     *
     * @return list
     */
    @Override
    public List<ESTemplateQuotaUsage> listAll() {
        return ConvertUtil.list2List(esTemplateQuotaUsageDAO.listAll(), ESTemplateQuotaUsage.class);
    }

    @Override
    public List<ESTemplateQuotaUsage> listAllTemplateQuotaUsageWithCache() {
        if (cacheSwitch.logicTemplateQuotaUsageCacheEnable()) {
            try {
                return templateLogicQuotaUsageListCache.get("listAllPO", this::listAll);
            } catch (Exception e) {
                return listAll();
            }
        }

        return listAll();
    }

    @Override
    public ESTemplateQuotaUsage getByLogicId(Integer logicId) {
        return ConvertUtil.obj2Obj(esTemplateQuotaUsageDAO.getById(logicId), ESTemplateQuotaUsage.class);
    }

    /**
     * 获取模板的利用率
     *
     * @param logicId   逻辑模板ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return list
     */
    @Override
    public List<ESTemplateQuotaUsageRecordPO> getByLogicIdAndTime(Integer logicId, long startTime, long endTime) {
        return esTemplateQuotaUsageRecordDAO.getByLogicIdAndTime(logicId, startTime, endTime);
    }

    /**************************************** private method ****************************************************/
    /**
     * 根据模板quota的使用情况发布spring事件
     *
     * @param templateQuotaUsage quota
     * @return true/false
     */
    private boolean publishQuotaEvent(LogicTemplateQuotaUsage templateQuotaUsage) {
        if (templateQuotaUsage.getActualCpuCount() > templateQuotaUsage.getQuotaCpuCount()) {
            LOGGER.info(
                "class=TemplateQuotaManagerImpl||method=controlAndPublish||templateQuotaWithUsage={}||msg=tps limited by quota",
                templateQuotaUsage);
            SpringTool.publish(new TemplateCpuOutOfQuotaEvent(this, templateQuotaUsage));
        }

        if (templateQuotaUsage.getActualDiskG() > templateQuotaUsage.getQuotaDiskG()) {
            LOGGER.info(
                "class=TemplateQuotaManagerImpl||method=controlAndPublish||templateQuotaWithUsage={}||msg=disk limited by quota",
                templateQuotaUsage);
            SpringTool.publish(new TemplateDiskOutOfQuotaEvent(this, templateQuotaUsage));
        } else if (templateQuotaUsage.getActualDiskG()
                   / templateQuotaUsage.getQuotaDiskG() > QUOTA_DISK_WARN_THRESHOLD) {
            LOGGER.info(
                "class=TemplateQuotaManagerImpl||method=controlAndPublish||templateQuotaWithUsage={}||msg=disk quota has full",
                templateQuotaUsage);
            SpringTool.publish(new TemplateDiskUsageWarnEvent(this, templateQuotaUsage));
        }

        return true;
    }

    private PhysicalTemplateQuotaUsage getPhyTemplateQuotaUsageInner(IndexTemplatePhy templatePhysical, Long interval,
                                                                     GetTemplateQuotaUsageContext context) {
        if (interval == null) {
            interval = 15 * 60 * 1000L;
        }

        if (interval > 7 * AdminConstant.MILLIS_PER_DAY) {
            interval = 7 * AdminConstant.MILLIS_PER_DAY;
        }

        TemplateResourceConfig resourceConfig = templateAction
            .getPhysicalTemplateResourceConfig(templatePhysical.getId());

        long now = System.currentTimeMillis();
        Result<TemplateMetaMetric> result = templatePhyStatisManager
            .getTemplateMetricByPhysical(templatePhysical.getId(), now - interval, now, resourceConfig);

        PhysicalTemplateQuotaUsage physicalTemplateQuotaUsage = new PhysicalTemplateQuotaUsage();
        physicalTemplateQuotaUsage.setCluster(templatePhysical.getCluster());
        physicalTemplateQuotaUsage.setTemplate(templatePhysical.getName());

        if (result.success()) {
            TemplateMetaMetric templateMetaMetric = result.getData();
            context.setTemplateMetaMetric(templateMetaMetric);
            physicalTemplateQuotaUsage.setActualCpuCount(templateMetaMetric.getActualCpuCount());
            physicalTemplateQuotaUsage.setActualDiskG(templateMetaMetric.getActualDiskG());
            physicalTemplateQuotaUsage.setQuotaCpuCount(templateMetaMetric.getQuotaCpuCount());
            physicalTemplateQuotaUsage.setQuotaDiskG(templateMetaMetric.getQuotaDiskG());
            physicalTemplateQuotaUsage.setHotQuotaCpuCount(getHotQuotaCpuCount(templateMetaMetric));
        } else {
            physicalTemplateQuotaUsage.setActualCpuCount(0.0);
            physicalTemplateQuotaUsage.setActualDiskG(0.0);
            physicalTemplateQuotaUsage.setQuotaCpuCount(0.0);
            physicalTemplateQuotaUsage.setQuotaDiskG(0.0);
            physicalTemplateQuotaUsage.setHotQuotaCpuCount(0.0);
        }

        return physicalTemplateQuotaUsage;
    }

    private ESTemplateQuotaUsage buildESTemplateQuotaUsage(LogicTemplateQuotaUsage usage) {
        ESTemplateQuotaUsage esTemplateQuotaUsage = ConvertUtil.obj2Obj(usage, ESTemplateQuotaUsage.class);

        esTemplateQuotaUsage
            .setQuotaCpuUsage(usage.getQuotaCpuCount() == 0 ? 0 : usage.getActualCpuCount() / usage.getQuotaCpuCount());

        esTemplateQuotaUsage
            .setQuotaDiskUsage(usage.getQuotaDiskG() == 0 ? 0 : usage.getActualDiskG() / usage.getQuotaDiskG());

        return esTemplateQuotaUsage;
    }

    private Double getHotQuotaCpuCount(TemplateMetaMetric templateMetaMetric) {

        if (templateMetaMetric.getHotTime() > 0
            && templateMetaMetric.getHotTime() < templateMetaMetric.getExpireTime()) {
            return templateMetaMetric.getQuotaCpuCount() * templateMetaMetric.getHotTime()
                   / templateMetaMetric.getExpireTime();
        }

        return templateMetaMetric.getQuotaCpuCount();
    }

    private List<String> getLogicClusters(List<ClusterLogic> clusterLogics) {
        if (CollectionUtils.isEmpty(clusterLogics)) {
            return new ArrayList<>();
        }
        return clusterLogics.stream().map(ClusterLogic::getName).collect(Collectors.toList());
    }
}
