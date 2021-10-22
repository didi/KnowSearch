package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.access.TemplateAccessDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.access.UserAccessTemplateDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.IndexNameAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class IndexAccessESDAO extends BaseESDAO {

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
        this.indexName = dataCentreUtil.getAriusIndexNameAccess();
    }

    /**
     * 批量保存结果
     *
     * @return
     */
    public boolean batchInsert(List<IndexNameAccessCountPO> list) {
        return updateClient.batchInsert( EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, list);
    }

    /**
     * 根据索引模板名称获取查询的具体索引统计次数
     *
     * @param accessDetail
     * @return
     */
    public List<IndexNameAccessCountPO> getIndexNameAccessByTemplate(TemplateAccessDetail accessDetail) {
        int scrollSize = 1000;
        String dsl = dslLoaderUtil.getFormatDslByFileName( DslsConstant.SCROLL_GET_TEMPLATE_DETAIL_BY_TEMPLATE, scrollSize,
                accessDetail.getClusterName(), accessDetail.getTemplateName(), accessDetail.getCountDate());

        return getIndexNameAccessByDsl(dsl, scrollSize);
    }

    /**
     * 根据索引模板ID获取查询的具体索引统计次数
     *
     * @param userAccessTemplateDetail
     *
     * @return
     */
    public List<IndexNameAccessCountPO> getIndexNameAccessByTemplateId(UserAccessTemplateDetail userAccessTemplateDetail) {
        int scrollSize = 1000;
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_GET_TEMPLATE_DETAIL_BY_ID, scrollSize,
                userAccessTemplateDetail.getTemplateId(), userAccessTemplateDetail.getCountDate());

        return getIndexNameAccessByDsl(dsl, scrollSize);
    }

    /**
     * 根据查询语句获取索引模板查询的具体索引统计次数
     *
     * @param dsl
     * @param scrollSize
     * @return
     */
    private List<IndexNameAccessCountPO> getIndexNameAccessByDsl(String dsl, int scrollSize) {
        List<IndexNameAccessCountPO> list = Lists.newLinkedList();

        gatewayClient.queryWithScroll(indexName, typeName, dsl, scrollSize, null, IndexNameAccessCountPO.class, resultList -> {
            if (resultList != null) {
                list.addAll(resultList);
            }
        } );

        return list;
    }
}
