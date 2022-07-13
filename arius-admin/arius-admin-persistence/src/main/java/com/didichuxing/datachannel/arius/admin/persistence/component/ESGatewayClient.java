package com.didichuxing.datachannel.arius.admin.persistence.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.constant.ESConstant;
import com.didichuxing.datachannel.arius.admin.common.exception.AriusGatewayException;
import com.didichuxing.datachannel.arius.admin.common.util.BaseHttpUtil;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.gateway.document.ESGetRequest;
import com.didiglobal.logi.elasticsearch.client.gateway.document.ESGetResponse;
import com.didiglobal.logi.elasticsearch.client.gateway.document.ESIndexRequest;
import com.didiglobal.logi.elasticsearch.client.request.query.query.ESQueryRequest;
import com.didiglobal.logi.elasticsearch.client.request.query.query.ESQueryRequestBuilder;
import com.didiglobal.logi.elasticsearch.client.request.query.scroll.ESQueryScrollRequest;
import com.didiglobal.logi.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggrMap;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @Author: zqr
 * es的查询类操作需要通过ESGatewayClient, 访问gateway的客户端，
 */
@Component
@NoArgsConstructor
@Data
public class ESGatewayClient {

    private static final ILog                                      LOGGER                = LogFactory
        .getLog(ESGatewayClient.class);

    /**
     * 请求gateway地址
     */
    @Value("${es.gateway.url}")
    private String                                                 gatewayUrl;
    /**
     * 请求gateway 端口号
     */
    @Value("${es.gateway.port}")
    private Integer                                                gatewayPort;

    @Value("${es.client.io.thread.count:0}")
    private Integer                                                ioThreadCount;

    /**
     * 访问索引的es user
     */
    @Value("${es.user}")
    private String                                                 esUser;
    /**
     * 访问索引的密钥
     */
    @Value("${es.password}")
    private String                                                 password;

    @Value("${scroll.timeout}")
    private String                                                 scrollTimeOut;

    private static final String                                    COMMA                 = ",";

    /**
     * 查询es的客户端
     */
    private Map<String/*esUser*/, ESClient>                        queryClientMap        = Maps.newLinkedHashMap();
    /**
     * 访问国内索引模板和es user的映射
     */
    private Map<String/*access template name*/, String/*es user*/> accessTemplateNameMap = Maps.newHashMap();
    /**
     * 认证header
     */
    private Map<String/*esUser*/, Header>                          esUserHeaderMap       = Maps.newTreeMap();

    /**
     * 初始化访问es客户端
     *
     */
    @PostConstruct
    public void init() throws AriusGatewayException {
        LOGGER.info("class=ESGatewayClient||method=init||ESGatewayClient init start.");
        // 多个es user
        String[] esUsers = StringUtils.splitByWholeSeparatorPreserveAllTokens(esUser, COMMA);
        String[] passwords = StringUtils.splitByWholeSeparatorPreserveAllTokens(password, COMMA);

        if (esUsers == null || passwords == null || esUsers.length != passwords.length) {
            throw new AriusGatewayException("please check cn gateway es user,password");
        }

        // 构建认证信息的header
        Header accessHeader = null;
        ESClient esClient = null;

        for (int i = 0; i < esUsers.length; ++i) {
            accessHeader = BaseHttpUtil.buildHttpHeader(esUsers[i], passwords[i]);
            esClient = buildGateWayClient(this.gatewayUrl, this.gatewayPort, accessHeader, "gateway");

            queryClientMap.put(esUsers[i], esClient);
            esUserHeaderMap.put(esUsers[i], accessHeader);
        }
        LOGGER.info("class=ESGatewayClient||method=init||ESGatewayClient init finished.");
    }

    /**
     * 获取gateway读写地址
     */
    public String getGatewayAddress() {
        List<String> urls = ListUtils.string2StrList(gatewayUrl);
        List<String> gateWayList = Lists.newArrayList();
        urls.forEach(url -> gateWayList.add(url + ":" + gatewayPort));
        return ListUtils.strList2String(gateWayList);
    }

    /**
     * 获取一个gateway读写地址
     */
    public String getSingleGatewayAddress() {
        List<String> urls = ListUtils.string2StrList(gatewayUrl);
        Random random = new Random();
        int n = random.nextInt(urls.size());
        return urls.get(n) + ":" + gatewayPort;
    }

