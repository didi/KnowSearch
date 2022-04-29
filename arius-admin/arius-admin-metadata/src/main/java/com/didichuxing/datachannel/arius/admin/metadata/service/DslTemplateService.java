package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslQueryLimit;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslTemplateESDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjm
 */
@Service
public class DslTemplateService {

    @Autowired
    private DslTemplateESDAO dslTemplateESDAO;

    public Boolean updateDslTemplateQueryLimit(Integer appId, List<String> dslTemplateMd5List, Double queryLimit) {
        List<DslQueryLimit> dslQueryLimitList = new ArrayList<>();
        for(String dslTemplateMd5 : dslTemplateMd5List) {
            // 排除无效的 dslTemplateMd5
            if(dslTemplateESDAO.getDslTemplateByKey(appId, dslTemplateMd5) != null) {
                dslQueryLimitList.add(new DslQueryLimit(appId, dslTemplateMd5, queryLimit));
            }
        }
        return dslTemplateESDAO.updateQueryLimitByAppidDslTemplate(dslQueryLimitList);
    }

    public Boolean updateDslTemplateStatus(Integer appId, String dslTemplateMd5) {
        DslTemplatePO dslTemplatePO = dslTemplateESDAO.getDslTemplateByKey(appId, dslTemplateMd5);
        if(dslTemplatePO == null) {
            // 如果没有该 dslTemplateMd5
            return false;
        }
        // getEnable() 如果为 null，表示当前是启用状态，反转模版启用状态
        if(dslTemplatePO.getEnable() == null) {
            dslTemplatePO.setEnable(false);
        } else {
            dslTemplatePO.setEnable(!dslTemplatePO.getEnable());
        }
        List<DslTemplatePO> dslTemplatePOList = new ArrayList<>();
        dslTemplatePOList.add(dslTemplatePO);
        return dslTemplateESDAO.updateTemplates(dslTemplatePOList);
    }

    public DslTemplatePO getDslTemplateDetail(Integer appId, String dslTemplateMd5) {
        return dslTemplateESDAO.getDslTemplateByKey(appId, dslTemplateMd5);
    }

    public Tuple<Long, List<DslTemplatePO>> getDslTemplatePage(Integer appId, DslTemplateConditionDTO queryDTO) {
        return dslTemplateESDAO.getDslTemplatePage(appId, queryDTO);
    }
}
