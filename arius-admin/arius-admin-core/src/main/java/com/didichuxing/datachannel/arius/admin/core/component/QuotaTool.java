package com.didichuxing.datachannel.arius.admin.core.component;

import com.didichuxing.datachannel.arius.admin.common.constant.quota.NodeSpecifyEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.quota.Resource;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.*;

/**
 * @author d06679
 * @date 2019/4/24
 * @deprecated
 */
@Deprecated
@Component
public class QuotaTool {

    public static final Double QUOTA_DISK_WARN_THRESHOLD = 0.80;

    public static final Double TEMPLATE_QUOTA_MIN = 0.001;

    @Autowired
    private AriusConfigInfoService configInfoService;

    /**
     * 计算模板quota
     *
     * @param specifyCode 节点规格
     * @param disk        磁盘
     * @return quota
     */
    public Double getQuotaCountByDisk(int specifyCode, double disk, double quotaMin) {
        if (disk < 0) {
            // 永不过期的场景
            disk = 1024.0;
        }

        Resource nodeSpecify = NodeSpecifyEnum.resourceOf(specifyCode);
        double quota = disk / nodeSpecify.getDisk();

        if (quota < quotaMin) {
            quota = quotaMin;
        }

        return quota;
    }

    /**
     * 计算模板quota
     *
     * @param specifyCode 节点规格
     * @param cpu         CPU需求个数
     * @return quota
     */
    public Double getQuotaCountByCpu(int specifyCode, double cpu, double quotaMin) {
        Resource nodeSpecify = NodeSpecifyEnum.resourceOf(specifyCode);
        double quota = cpu / nodeSpecify.getCpu();

        if (quota < quotaMin) {
            quota = quotaMin;
        }
        return quota;
    }

    /**
     * 计算模板quota
     *
     * @param specifyCode 节点规格
     * @param cpu         cpu
     * @param disk        磁盘
     * @return quota
     */
    public Double getTemplateQuotaCountByCpuAndDisk(int specifyCode, double cpu, double disk, double quotaMin) {
        double quotaByCpu = getQuotaCountByCpu(specifyCode, cpu, quotaMin);
        double quotaByDisk = getQuotaCountByDisk(specifyCode, disk, quotaMin);
        return Math.max(quotaByCpu, quotaByDisk);
    }

    /**
     * 计算模板quota
     *
     * @param specifyCode 节点规格
     * @param cpu         cpu
     * @param disk        磁盘
     * @return quota
     */
    public Double getResourceQuotaCountByCpuAndDisk(int specifyCode, double cpu, double disk, double quotaMin) {
        double quotaByCpu = getQuotaCountByCpu(specifyCode, cpu, quotaMin);
        double quotaByDisk = getQuotaCountByDisk(specifyCode, disk, quotaMin);
        return Math.min(quotaByCpu, quotaByDisk);
    }

    /**
     * 根据quota个数得到资源信息
     *
     * @param quota quota个数
     * @return 资源总量
     */
    public Resource getResourceOfQuota(int specifyCode, double quota) {
        Resource nodeSpecify = NodeSpecifyEnum.resourceOf(specifyCode);
        double cpu = nodeSpecify.getCpu() * quota;
        double mem = nodeSpecify.getMem() * quota;
        double disk = nodeSpecify.getDisk() * quota;
        return new Resource(cpu, mem, disk);
    }

    public Double getTpsPerCpu(boolean hasReplica) {
        if (hasReplica) {
            return configInfoService.doubleSetting(ARIUS_QUOTA_CONFIG_GROUP,
                    ARIUS_QUOTA_CONFIG_TPS_PER_CPU_WITH_REPLICA, 1000.0);
        }

        return configInfoService.doubleSetting(ARIUS_QUOTA_CONFIG_GROUP, ARIUS_QUOTA_CONFIG_TPS_PER_CPU_NO_REPLICA,
                2300.0);
    }

    public Double computeCostByQuota(int specifyCode, double quota) {
        Resource nodeSpecify = NodeSpecifyEnum.resourceOf(specifyCode);
        Double costPerGPerMonth = configInfoService.doubleSetting(ARIUS_QUOTA_CONFIG_GROUP,
                ARIUS_QUOTA_CONFIG_COST_PER_G_PER_MONTH, 1.06);
        return nodeSpecify.getDisk() * costPerGPerMonth * quota;
    }
}