    /**
     * 执行sql查询
     * @param sql
     * @return
     */
    @Nullable
    public ESQueryResponse performSQLRequest(String indexName, String sql, String orginalQuery) {
        return performSQLRequest(null, indexName, sql, orginalQuery);
    }

    /**
     * 执行sql查询
     * @param sql
     * @return
     */
    @Nullable
    public ESQueryResponse performSQLRequest(String clusterName, String indexName, String sql, String orginalQuery) {
        Tuple<String, ESClient> gatewayClientTuple = null;
        try {
            gatewayClientTuple = getGatewayClientByDataCenterAndIndexName(clusterName, indexName);

            return gatewayClientTuple.v2().prepareSQL(sql).get(new TimeValue(120, TimeUnit.SECONDS));
        } catch (Exception e) {
            LOGGER.warn(
                "class=GatewayClient||method=performSQLRequest||dataCenter={}||gatewayClientTuple={}||clusterName={}||sql={}||md5={}||errMsg=query error. ",
                EnvUtil.getDC(), JSON.toJSONString(gatewayClientTuple), clusterName, sql,
                CommonUtils.getMD5(orginalQuery), e);
            return null;
        }
    }

    /**
     * 根据查询语句获取数据
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @return
     */
    public String performRequestAndGetResponse(String indexName, String typeName, String queryDsl) {

        return performRequestAndGetResponse(null, indexName, typeName, queryDsl);
    }

    /**
     * 根据查询语句获取数据
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @return
     */
    public String performRequestAndGetResponse(String clusterName, String indexName, String typeName, String queryDsl) {

        ESQueryResponse esQueryResponse = doQuery(clusterName, indexName,
            new ESQueryRequest().indices(indexName).types(typeName).source(queryDsl));
        if (esQueryResponse == null) {
            return null;
        }

        return esQueryResponse.toJson().toJSONString();
    }

    /**
     * 执行查询
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @return
     * @throws IOException
     */
    public ESQueryResponse performRequest(String indexName, String typeName, String queryDsl) {

        return performRequest(null, indexName, typeName, queryDsl);
    }

    /**
     * 执行查询
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @return
     * @throws IOException
     */
    public ESQueryResponse performRequest(String clusterName, String indexName, String typeName, String queryDsl) {
        return doQuery(clusterName, indexName,
            new ESQueryRequest().indices(indexName).types(typeName).source(queryDsl));
    }

    public ESQueryResponse performRequestWithRouting(String clusterName, String routing, String indexName,
                                                     String typeName, String queryDsl) {
        return doQuery(clusterName, indexName,
            new ESQueryRequest().indices(indexName).routing(routing).types(typeName).source(queryDsl));
    }

    /**
     * 获取命中总数
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @return
     */
    public Long performRequestAndGetTotalCount(String indexName, String typeName, String queryDsl) {
        return performRequestAndGetTotalCount(null, indexName, typeName, queryDsl);
    }

    /**
     * 获取命中总数
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @return
     */
    public Long performRequestAndGetTotalCount(String clusterName, String indexName, String typeName, String queryDsl,
                                               int tryTimes) {
        return performRequest(clusterName, indexName, typeName, queryDsl, esQueryResponse -> {
            if (null == esQueryResponse || esQueryResponse.getHits() == null) {
                return 0L;
            }
            return Long
                .valueOf(esQueryResponse.getHits().getUnusedMap().getOrDefault(ESConstant.HITS_TOTAL, "0").toString());
        }, tryTimes);
    }

    /**
     * 获取命中总数
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @return
     */
    public Long performRequestAndGetTotalCount(String clusterName, String indexName, String typeName, String queryDsl) {
        ESQueryResponse esQueryResponse = performRequest(clusterName, indexName, typeName, queryDsl);
        if (null == esQueryResponse || esQueryResponse.getHits() == null) {
            return 0L;
        }

        return Long
            .valueOf(esQueryResponse.getHits().getUnusedMap().getOrDefault(ESConstant.HITS_TOTAL, "0").toString());
    }

    /**
     * 根据查询语句获取数据
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @param clzz
     * @param <T>
     * @return
     */
    public <T> List<T> performRequest(String indexName, String typeName, String queryDsl, Class<T> clzz) {
        return performRequest(null, indexName, typeName, queryDsl, clzz);
    }

