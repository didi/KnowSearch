package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.TemplateHealthDegreeRecord;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateHealthDegreeRecordESDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateHealthDegreeService {

    @Autowired
    private TemplateHealthDegreeRecordESDAO templateHealthDegreeRecordESDAO;

    public List<TemplateHealthDegreeRecord> getRecordByLogicTemplateId(Long logicTemplateId, Long startDate, Long endDate) {
        return ConvertUtil.list2List(
                templateHealthDegreeRecordESDAO.getRecordByLogicTemplateId(logicTemplateId, startDate, endDate),
                TemplateHealthDegreeRecord.class);
    }
}
