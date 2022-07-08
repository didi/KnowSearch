package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index;

import com.didichuxing.datachannel.arius.admin.common.bean.po.index.IndexSizePO;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@NoArgsConstructor
public class IndexSizeESDAO extends BaseESDAO {

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
        this.indexName = dataCentreUtil.getAriusIndexSize();
    }

    /**
     * 批量保存索引大小结果
     *
     * @param list
     * @return
     */
    public boolean batchInsert(List<IndexSizePO> list) {

        return updateClient.batchInsert(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, list);
    }

    /**
     * 根据索引模板名称获取索引大小信息
     *
     * @param templateName
     * @return
     */
    public IndexSizePO getNearestIndexSizeByTemplateName(String templateName) {
        List<IndexSizePO> hits = getIndexSizeByTemplateName(templateName, 1);
        if(CollectionUtils.isEmpty(hits)) {
            return null;
        }

        return hits.get(0);
    }


    /**
     * 根据索引模板名称获取索引大小信息
     *
     * @param templateName
     * @return
     */
    public List<IndexSizePO> getIndexSizeByTemplateName(String templateName, int count) {

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_INDEX_SIZE_BY_TEMPLATE, count, templateName);

        return gatewayClient.performRequest(indexName, typeName, dsl, IndexSizePO.class);
    }


    public Map<String, IndexSizePO> getYesterDayIndexSize(int offset) {

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_YESTERDAY_INDEX_SIZE, DateTimeUtil.getFormatDayByOffset(offset));

        List<IndexSizePO> indexSizePOS = gatewayClient.performRequest(indexName, typeName, dsl, IndexSizePO.class);

        Map<String, IndexSizePO> ret = new HashMap<>();
        for(IndexSizePO IndexSizePO : indexSizePOS) {
            ret.put(IndexSizePO.getTemplateName(), IndexSizePO);
        }
        return ret;
    }
    
}