    /**
     * 根据dsl语句写数据
     *
     * @param templateName 模版名称
     * @param typeName 类型
     * @param dsl 数据
     */
    public void performWriteRequest(String templateName, String typeName, String dsl) {
        performWriteRequest(null, templateName, typeName, dsl);
    }

    /**
     * 根据dsl语句写数据
     *
     * @param clusterName 集群名字
     * @param templateName 模版名称（目前通过gateway写入只支持传模版，不能具体某一个日期的索引）
     * @param typeName 类型
     * @param dsl 数据
     */
    public void performWriteRequest(String clusterName, String templateName, String typeName, String dsl) {
        doWrite(clusterName, templateName, new ESIndexRequest().index(templateName).type(typeName).source(dsl));
    }

    /**
     * 根据查询语句获取数据
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @param clzz
     * @param <T>
     * @return
     */
    public <T> List<T> performRequest(String clusterName, String indexName, String typeName, String queryDsl,
                                      Class<T> clzz) {
        ESQueryResponse esQueryResponse = doQuery(clusterName, indexName,
            new ESQueryRequest().indices(indexName).types(typeName).source(queryDsl).clazz(clzz));
        if (esQueryResponse == null) {
            return new ArrayList<>();
        }

        List<Object> objectList = esQueryResponse.getSourceList();
        if (CollectionUtils.isEmpty(objectList)) {
            return new ArrayList<>();
        }

        List<T> hits = Lists.newLinkedList();
        for (Object obj : objectList) {
            hits.add((T) obj);
        }

        return hits;
    }

    public <R> R performRequest(String indexName, String typeName, String queryDsl, Function<ESQueryResponse, R> func,
                                int tryTimes) {
        return performRequest(null, indexName, typeName, queryDsl, func, tryTimes);
    }

    public <R> R performRequest(String clusterName, String indexName, String typeName, String queryDsl,
                                Function<ESQueryResponse, R> func, int tryTimes) {
        ESQueryResponse esQueryResponse;
        do {
            esQueryResponse = doQuery(clusterName, indexName,
                new ESQueryRequest().indices(indexName).types(typeName).source(queryDsl));
        } while (tryTimes-- > 0 && null == esQueryResponse);

        if (!EnvUtil.isOnline()) {
            LOGGER.warn("class=GatewayClient||method=performRequest||dataCenter={}||indexName={}||queryDsl={}||ret={}",
                EnvUtil.getDC(), indexName, queryDsl, JSON.toJSONString(esQueryResponse));
        }

        return func.apply(esQueryResponse);
    }

    public <R> R performRequestWithRouting(String clusterName, String routingValue, String indexName, String typeName,
                                           String queryDsl, Function<ESQueryResponse, R> func, int tryTimes) {
        ESQueryResponse esQueryResponse;
        do {
            esQueryResponse = doQuery(clusterName, indexName,
                new ESQueryRequest().routing(routingValue).indices(indexName).types(typeName).source(queryDsl));
        } while (tryTimes-- > 0 && null == esQueryResponse);

        if (!EnvUtil.isOnline()) {
            LOGGER.warn(
                "class=GatewayClient||method=performRequestWithRouting||dataCenter={}||indexName={}||queryDsl={}||ret={}",
                EnvUtil.getDC(), indexName, queryDsl, JSON.toJSONString(esQueryResponse));
        }

        return func.apply(esQueryResponse);
    }

    /**
     * 查询并获取第一个元素
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @param clzz
     * @param <T>
     * @return
     */
    public <T> T performRequestAndTakeFirst(String indexName, String typeName, String queryDsl, Class<T> clzz) {
        return performRequestAndTakeFirst(null, indexName, typeName, queryDsl, clzz);
    }

    /**
     * 查询并获取第一个元素
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @param clzz
     * @param <T>
     * @return
     */
    public <T> T performRequestAndTakeFirst(String clusterName, String indexName, String typeName, String queryDsl,
                                            Class<T> clzz) {
        List<T> hits = performRequest(clusterName, indexName, typeName, queryDsl, clzz);

        if (CollectionUtils.isEmpty(hits)) {
            return null;
        }

        return hits.get(0);
    }

