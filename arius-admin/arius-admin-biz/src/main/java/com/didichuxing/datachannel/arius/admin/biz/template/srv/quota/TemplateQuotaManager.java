package com.didichuxing.datachannel.arius.admin.biz.template.srv.quota;

import com.didichuxing.datachannel.arius.admin.client.bean.common.GetTemplateQuotaUsageContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.ESTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.LogicTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.PhysicalTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.po.quota.ESTemplateQuotaUsageRecordPO;

import java.util.List;

/**
 * @author d06679
 * @date 2019/4/25
 */
public interface TemplateQuotaManager {

    /**
     * 获取模板quota 及当前quota使用量
     * @param cluster  集群
     * @param template  模板
     * @param interval interval
     * @param context 上下文
     * @return result
     */
    PhysicalTemplateQuotaUsage getPhyTemplateQuotaUsage(String cluster, String template, Long interval,
                                                        GetTemplateQuotaUsageContext context);

    /**
     * 获取模板quota 及当前quota使用量
     * @param logicId  id
     * @param interval interval
     * @return result
     */
    LogicTemplateQuotaUsage getLogicTemplateQuotaUsage(Integer logicId, Long interval);

    /**
     * 根据模板quota的使用情况发布spring事件
     * @param logicId logicId
     * @return true/false
     */
    boolean controlAndPublish(Integer logicId);

    /**
     * 计算模板的费用
     * @param nodeSpecify 节点规格
     * @param quota quota
     * @return 费用 元/月
     */
    Double computeCostByQuota(Integer nodeSpecify, Double quota);

    /**
     * 根据磁盘计算成本
     * @param diskG 磁盘
     * @param resourceId resourceId
     * @return 成本
     */
    Double computeCostByDisk(Double diskG, Long resourceId);

    /**
     * 管控开关
     * @param logicId 逻辑ID
     * @return true/false
     */
    boolean enableClt(Integer logicId);

    /**
     * 管控维度
     * @param cluster 集群
     * @param racks racks
     * @return QuotaCtlRangeEnum
     */
    String getCtlRange(String cluster, String racks);

    String getCtlRange(Integer logicId);

    /**
     * 保存利用率
     * @param usage po
     * @return true/false
     */
    boolean save(ESTemplateQuotaUsage usage);

    /**
     * 查询全部
     * @return list
     */
    List<ESTemplateQuotaUsage> listAll();

    List<ESTemplateQuotaUsage> listAllTemplateQuotaUsageWithCache();

    /**
     * 查询一个模板的利用率
     * @param logicId 逻辑ID
     * @return usage
     */
    ESTemplateQuotaUsage getByLogicId(Integer logicId);

    /**
     * 获取模板的利用率
     * @param logicId 逻辑模板ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return list
     */
    List<ESTemplateQuotaUsageRecordPO> getByLogicIdAndTime(Integer logicId, long startTime, long endTime);

}
