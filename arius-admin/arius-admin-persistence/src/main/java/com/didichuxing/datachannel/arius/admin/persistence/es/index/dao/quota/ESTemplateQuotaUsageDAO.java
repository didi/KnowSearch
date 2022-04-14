package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.quota;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.po.quota.ESTemplateQuotaUsagePO;

/**
 * @author d06679
 * @date 2019-09-03
 */
public interface ESTemplateQuotaUsageDAO {

    /**
     * 保存quota利用率
     * @param usagePO 利用率
     * @return true/false
     */
    boolean insert(ESTemplateQuotaUsagePO usagePO);

    /**
     * listAll
     * @return list
     */
    List<ESTemplateQuotaUsagePO> listAll();

    /**
     * getById
     * @param logicId logicId
     * @return
     */
    ESTemplateQuotaUsagePO getById(Integer logicId);
}