    /**
     * 获取命中总数和第一条记录
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @param clzz
     * @param <T>
     * @return
     */
    public <T> Tuple<Long, T> performRequestAndGetTotalCount(String indexName, String typeName, String queryDsl,
                                                             Class<T> clzz) {
        return performRequestAndGetTotalCount(null, indexName, typeName, queryDsl, clzz);
    }

    /**
     * 获取命中总数和第一条记录
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @param clzz
     * @param <T>
     * @return
     */
    public <T> Tuple<Long, T> performRequestAndGetTotalCount(String clusterName, String indexName, String typeName,
                                                             String queryDsl, Class<T> clzz) {
        ESQueryResponse esQueryResponse = doQuery(clusterName, indexName,
            new ESQueryRequest().indices(indexName).types(typeName).source(queryDsl).clazz(clzz));
        if (esQueryResponse == null) {
            return null;
        }

        List<Object> objectList = esQueryResponse.getSourceList();
        if (CollectionUtils.isEmpty(objectList)) {
            return null;
        }

        return new Tuple<>(
            Long.valueOf(esQueryResponse.getHits().getUnusedMap().getOrDefault(ESConstant.HITS_TOTAL, "0").toString()),
            (T) objectList.get(0));
    }

    /**
     * 执行查询
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @return
     * @throws IOException
     */
    @Nullable
    public ESAggrMap performAggRequest(String indexName, String typeName, String queryDsl) {
        return performAggRequest(null, indexName, typeName, queryDsl);
    }

    /**
     * 执行查询
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @return
     * @throws IOException
     */
    @Nullable
    public ESAggrMap performAggRequest(String clusterName, String indexName, String typeName, String queryDsl) {
        ESQueryResponse esQueryResponse = doQuery(clusterName, indexName,
            new ESQueryRequest().indices(indexName).types(typeName).source(queryDsl));
        if (esQueryResponse == null || esQueryResponse.getAggs() == null) {
            return null;
        }

        return esQueryResponse.getAggs();
    }

    /**
     * 执行查询
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @return
     * @throws IOException
     */
    @Nullable
    public ESAggrMap performAggRequestWithPreference(String indexName, String typeName, String queryDsl,
                                                     String preference) {
        return performAggRequest(null, indexName, typeName, queryDsl, preference);
    }

    /**
     * 获取命中总数和返回查询记录
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @param clzz
     * @param <T>
     * @return
     */
    public <T> Tuple<Long, List<T>> performRequestListAndGetTotalCount(String clusterName, String indexName,
                                                                       String typeName, String queryDsl,
                                                                       Class<T> clzz) {
        ESQueryResponse esQueryResponse = doQuery(clusterName, indexName,
            new ESQueryRequest().indices(indexName).types(typeName).source(queryDsl).clazz(clzz));
        if (esQueryResponse == null) {
            return null;
        }

        List<Object> objectList = esQueryResponse.getSourceList();
        if (CollectionUtils.isEmpty(objectList)) {
            return null;
        }

        List<T> hits = Lists.newLinkedList();
        for (Object obj : objectList) {
            hits.add((T) obj);
        }

        return new Tuple<>(
            Long.valueOf(esQueryResponse.getHits().getUnusedMap().getOrDefault(ESConstant.HITS_TOTAL, "0").toString()),
            hits);
    }

    /**
     * 执行查询
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @return
     * @throws IOException
     */
    @Nullable
    public ESAggrMap performAggRequest(String clusterName, String indexName, String typeName, String queryDsl,
                                       String preference) {
        ESQueryResponse esQueryResponse = doQuery(clusterName, indexName,
            new ESQueryRequest().indices(indexName).types(typeName).source(queryDsl).preference(preference));
        if (esQueryResponse == null || esQueryResponse.getAggs() == null) {
            return null;
        }

        return esQueryResponse.getAggs();
    }

    /**
     * 准备滚动查询
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @param clzz
     * @param scrollResultVisitor
     * @param <T>
     * @return
     */
    public <T> ESQueryResponse prepareScrollQuery(String indexName, String typeName, String queryDsl, String preference,
                                                  Class<T> clzz, ScrollResultVisitor<T> scrollResultVisitor) {

        return prepareScrollQuery(null, indexName, typeName, queryDsl, preference, clzz, scrollResultVisitor);
    }

