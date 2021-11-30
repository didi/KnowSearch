package com.didichuxing.datachannel.arius.admin.common.bean.po.quota;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019-09-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESTemplateQuotaUsagePO extends BaseESPO {

    /**
     * 逻辑模板id
     */
    private Integer      logicId;

    /**
     * 模板
     */
    private String       template;

    /**
     * 数据中心
     */
    private String       dataCenter;

    private Double       quota;

    private Integer      appId;

    /**
     * 逻辑资源
     */
    private List<String> logicClusters;

    /**
     * 实际的磁盘消耗
     */
    private Double       actualDiskG;

    /**
     * 实际的CPU消耗
     */
    private Double       actualCpuCount;

    /**
     * Quota的磁盘消耗
     */
    private Double       quotaDiskG;

    /**
     * Quota的CPU消耗
     */
    private Double       quotaCpuCount;

    /**
     * Quota的磁盘消耗率
     */
    private Double       quotaDiskUsage;

    /**
     * Quota的CPU消耗率
     */
    private Double       quotaCpuUsage;

    /**
     * 获取主键key
     *
     * @return
     */
    @Override
    public String getKey() {
        return String.valueOf(logicId);
    }
}
