package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.TemplateValueRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateValuePO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateValueESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateValueRecordESDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateValueService {

    @Autowired
    private TemplateValueESDAO templateValueESDAO;

    @Autowired
    private TemplateValueRecordESDAO templateValueRecordESDAO;

    /**
     * 获取模板的健康分
     *
     * @param logicTemplateId 逻辑模板ID
     * @return result
     */
    public TemplateValuePO getTemplateValueByLogicTemplateId(Integer logicTemplateId) {
        return templateValueESDAO.getByLogicTemplateId(logicTemplateId);
    }

    public List<TemplateValuePO> listAll() {
        return templateValueESDAO.listAll();
    }

    public List<TemplateValueRecord> getRecordByLogicTemplateId(Long logicTemplateId, Long startDate, Long endDate) {
        return ConvertUtil.list2List(
                templateValueRecordESDAO.getRecordByLogicTemplateId(logicTemplateId, startDate, endDate),
                TemplateValueRecord.class);
    }
}
