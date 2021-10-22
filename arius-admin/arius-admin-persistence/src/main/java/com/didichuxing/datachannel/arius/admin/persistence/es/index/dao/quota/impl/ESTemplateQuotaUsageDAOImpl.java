package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.quota.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.DEFAULT_TYPE;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.quota.ESTemplateQuotaUsagePO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.quota.ESTemplateQuotaUsageDAO;
import com.google.common.collect.Lists;

import javax.annotation.PostConstruct;

/**
 * @author d06679
 * @date 2019-09-03
 */
@Repository
public class ESTemplateQuotaUsageDAOImpl extends BaseESDAO implements ESTemplateQuotaUsageDAO {

    private String INDEX_NAME;

    @PostConstruct
    public void init() {
        LOGGER.info("class=TemplateSrvService||method=init||ESTemplateQuotaUsageDAOImpl init start.");
        this.INDEX_NAME = dataCentreUtil.getAriusTemplateQuotaUsage();
        LOGGER.info("class=TemplateSrvService||method=init||ESTemplateQuotaUsageDAOImpl init finished.");
    }

    /**
     * 保存quota利用率
     *
     * @param usagePO 利用率
     * @return true/false
     */
    @Override
    public boolean insert(ESTemplateQuotaUsagePO usagePO) {
        return updateClient.batchInsert(INDEX_NAME, DEFAULT_TYPE, Lists.newArrayList(usagePO));
    }

    /**
     * listAll
     *
     * DataCenterEnum dataCenterEnum, String indexName, String typeName, String queryDsl, Class<T> clzz
     *
     * @return list
     */
    @Override
    public List<ESTemplateQuotaUsagePO> listAll() {
        return gatewayClient.performRequest(INDEX_NAME, DEFAULT_TYPE,
            "{\"query\":{\"match_all\":{}},\"size\":9999}", ESTemplateQuotaUsagePO.class);
    }

    @Override
    public ESTemplateQuotaUsagePO getById(Integer logicId) {
        return gatewayClient.doGet(INDEX_NAME, DEFAULT_TYPE, String.valueOf(logicId), ESTemplateQuotaUsagePO.class);
    }
}
