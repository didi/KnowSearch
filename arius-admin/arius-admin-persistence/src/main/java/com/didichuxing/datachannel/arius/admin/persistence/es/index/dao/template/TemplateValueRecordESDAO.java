package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateValueRecordPO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
@NoArgsConstructor
public class TemplateValueRecordESDAO extends BaseESDAO {
    private String indexName;

    /**
     * type名称
     */
    private String typeName = "type";

    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getAriusTemplateValueRecord();
    }

    /**
     * 批量保存结果
     *
     * @return
     */
    public boolean batchInsert(List<TemplateValueRecordPO> list) {
        return updateClient.batchInsert(IndexNameUtils.genCurrentMonthlyIndexName(EnvUtil.getWriteIndexNameByEnv(this.indexName)), typeName, list);
    }

    public List<TemplateValueRecordPO> getRecordByLogicTemplateId(Long logicTemplateId, Long startDate, Long endDate) {
        int scrollSize = 500;
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_VALUE_RECORD_BY_TEMPLATE,
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
    private List<TemplateValueRecordPO> getLabelByDsl(String dsl, int scrollSize) {
        List<TemplateValueRecordPO> list = Lists.newLinkedList();

        gatewayClient.queryWithScroll(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, dsl, scrollSize,
                null, TemplateValueRecordPO.class, resultList -> {
                    if (resultList != null) {
                        list.addAll(resultList);

                        if (!EnvUtil.isOnline()) {
                            LOGGER.info("class=IndexTemplateValueRecordEsDao||method=getLabelByDsl||IndexTemplateValueRecordPOList={}||dsl={}",
                                    JSON.toJSONString(resultList), dsl);
                        }
                    }
                });

        return list;
    }
}
