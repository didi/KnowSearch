package com.didichuxing.datachannel.arius.admin.common.bean.vo.quota;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

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
@Deprecated
public class ESTemplateQuotaUsageRecordVO extends BaseVO {

    /**
     * 逻辑模板id
     */
    private Integer logicId;

    /**
     * 模板
     */
    private String  template;

    /**
     * 实际的磁盘消耗
     */
    private Double  actualDiskG;

    /**
     * 实际的CPU消耗
     */
    private Double  actualCpuCount;

    /**
     * Quota的磁盘消耗
     */
    private Double  quotaDiskG;

    /**
     * Quota的CPU消耗
     */
    private Double  quotaCpuCount;

    /**
     * Quota的磁盘消耗率
     */
    private Double  quotaDiskUsage;

    /**
     * Quota的CPU消耗率
     */
    private Double  quotaCpuUsage;

    /**
     * 时间戳
     */
    private Date    timestamp;

}