    /**
     * 准备滚动查询
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @param clzz
     * @param scrollResultVisitor
     * @param <T>
     * @return
     */
    public <T> ESQueryResponse prepareScrollQuery(String clusterName, String indexName, String typeName,
                                                  String queryDsl, String preference, Class<T> clzz,
                                                  ScrollResultVisitor<T> scrollResultVisitor) {
        ESQueryResponse esQueryResponse = null;
        ESQueryRequestBuilder builder = null;

        Tuple<String, ESClient> gatewayClientTuple = null;
        gatewayClientTuple = getGatewayClientByDataCenterAndIndexName(clusterName, indexName);
        builder = gatewayClientTuple.v2().prepareQuery(indexName).setTypes(typeName).setClazz(clzz).setSource(queryDsl)
            .setScroll(new TimeValue(60000));

        // 如果指定了preference
        if (StringUtils.isNotBlank(preference)) {
            builder = builder.preference(preference);
        }
        esQueryResponse = builder.execute().actionGet(120, TimeUnit.SECONDS);

        if (esQueryResponse == null) {
            return null;
        }

        List<Object> objectList = esQueryResponse.getSourceList();
        if (objectList == null) {
            return esQueryResponse;
        }

        List<T> hits = Lists.newLinkedList();
        for (Object obj : objectList) {
            hits.add((T) obj);
        }

        scrollResultVisitor.handleScrollResult(hits);

        return esQueryResponse;
    }

    /**
     * 再次滚动查询
     *
     * @param scrollId
     * @param clzz
     * @param scrollResultVisitor
     * @param <T>
     * @return
     */
    public <T> ESQueryResponse queryScrollQuery(String indexName, String scrollId, Class<T> clzz,
                                                ScrollResultVisitor<T> scrollResultVisitor) {
        return queryScrollQuery(null, indexName, scrollId, clzz, scrollResultVisitor);
    }

    /**
     * 再次滚动查询
     *
     * @param scrollId
     * @param clzz
     * @param scrollResultVisitor
     * @param <T>
     * @return
     */
    public <T> ESQueryResponse queryScrollQuery(String clusterName, String indexName, String scrollId, Class<T> clzz,
                                                ScrollResultVisitor<T> scrollResultVisitor) {
        ESQueryResponse esQueryResponse = null;
        ESQueryScrollRequest queryScrollRequest = new ESQueryScrollRequest();
        queryScrollRequest.setScrollId(scrollId).scroll(new TimeValue(60000));
        queryScrollRequest.clazz(clzz);

        Tuple<String, ESClient> gatewayClientTuple = null;
        gatewayClientTuple = getGatewayClientByDataCenterAndIndexName(clusterName, indexName);
        esQueryResponse = gatewayClientTuple.v2().queryScroll(queryScrollRequest).actionGet(120, TimeUnit.SECONDS);

        if (esQueryResponse == null) {
            return null;
        }

        List<Object> objectList = esQueryResponse.getSourceList();
        if (objectList == null) {
            return esQueryResponse;
        }

        List<T> hits = Lists.newLinkedList();
        for (Object obj : objectList) {
            hits.add((T) obj);
        }

        scrollResultVisitor.handleScrollResult(hits);

        return esQueryResponse;
    }

    /**
     * 使用滚动查询方式
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @param scrollSize
     * @param clzz
     * @return
     */
    public <T> void queryWithScroll(String indexName, String typeName, String queryDsl, int scrollSize,
                                    String preference, Class<T> clzz, ScrollResultVisitor<T> scrollResultVisitor) {
        queryWithScroll(null, indexName, typeName, queryDsl, scrollSize, preference, clzz, scrollResultVisitor);
    }

