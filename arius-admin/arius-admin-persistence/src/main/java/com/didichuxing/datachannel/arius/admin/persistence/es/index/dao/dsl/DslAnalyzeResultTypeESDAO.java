package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl;

import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslAnalyzeResultTypePO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.List;

@Component
@NoArgsConstructor
public class DslAnalyzeResultTypeESDAO extends BaseESDAO {

    /**
     * 索引名称
     */
    private String indexName;
    /**
     * type名称
     */
    private String typeName = "type";

    private static final int SCROLL_SIZE = 5000;

    @PostConstruct
    public void init(){
        this.indexName = dataCentreUtil.getAriusDslAnalyzeResult();
    }

    /**
     * 批量写入appid维度分析查询结果到es
     *
     * @param appidAnalyzeResultList
     * @return
     */
    public boolean batchInsert(List<DslAnalyzeResultTypePO> appidAnalyzeResultList) {
        return updateClient.batchInsert(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, appidAnalyzeResultList);
    }

    /**
     * 查询分析结果根据appid
     *
     * @param appid
     * @return
     */
    @Nullable
    public List<DslAnalyzeResultTypePO> getDslAnalyzeResultByAppid(Integer from, Long appid) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_ANALYZE_RESULT_BY_APPID, from, appid);

        return gatewayClient.performRequest(indexName, typeName, dsl, DslAnalyzeResultTypePO.class);
    }

    /**
     * 查询分析结果根据appid
     *
     * @param appid
     * @return
     */
    @Nullable
    public List<DslAnalyzeResultTypePO> getDslAnalyzeResultByAppidAndRange(Long appid, Long startDate, Long endDate) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_ANALYZE_RESULT_BY_APPID_AND_RANGE, SCROLL_SIZE, startDate, endDate, appid);

        List<DslAnalyzeResultTypePO> dslAnalyzeResultTypePos = Lists.newLinkedList();
        gatewayClient.queryWithScroll(indexName,
                typeName, dsl, SCROLL_SIZE, null, DslAnalyzeResultTypePO.class, resultList -> {
                    if (resultList != null) {
                        dslAnalyzeResultTypePos.addAll(resultList);
                    }
                });

        return dslAnalyzeResultTypePos;
    }
}
