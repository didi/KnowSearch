package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateValuePO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class TemplateValueESDAO extends BaseESDAO {

    private String indexName;

    /**
     * type名称
     */
    private String typeName = "type";

    @PostConstruct
    public void init(){
        this.indexName = dataCentreUtil.getAriusTemplateValue();
    }

    /**
     * 批量保存结果
     *
     * @return
     */
    public boolean batchInsert(List<TemplateValuePO> list) {
        return updateClient.batchInsert( EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, list);
    }

    /**
     * 根据逻辑模板ID获取
     *
     * @param logicTemplateId
     * @return
     */
    public TemplateValuePO getByLogicTemplateId(Integer logicTemplateId) {
        return gatewayClient.doGet(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, String.valueOf(logicTemplateId), TemplateValuePO.class);
    }

    /**
     * 根据标签ID获取标签
     *
     * @return
     */
    public List<TemplateValuePO> listAll() {
        int scrollSize = 500;
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_VALUE_LIST_ALL,
                scrollSize);

        List<TemplateValuePO> list = Lists.newLinkedList();

        gatewayClient.queryWithScroll(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, dsl, scrollSize,
                null, TemplateValuePO.class, resultList -> {
                    if (resultList != null) {
                        list.addAll(resultList);

                        if (!EnvUtil.isOnline()) {
                            LOGGER.info("class=IndexTemplateValueEsDao||method=getLabelByDsl||TemplateValuePOList={}||dsl={}",
                                    JSON.toJSONString(resultList), dsl);
                        }
                    }
                });

        return list;
    }
}