    /**
     * 使用滚动查询方式
     *
     * @param indexName
     * @param typeName
     * @param queryDsl
     * @param scrollSize
     * @param clzz
     * @return
     */
    public <T> void queryWithScroll(String clusterName, String indexName, String typeName, String queryDsl,
                                    int scrollSize, String preference, Class<T> clzz,
                                    ScrollResultVisitor<T> scrollResultVisitor) {
        ESQueryResponse esQueryResponse = null;
        try {
            esQueryResponse = prepareScrollQuery(clusterName, indexName, typeName, queryDsl, preference, clzz,
                scrollResultVisitor);
        } catch (Exception e) {
            LOGGER.warn(
                "class=GatewayClient||method=queryWithScroll||dataCenter={}||indexName={}||queryDsl={}||errMsg=query error. ",
                EnvUtil.getDC(), indexName, queryDsl, e);
        }

        if (esQueryResponse == null) {
            return;
        }

        long totalCount = Long
            .parseLong(esQueryResponse.getHits().getUnusedMap().getOrDefault(ESConstant.HITS_TOTAL, "0").toString());
        int scrollCnt = (int) Math.ceil((double) totalCount / scrollSize);

        for (int scrollIndex = 0; scrollIndex < scrollCnt - 1; ++scrollIndex) {
            if (esQueryResponse == null) {
                continue;
            }

            String scrollId = esQueryResponse.getUnusedMap().get("_scroll_id").toString();

            try {
                esQueryResponse = queryScrollQuery(clusterName, indexName, scrollId, clzz, scrollResultVisitor);
            } catch (Exception e) {
                LOGGER.warn(
                    "class=GatewayClient||method=queryWithScroll||dataCenter={}||scrollId={}||errMsg=query error. ",
                    EnvUtil.getDC(), scrollId, e);
            }
        }

    }

    /**
     * 根据主键获取
     *
     * @param indexName
     * @param typeName
     * @param id
     * @param clzz
     * @param <T>
     * @return
     */
    public <T> T doGet(String indexName, String typeName, String id, Class<T> clzz) {
        return doGet(null, indexName, typeName, id, clzz);
    }

