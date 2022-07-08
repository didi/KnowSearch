package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLabelPO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/3/8 下午11:39
 * @modified By D10865
 */
@Repository
@NoArgsConstructor
public class TemplateLabelESDAO extends BaseESDAO {

    /**
     * 索引名称
     */
    private String indexName;

    /**
     * type名称
     */
    private String typeName = "type";

    @PostConstruct
    public void init(){
        this.indexName = dataCentreUtil.getAriusTemplateLabel();
    }

    /**
     * 批量保存结果
     *
     * @return
     */
    public boolean batchInsert(List<TemplateLabelPO> list) {
        return updateClient.batchInsert( EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, list);
    }

    /**
     * 批量删除
     *
     * @return
     */
    public boolean batchDelete(List<String> list) {
        return updateClient.batchDelete(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, list);
    }


    /**
     * 根据索引模板ID获取标签
     *
     * @param logicTemplateId
     * @return
     */
    public List<TemplateLabelPO> getLabelByLogicTemplateId(Integer logicTemplateId) {

        int scrollSize = 500;
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.V2_SCROLL_LABEL_BY_TEMPLATE_ID,
                500, logicTemplateId);

        return getLabelByDsl(dsl, scrollSize);
    }

    /**
     * 根据标签ID获取标签
     *
     * @param labelId
     * @return
     */
    public List<TemplateLabelPO> getLabelByLabelId(String labelId) {
        int scrollSize = 500;
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.V2_SCROLL_LABEL_BY_LABEL_ID,
                scrollSize, labelId);

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
    private List<TemplateLabelPO> getLabelByDsl(String dsl, int scrollSize) {
        List<TemplateLabelPO> list = Lists.newLinkedList();

        gatewayClient.queryWithScroll(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, dsl, scrollSize,
                null, TemplateLabelPO.class, resultList -> {
                    if (resultList != null) {
                        list.addAll(resultList);

                        if (!EnvUtil.isOnline()) {
                            LOGGER.info("class=IndexTemplateLabelEsDao||method=getLabelByDsl||indexTemplateLabelPOList={}||dsl={}",
                                    JSON.toJSONString(resultList), dsl);
                        }
                    }
                });

        return list;
    }
}