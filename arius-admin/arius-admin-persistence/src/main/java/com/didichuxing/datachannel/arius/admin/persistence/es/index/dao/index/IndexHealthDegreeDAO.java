package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.po.index.healthdegree.HealthDegreesPO;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@NoArgsConstructor
public class IndexHealthDegreeDAO extends BaseESDAO {

    /**
     * 索引名称
     */
    private String indexName;

    private static final String TYPE = "score";

    @PostConstruct
    public void init(){
        this.indexName = dataCentreUtil.getAriusIndexHealthDegress();
    }

    public boolean batchInsertHealthDegress(List<HealthDegreesPO> infos){
        return updateClient.batchInsert(genCurrentDayIndex(), TYPE, infos);
    }

    public boolean insertHealthDegress(HealthDegreesPO info){
        return updateClient.index(genCurrentDayIndex(), TYPE,null, JSON.toJSONString(info));
    }

    /**
     * 获取模板一天的健康分
     * @param template
     * @param offset 相对于当天的偏移，如：offset=1，则是获取昨天的健康分；offset=2，则是获取前天的健康分
     * @return
     */
    public double getTemplateOneDayAvgDegree(String template, int offset){
        Long startTime = DateTimeUtil.getZeroDate(new Date(), offset).getTime();
        Long endTime   = DateTimeUtil.getZeroDate(new Date(), offset - 1).getTime();

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_ONE_DAY_AVG_DEGREE,
                startTime, endTime, template);

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequest(realIndexName, TYPE, dsl, s -> {
            if(null == s){return 0d;}

            try {
                Map<String, ESAggr> esAggrMap = s.getAggs().getEsAggrMap();
                return Double.valueOf(esAggrMap.get("totalScoreAvg").getUnusedMap().get("value").toString());
            }catch (Exception e){
                LOGGER.error("class=IndexHealthDegreeDao||method=getTemplateOneDayAvgDegree||template={}||offset={}", template, offset, e);
            }
            return 0d;
        }, 3);
    }

    public double getTemplateAvgDegree(Long logicTemplateId, Long startDate, Long endDate){
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_ONE_DAY_AVG_DEGREE_AND_TEMPLATE_ID,
                startDate, endDate, logicTemplateId);

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startDate, endDate);

        return gatewayClient.performRequest(realIndexName, TYPE, dsl, s -> {
            if(null == s){return 0d;}

            try {
                Map<String, ESAggr> esAggrMap = s.getAggs().getEsAggrMap();
                return Double.valueOf(esAggrMap.get("totalScoreAvg").getUnusedMap().get("value").toString());
            }catch (Exception e){
                LOGGER.error("class=IndexHealthDegreeDao||method=getTemplateOneDayAvgDegree||logicTemplateId={}||startDate={}||endDate={}",
                        logicTemplateId, startDate, endDate, e);
            }
            return 0d;
        }, 3);
    }

    public List<HealthDegreesPO> getIndexHealthDegree(Long logicTemplateId, Long startDate, Long endDate){
        final int scrollSize = 5000;

        String dsl = dslLoaderUtil.getFormatDslByFileName( DslsConstant.GET_TEMPLATE_HEALTH_DEGREE_BY_RANGE,
                scrollSize, startDate, endDate, logicTemplateId);

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startDate, endDate);

        List<HealthDegreesPO> healthDegreesPOS = Lists.newLinkedList();
        gatewayClient.queryWithScroll(realIndexName,
                TYPE, dsl, scrollSize, null, HealthDegreesPO.class, resultList -> {
                    if (resultList != null) {
                        healthDegreesPOS.addAll(resultList);
                    }
                });

        return healthDegreesPOS;
    }

    /********************************************* private methods *********************************************/
    private String genCurrentDayIndex() {
        return IndexNameUtils.genDailyIndexName(indexName, 0);
    }
}
