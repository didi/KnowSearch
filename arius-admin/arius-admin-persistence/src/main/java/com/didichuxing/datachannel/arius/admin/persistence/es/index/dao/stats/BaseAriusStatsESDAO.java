package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class BaseAriusStatsESDAO extends BaseESDAO {

    /**
     * 操作的索引名称
     */
    protected String indexName;

    /**
     * 索引type名称为type
     */
    protected final static String TYPE = "type";

    public static final String TPS_METRICS = "indexing-index_total_rate";
    public static final String QPS_METRICS = "search-query_total_rate";
    public static final String OS_CPU      = "os-cpu_percent";
    public final static String METRICS_TRANS_TX         = "transport-tx_size_in_bytes_rate";
    public final static String METRICS_TRANS_RX         = "transport-rx_size_in_bytes_rate";

    public final static int SCROLL_SIZE = 5000;
    public final static Long  ONE_GB               = 1024 * 1024 * 1024L;

    /**
     * 不同维度es监控数据
     */
    private static Map<AriusStatsEnum/*stats type*/, BaseAriusStatsESDAO> ariusStatsEsDaoMap = Maps.newConcurrentMap();

    public static BaseAriusStatsESDAO getByStatsType(AriusStatsEnum statsType){
        return ariusStatsEsDaoMap.get(statsType);
    }

    /**
     * 注册不同维度数据对应操作的es类
     *
     * @param statsType
     * @param baseAriusStatsEsDao
     */
    public static void register(AriusStatsEnum statsType, BaseAriusStatsESDAO baseAriusStatsEsDao) {
        ariusStatsEsDaoMap.put(statsType, baseAriusStatsEsDao);
    }

    /**
     * 批量插入索引统计信息

     * @param statsInfo
     * @return
     */
    public boolean batchInsertStats(List<? extends BaseESPO> statsInfo) {
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);
        return updateClient.batchInsert(realIndex, TYPE, statsInfo);
    }

    /**
     * 从es返回结果中得到求和值
     *
     * @param esQueryResponse
     * @param sumKey
     * @return
     */
    public Double getSumFromESQueryResponse(ESQueryResponse esQueryResponse, String sumKey) {
        if(null == esQueryResponse || esQueryResponse.getAggs() == null) {
            LOGGER.error("class=BaseAriusStatsEsDao||method=getSumFromESQueryResponse||msg=esQueryResponse is null");
            return -1d;
        }

        String value = null;
        try {
            Map<String, ESAggr> esAggrMap = esQueryResponse.getAggs().getEsAggrMap();

            if (esAggrMap != null && esAggrMap.containsKey(sumKey) && esAggrMap.get(sumKey).getUnusedMap() != null
                    && esAggrMap.get(sumKey).getUnusedMap().containsKey("value")) {

                if (esAggrMap.get(sumKey).getUnusedMap().get("value") != null) {
                    value = esAggrMap.get(sumKey).getUnusedMap().get("value").toString();
                    return Double.valueOf(value);
                }

            }

        } catch (Exception e){
            LOGGER.error("class=BaseAriusStatsEsDao||method=getSumFromESQueryResponse||sumKey={}||value={}||esQueryResponse={}", sumKey, value, esQueryResponse, e);
        }
        return 0d;
    }

    /**
     * 生成查询的索引名称，默认7天
     *
     * @param indexCount
     * @return
     */
    public String genIndexNames(Integer indexCount) {
        try {
            if (indexCount == null) {
                indexCount = 7;
            }

            List<String> indices = Lists.newArrayList();

            for (int day = 0; day < indexCount; day++) {
                String indexName = this.indexName.concat("_").concat(DateTimeUtil.getFormatDayByOffset(day)).concat("*");
                indices.add(indexName);
            }
            return StringUtils.join(indices, ",");

        } catch (Exception e) {
            LOGGER.error("class=BaseAriusStatsEsDao||method=genIndexNames||errMsg=gen last 7 days index names error.||stack={}",
                    e);
        }

        // 异常时查询所有时间段的索引
        return this.indexName.concat("*");
    }
}
