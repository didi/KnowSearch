package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class AriusStatsIngestInfoESDAO extends BaseAriusStatsESDAO {

    @PostConstruct
    public void init(){
        super.indexName   = dataCentreUtil.getAriusStatsIngestInfo();

        register( AriusStatsEnum.INGEST_INFO,this);
    }

    /**
     * 获取指定索引模板 一段时间的ingest 失败的指标数据
     *
     * @param logicId
     * @return
     */
    /**
     * 解析结果
        {
            "groupByTemplateId" : {
              "doc_count_error_upper_bound" : 0,
              "sum_other_doc_count" : 0,
              "buckets" : [
                {
                  "key" : 11597,
                  "doc_count" : 1760,
                  "minute_buckets" : {
                    "buckets" : [
                      {
                        "key_as_string" : "1573124520000",
                        "key" : 1573124520000,
                        "doc_count" : 176,
                        "sum_failed" : {
                          "value" : 5777.9133377075195
                        }
                      },
                      ...
                      {
                        "key_as_string" : "1573125060000",
                        "key" : 1573125060000,
                        "doc_count" : 176,
                        "sum_failed" : {
                          "value" : 4588.282709121704
                        }
                      }
                    ]
                  },
                  "avg_count_templateId" : {
                    "value" : 4507.235869669914
                  }
                }
              ]
            }
          }
     */
    public Map<Long/*templateId*/, Double> getTemplateIngestFailMetricInfo(Integer logicId, Long currentStartDate, Long currentEndDate) {
        Map<Long/*templateId*/, Double> currentFailCountMap = Maps.newHashMap();

        String indexNames = IndexNameUtils.genDailyIndexName(indexName, currentStartDate, currentEndDate);

        // 获得ingest写入错误数
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_INGEST_FAILED_COUNT_BY_LOGIC_ID_AND_TIME_RANGE, currentStartDate, currentEndDate, logicId);
        ESQueryResponse esQueryResponse = gatewayClient.performRequest(indexNames, TYPE, dsl);
        if (esQueryResponse != null && esQueryResponse.getAggs() != null) {
            Map<String, ESAggr> esAggrMap = esQueryResponse.getAggs().getEsAggrMap();

            if (esAggrMap != null && esAggrMap.containsKey("groupByTemplateId")) {
                ESAggr groupByTemplateIdESAggr = esAggrMap.get("groupByTemplateId");
                if (groupByTemplateIdESAggr != null && CollectionUtils.isNotEmpty(groupByTemplateIdESAggr.getBucketList())) {

                    for (ESBucket esBucket : groupByTemplateIdESAggr.getBucketList()) {
                        Long templateId = Long.valueOf(esBucket.getUnusedMap().get("key").toString());
                        String avgCountStr = "0.0";
                        ESAggr avgCountAggr = esBucket.getAggrMap().get("avg_count_templateId");
                        if (avgCountAggr != null && avgCountAggr.getUnusedMap() != null && avgCountAggr.getUnusedMap().get("value") != null) {
                            avgCountStr = avgCountAggr.getUnusedMap().get("value").toString();
                        }
                        currentFailCountMap.put(templateId, Double.valueOf(avgCountStr));
                    }
                }
            }
        }

        return currentFailCountMap;
    }
}
