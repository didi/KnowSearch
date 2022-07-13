package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateValuePO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateValueESDAO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplateValueService {

    @Autowired
    private TemplateValueESDAO templateValueESDAO;

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

}