    /**
     * 根据主键获取
     *
     * @param indexName
     * @param typeName
     * @param id
     * @param clzz
     * @param <T>
     * @return
     */
    public <T> T doGet(String clusterName, String indexName, String typeName, String id, Class<T> clzz) {
        ESGetRequest request = new ESGetRequest();
        request.index(indexName).type(typeName).id(id);

        ESGetResponse response = null;
        Tuple<String, ESClient> gatewayClientTuple = null;
        try {
            gatewayClientTuple = getGatewayClientByDataCenterAndIndexName(clusterName, indexName);
            response = gatewayClientTuple.v2().get(request).actionGet(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn(
                "class=GatewayClient||method=doGet||dataCenter={}||gatewayClientTuple={}||indexName={}||typeName={}||id={}||errMsg=get error. ",
                EnvUtil.getDC(), JSON.toJSONString(gatewayClientTuple), indexName, typeName, id, e);
        }

        if (response == null) {
            return null;
        }

        T obj = null;
        try {
            obj = JSON.parseObject(JSON.toJSONString(response.getSource()), clzz);
        } catch (JSONException e) {
            LOGGER.warn(
                "class=GatewayClient||method=doGet||dataCenter={}||indexName={}||typeName={}||id={}||clzz={}||errMsg=fail to parse json. ",
                EnvUtil.getDC(), indexName, typeName, id, clzz, e);
        }

        return obj;
    }

    /**
     * 执行查询
     * @param queryRequest
     * @return
     */
    @Nullable
    private ESQueryResponse doQuery(String clusterName, String indexName, ESQueryRequest queryRequest) {
        Tuple<String, ESClient> gatewayClientTuple = null;
        try {
            gatewayClientTuple = getGatewayClientByDataCenterAndIndexName(clusterName, indexName);

            return gatewayClientTuple.v2().query(queryRequest).actionGet(120, TimeUnit.SECONDS);
        } catch (Exception e) {
            String queryDsl = bytesReferenceConvertDsl(queryRequest.source());
            LOGGER.warn(
                "class=GatewayClient||method=doQuery||dataCenter={}||gatewayClientTuple={}||clusterName={}||indexName={}||queryDsl={}||md5={}||errMsg=query error. ",
                EnvUtil.getDC(), JSON.toJSONString(gatewayClientTuple), clusterName, queryRequest.indices(), queryDsl,
                CommonUtils.getMD5(queryDsl), e);
            return null;
        }
    }

    /**
     * @param clusterName 集群名字
     * @param templateName 模版名称（目前通过gateway写入只支持传模版，不能具体某一个日期的索引）
     * @param indexRequest indexRequest
     */
    private void doWrite(String clusterName, String templateName, ESIndexRequest indexRequest) {
        Tuple<String, ESClient> gatewayClientTuple = null;
        try {
            gatewayClientTuple = getGatewayClientByDataCenterAndIndexName(clusterName, templateName);
            gatewayClientTuple.v2().index(indexRequest);
        } catch (Exception e) {
            String dsl = bytesReferenceConvertDsl(indexRequest.source());
            LOGGER.warn(
                "class=GatewayClient||method=doWrite||dataCenter={}||gatewayClientTuple={}||clusterName={}||indexName={}||queryDsl={}||md5={}||errMsg=query error. ",
                EnvUtil.getDC(), JSON.toJSONString(gatewayClientTuple), clusterName, indexRequest.index(), dsl,
                CommonUtils.getMD5(dsl), e);
        }
    }

    /**
     * 根据机房和索引名称获取访问gateway客户端
     *
     * @param indexName
     * @return
     */
    private Tuple<String, ESClient> getGatewayClientByDataCenterAndIndexName(String clusterName, String indexName) {
        // 默认第一个esUser
        String esUser = queryClientMap.keySet().iterator().next();

        // 只有一个es user时或者没传索引名称时，直接返回第一个；配置多个es user时，根据访问索引名称进行选择
        if (queryClientMap.size() > 1 && StringUtils.isNotBlank(indexName)) {
            for (Map.Entry<String/*access template name*/, String/*esUser*/> entry : accessTemplateNameMap.entrySet()) {
                // 去掉索引表达式最后的*，如果访问的索引名称以表达式开头，则返回该es user
                String accessTemplateName = StringUtils.removeEnd(entry.getKey(), "*");
                if (StringUtils.isNotBlank(accessTemplateName) && indexName.startsWith(accessTemplateName)) {
                    esUser = entry.getValue();
                    break;
                }
            }
        }

        ESClient esClient = queryClientMap.get(esUser);

        if (!EnvUtil.isOnline()) {
            LOGGER.info("class=GatewayClient||method=getGatewayClientByDataCenterAndIndexName||esUser={}||indexName={}",
                esUser, indexName);
        }

        Header esUserHeader = esUserHeaderMap.get(esUser);
        if (esClient != null) {
            List<Header> headers = Lists.newArrayList();
            if (esUserHeader != null) {
                // 添加认证头
                headers.add(esUserHeader);

                // 添加指定集群访问头
                if (StringUtils.isNotBlank(clusterName)) {
                    Header clusterNameHeader = new BasicHeader("CLUSTER_ID", clusterName);
                    headers.add(clusterNameHeader);
                }

                esClient.setHeaders(headers);
            }
        }

        return new Tuple<>(esUser, esClient);
    }

    private ESClient buildGateWayClient(String url, Integer port, Header header, String clusterName) {
        String[] ipArray = null;
        TransportAddress[] transportAddresses = null;
        ESClient esClient = null;
        // 构建查询客户端
        ipArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(url, COMMA);
        if (ipArray != null && ipArray.length > 0) {
            try {
                esClient = new ESClient();
                transportAddresses = new TransportAddress[ipArray.length];
                for (int j = 0; j < ipArray.length; ++j) {
                    transportAddresses[j] = new InetSocketTransportAddress(new InetSocketAddress(ipArray[j], port));
                }
                esClient.addTransportAddresses(transportAddresses);
                if (header != null) {
                    esClient.setHeader(header);
                }
                if (StringUtils.isNotBlank(clusterName)) {
                    esClient.setClusterName(clusterName);
                }

                if (ioThreadCount > 0) {
                    esClient.setIoThreadCount(ioThreadCount);
                }

                // 配置http超时
                esClient.setRequestConfigCallback(builder -> builder.setConnectTimeout(10000).setSocketTimeout(120000)
                    .setConnectionRequestTimeout(120000));
                esClient.start();
            } catch (Exception e) {
                if (null != esClient) {
                    esClient.close();
                }

                LOGGER.error("class=ESGatewayClient||method=buildGateWayClient||errMsg={}||url={}||port={}",
                    e.getMessage(), url, port, e);
                return null;
            }
        }

        return esClient;
    }

    /**
     * 转换dsl语句
     *
     * @param bytes
     * @return
     */
    private String bytesReferenceConvertDsl(BytesReference bytes) {
        try {
            return XContentHelper.convertToJson(bytes, false);
        } catch (IOException e) {
            LOGGER.warn("class=CommonUtils||method=bytesReferenceConvertDsl||errMsg=fail to covert", e);
        }

        return "";
    }
}