package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateHealthDegreeRecordPO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class TemplateHealthDegreeRecordESDAO extends BaseESDAO {

    private String indexName;

    /**
     * type名称
     */
    private String typeName = "type";

    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getAriusTemplateHealthDegree();
    }

    public List<TemplateHealthDegreeRecordPO> getRecordByLogicTemplateId(Long logicTemplateId, Long startDate, Long endDate) {
        int scrollSize = 500;
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_TEMPLATE_HEALTH_DEGREE_RECORD_BY_TEMPLATE,
                scrollSize, startDate, endDate, logicTemplateId);

        return getLabelByDsl(dsl, scrollSize);
    }

    /**************************************** private methods ****************************************/
    /**
     * 根据查询语句获取标签
     *
     * @param dsl
     * @param scrollSize
     * @return
     */
    private List<TemplateHealthDegreeRecordPO> getLabelByDsl(String dsl, int scrollSize) {
        List<TemplateHealthDegreeRecordPO> list = Lists.newLinkedList();

        gatewayClient.queryWithScroll(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, dsl, scrollSize,
                null, TemplateHealthDegreeRecordPO.class, resultList -> {
                    if (resultList != null) {
                        list.addAll(resultList);

                        if (!EnvUtil.isOnline()) {
                            LOGGER.info("class=IndexTemplateHealthDegreeRecordDao||method=getLabelByDsl||IndexTemplateHealthDegreeRecordPOList={}||dsl={}",
                                    JSON.toJSONString(resultList), dsl);
                        }
                    }
                });

        return list;
    }
}
