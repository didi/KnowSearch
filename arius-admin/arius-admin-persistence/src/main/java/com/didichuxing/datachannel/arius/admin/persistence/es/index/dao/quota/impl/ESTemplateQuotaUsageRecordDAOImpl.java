package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.quota.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.po.quota.ESTemplateQuotaUsageRecordPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameFactory;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.quota.ESTemplateQuotaUsageRecordDAO;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.DEFAULT_TYPE;

/**
 * @author d06679
 * @date 2019-09-03
 */
@Repository
public class ESTemplateQuotaUsageRecordDAOImpl extends BaseESDAO implements ESTemplateQuotaUsageRecordDAO {
    private String INDEX_NAME;

    private static final String GET_BY_LOGICID_AND_TIME_FORMAT = "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"logicId\":{\"value\":%s}}},{\"range\":{\"timestamp\":{\"gte\":%s,\"lte\":%s}}}]}},\"sort\":[{\"timestamp\":{\"order\":\"desc\"}}],\"size\":9999}";

    @PostConstruct
    public void init() {
        LOGGER.info("class=ESTemplateQuotaUsageRecordDAOImpl||method=init||ESTemplateQuotaUsageRecordDAOImpl init start.");
        this.INDEX_NAME = dataCentreUtil.getAriusTemplateQuotaUsageRecord();
        LOGGER.info("class=ESTemplateQuotaUsageRecordDAOImpl||method=init||ESTemplateQuotaUsageRecordDAOImpl init finished.");
    }

    @Autowired
    private ESGatewayClient     esGatewayClient;

    /**
     * 保存quota利用率
     *
     * @param recordPO 利用率
     * @return true/false
     */
    @Override
    public boolean insert(ESTemplateQuotaUsageRecordPO recordPO) {
        return updateClient.batchInsert(IndexNameFactory.getNoVersion(INDEX_NAME + "*", "_yyyy-MM", 0),
            AdminConstant.DEFAULT_TYPE, Lists.newArrayList(recordPO));
    }

    /**
     * 获取模板的quota利用率记录
     * @param logicId 逻辑模板ID
     * @param startTimestamp 开始时间
     * @param endTimestamp 截止时间
     * @return list
     */
    @Override
    public List<ESTemplateQuotaUsageRecordPO> getByLogicIdAndTime(Integer logicId, long startTimestamp,
                                                                  long endTimestamp) {
        return esGatewayClient.performRequest(INDEX_NAME, DEFAULT_TYPE, String.format(GET_BY_LOGICID_AND_TIME_FORMAT, logicId, startTimestamp, endTimestamp),
            ESTemplateQuotaUsageRecordPO.class);
    }
}
