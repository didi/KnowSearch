package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslQueryLimit;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleThree;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslTemplateESDAO;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author cjm
 */
@Service
public class DslTemplateService {

    @Autowired
    private DslTemplateESDAO dslTemplateESDAO;

    public TupleTwo</*变更后的结果*/Boolean,/*变更前的dsl*/Map</*dslTemplateMd5*/String,/*变更前的queryLimit*/ Double>> updateDslTemplateQueryLimit(Integer projectId,
                                                                                                         List<String> dslTemplateMd5List,
                                                                                                         Double queryLimit) {
               
        List<DslQueryLimit> dslQueryLimitList = new ArrayList<>();
        Map<String,Double> dslTemplateMd5QueryLimitMap= Maps.newHashMap();
        for(String dslTemplateMd5 : dslTemplateMd5List) {
            // 排除无效的 dslTemplateMd5
            final DslTemplatePO dslTemplateByKey = dslTemplateESDAO.getDslTemplateByKey(projectId, dslTemplateMd5);
            if(dslTemplateByKey != null) {
                dslQueryLimitList.add(new DslQueryLimit(projectId, dslTemplateMd5, queryLimit));
                dslTemplateMd5QueryLimitMap.put(dslTemplateMd5,dslTemplateByKey.getQueryLimit());
            }
        }
        return Tuples.of(dslTemplateESDAO.updateQueryLimitByProjectIdDslTemplate(dslQueryLimitList),
                dslTemplateMd5QueryLimitMap);
    }
    
    public TupleThree</*变更前*/Boolean,/*变更后*/ Boolean,/*变更状态*/Boolean> updateDslTemplateStatus(Integer projectId,
                                                                                              String dslTemplateMd5) {
        DslTemplatePO dslTemplatePO = dslTemplateESDAO.getDslTemplateByKey(projectId, dslTemplateMd5);
        if(dslTemplatePO == null) {
            // 如果没有该 dslTemplateMd5
            return Tuples.of(false,null,null);
        }
        //设置变更前的enable
        final TupleThree<Boolean, Boolean, Boolean> statusTuple3 = Tuples.of(dslTemplatePO.getEnable(), null, null);
        // getEnable() 如果为 null，表示当前是启用状态，反转模版启用状态
        if(dslTemplatePO.getEnable() == null) {
            dslTemplatePO.setEnable(false);
            
        } else {
            dslTemplatePO.setEnable(!dslTemplatePO.getEnable());
        }
        statusTuple3.update2(dslTemplatePO.getEnable());
        String ariusModifyTime = DateTimeUtil.getCurrentFormatDateTime();
        dslTemplatePO.setAriusModifyTime(ariusModifyTime);
        List<DslTemplatePO> dslTemplatePOList = new ArrayList<>();
        dslTemplatePOList.add(dslTemplatePO);
        return statusTuple3.update3(dslTemplateESDAO.updateTemplates(dslTemplatePOList));
    }

    public DslTemplatePO getDslTemplateDetail(Integer projectId, String dslTemplateMd5) {
        return dslTemplateESDAO.getDslTemplateByKey(projectId, dslTemplateMd5);
    }

    public Tuple<Long, List<DslTemplatePO>> getDslTemplatePage(Integer projectId, DslTemplateConditionDTO queryDTO) {
        return dslTemplateESDAO.getDslTemplatePage(projectId, queryDTO);
    }
}