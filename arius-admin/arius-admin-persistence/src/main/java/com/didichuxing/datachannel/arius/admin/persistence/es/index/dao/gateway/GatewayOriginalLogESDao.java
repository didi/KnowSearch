//package com.didichuxing.arius.admin.extend.metadata.dao.es.gateway;
//
//import GatewayOriginalLogPO;
//import com.didichuxing.datachannel.arius.admin.common.constant.ESConstant;
//import com.didichuxing.datachannel.arius.admin.persistence.component.ScrollResultVisitor;
//import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
//import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
//import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESAggr;
//import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESAggrMap;
//import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESBucket;
//import org.apache.commons.collections4.CollectionUtils;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.TreeSet;
//
//@Component
//public class GatewayOriginalLogESDao extends BaseESDAO {
//
//    /**
//     * gateway 原始日志索引
//     */
//    private String indexName;
//
//    @PostConstruct
//    public void init(){
//        this.indexName = dataCentreUtil.getAriusGatewayOriginalLog();
//    }
//
//    /**
//     * type名称
//     */
//    private String typeName = null;
//
//    /**
//     * 根据时间获取访问原始查询日志
//     *
//     * @param indexDate
//     * @param startTime
//     * @param endTime
//     * @param scrollResultVisitor
//     */
//    public void scrollByLogTime(String indexDate,
//                                Long startTime, Long endTime,
//                                ScrollResultVisitor<GatewayOriginalLogPO> scrollResultVisitor) {
//        String indexName = getSearchIndexName(indexDate);
//        int scrollSize = 1000;
//
//        String dsl = dslLoaderUtil.getFormatDslByFileName( DslsConstant.SCROLL_GATEWAY_BY_LOGTIME, scrollSize, startTime, endTime);
//
//        gatewayClient.queryWithScroll(indexName, typeName, dsl, scrollSize, null, GatewayOriginalLogPO.class, scrollResultVisitor);
//    }
//
//    /**
//     * 根据shard编号获取原始查询日志
//     *
//     * @param indexDate
//     * @param shardNo
//     * @param scrollResultVisitor
//     */
//    public void scrollByShardNo(String indexDate, Integer shardNo,
//                                ScrollResultVisitor<GatewayOriginalLogPO> scrollResultVisitor) {
//        String indexName = getSearchIndexName(indexDate);
//        int scrollSize = 5000;
//
//        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_GATEWAY_BY_SHARDNO, scrollSize);
//
//        gatewayClient.queryWithScroll(indexName, typeName, dsl, scrollSize, String.format("_shards:%d", shardNo), GatewayOriginalLogPO.class, scrollResultVisitor);
//    }
//
//    /**
//     * 得到查询使用的索引名称
//     *
//     * @param indexDate
//     * @return
//     */
//    public String getSearchIndexName(String indexDate) {
//        return this.indexName.concat("_").concat(indexDate);
//    }
//
//    /**
//     * 获取查询使用的索引所在集群
//     *
//     * @param indexTemplateMap
//     * @return
//     */
//    public String getSearchTemplateClusterName(Map<String/*templateName*/, List<IndexTemplate>> indexTemplateMap) {
//        String templateName = this.indexName;
//
//        List<IndexTemplate> indexTemplateList = indexTemplateMap.get(templateName);
//        if (CollectionUtils.isEmpty(indexTemplateList)) {
//            return null;
//        }
//
//        return indexTemplateList.get(0).getCluster();
//    }
//
//    public Set<Integer> getTcpAppidList() throws Exception {
//        String templateName = this.indexName;
//
//        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TCP_APPID_LIST);
//
//        ESAggrMap result = gatewayClient.performAggRequest(templateName + "*", null, dsl);
//
//        Set<Integer> appids = new TreeSet<>();
//
//        if (result == null) {
//            return appids;
//        }
//
//        ESAggr esAggr = result.getEsAggrMap().get("appid");
//
//        for (ESBucket esBucket : esAggr.getBucketList()) {
//            if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
//                continue;
//            }
//            String appid = esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();
//            appids.add(Integer.valueOf(appid));
//        }
//
//        return appids;
//    }
//}
