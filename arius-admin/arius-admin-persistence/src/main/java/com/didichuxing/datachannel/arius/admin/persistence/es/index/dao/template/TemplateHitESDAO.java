package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateHitPO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESAggrMap;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TemplateHitESDAO extends BaseESDAO {

    /**
     * 索引模板查询命中次数统计
     */
    private String indexName;
    /**
     * type名称
     */
    private String typeName = "type";

    @PostConstruct
    public void init(){
        this.indexName = dataCentreUtil.getAriusTemplateHit();
    }

    /**
     * 批量保存查询模板命中信息
     *
     * @param TemplateHitPOList
     * @return
     */
    public boolean batchInsert(List<TemplateHitPO> TemplateHitPOList) {
        return updateClient.batchInsert(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, TemplateHitPOList);
    }


    public long getMaxUseTime(long id) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_USE_TIME_BY_TEMPLATE, id);
        String index = indexName.trim();

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(index, typeName, dsl);
        if (esAggrMap == null) {
            LOGGER.error("class=TemplateHitEsDao||method=getMaxUseTime||indexName={}||queryDsl={}||errMsg=list is empty", index, dsl);
            return -1;
        }

        try {
            ESAggr aggr = esAggrMap.getEsAggrMap().get("NAME");
            return Double.valueOf(aggr.getUnusedMap().get("value").toString()).longValue();
        } catch (Throwable t) {
            return -1;
        }
    }


    public List<TemplateHitPO> getByDate(String date) {
        String dsl = dslLoaderUtil.getFormatDslByFileName( DslsConstant.GET_TEMPLATE_HIT_BY_DATE, date);
        String index = indexName.trim();

        List<TemplateHitPO> hits = gatewayClient.performRequest(index, typeName, dsl, TemplateHitPO.class);
        if (hits == null) {
            LOGGER.error("class=TemplateHitEsDao||method=getByTemplate||indexName={}||queryDsl={}||errMsg=list is empty",
                    index, dsl);
            return null;
        }

        return hits;
    }


    public List<TemplateHitPO> getByTemplate(long id) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_HIT_BY_TEMPLATE, id);
        String index = indexName.trim();

        List<TemplateHitPO> hits = gatewayClient.performRequest(index, typeName, dsl, TemplateHitPO.class);
        if (hits == null) {
            LOGGER.error("class=TemplateHitEsDao||method=getByTemplate||indexName={}||queryDsl={}||errMsg=list is empty",
                    index, dsl);
            return null;
        }

        // 合并数据
        Map<String, TemplateHitPO> m = new HashMap<>();
        for (TemplateHitPO hit : hits) {
            if (!m.containsKey(hit.getDate())) {
                m.put(hit.getDate(), new TemplateHitPO(hit));
            }

            m.get(hit.getDate()).merge(hit);
        }

        return new ArrayList<>(m.values());
    }

    public List<TemplateHitPO> getTemplateHit(String templateName, Long startDate, Long endDate){
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_HIT_BY_TEMPLATE_BY_TEMPLATEN_NAME_AND_DATA_RANGE, templateName, startDate, endDate);

        String index = indexName.trim();
        List<TemplateHitPO> hits = gatewayClient.performRequest(index, typeName, dsl, TemplateHitPO.class);
        if (hits == null) {
            LOGGER.error("class=TemplateHitEsDao||method=getByTemplate||indexName={}||queryDsl={}||errMsg=list is empty",
                    index, dsl);
            return null;
        }

        return hits;
    }
}
