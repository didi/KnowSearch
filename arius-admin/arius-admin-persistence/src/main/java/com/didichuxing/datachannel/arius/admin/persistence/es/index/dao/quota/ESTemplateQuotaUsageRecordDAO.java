package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.quota;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.po.quota.ESTemplateQuotaUsageRecordPO;

/**
 * @author d06679
 * @date 2019-09-03
 */
public interface ESTemplateQuotaUsageRecordDAO {

    /**
     * 保存quota利用率
     * @param recordPO 利用率
     * @return true/false
     */
    boolean insert(ESTemplateQuotaUsageRecordPO recordPO);

    /**
     * 获取模板的quota利用率记录
     * @param logicId 逻辑模板ID
     * @param startTimestamp 开始时间
     * @param endTimestamp 截止时间
     * @return list
     */
    List<ESTemplateQuotaUsageRecordPO> getByLogicIdAndTime(Integer logicId, long startTimestamp, long endTimestamp);

}
