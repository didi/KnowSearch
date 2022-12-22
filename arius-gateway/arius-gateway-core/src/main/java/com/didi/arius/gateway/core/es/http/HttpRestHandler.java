package com.didi.arius.gateway.core.es.http;

import static com.didi.arius.gateway.common.consts.RestConsts.SCROLL_SPLIT;
import static com.didi.arius.gateway.elasticsearch.client.utils.LogUtils.setWriteLog;

import java.util.*;
import java.util.stream.Collectors;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.support.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.consts.RestConsts;
import com.didi.arius.gateway.common.enums.TemplateBlockTypeEnum;
import com.didi.arius.gateway.common.exception.FlowLimitException;
import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.JoinLogContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.es.http.action.reindex.RestDeleteByQueryAction;
import com.didi.arius.gateway.core.es.http.action.reindex.RestUpdateByQueryAction;
import com.didi.arius.gateway.core.es.http.bulk.RestBulkAction;
import com.didi.arius.gateway.core.es.http.count.RestCountAction;
import com.didi.arius.gateway.core.es.http.document.RestBaseWriteAction;
import com.didi.arius.gateway.core.es.http.get.RestBaseGetAction;
import com.didi.arius.gateway.core.es.http.get.RestHeadAction;
import com.didi.arius.gateway.core.es.http.get.RestMultiGetAction;
import com.didi.arius.gateway.core.es.http.search.*;
import com.didi.arius.gateway.core.es.http.sql.SQLAction;
import com.didi.arius.gateway.core.service.ESRestClientService;
import com.didi.arius.gateway.core.service.MetricsService;
import com.didi.arius.gateway.core.service.RateLimitService;
import com.didi.arius.gateway.core.service.RequestStatsService;
import com.didi.arius.gateway.core.service.arius.AppService;
import com.didi.arius.gateway.core.service.arius.DynamicConfigService;
import com.didi.arius.gateway.core.service.arius.ESClusterService;
import com.didi.arius.gateway.core.service.arius.IndexTemplateService;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectResponse;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchResponse;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.response.Failure;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.response.src.Hit;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.google.common.collect.Lists;

public abstract class HttpRestHandler extends ESBase {
    protected static final ILog logger = LogFactory.getLog(HttpRestHandler.class);
    protected static final Logger statLogger = LoggerFactory.getLogger(QueryConsts.STAT_LOGGER);

    @Autowired
    protected DynamicConfigService dynamicConfigService;

    @Autowired
    protected RequestStatsService requestStatsService;

    @Autowired
    protected ESClusterService    esClusterService;

    @Autowired
    protected ESRestClientService esRestClientService;

    @Autowired
    protected QueryConfig queryConfig;

    @Autowired
    protected RateLimitService rateLimitService;

    @Autowired
    protected MetricsService metricsService;

    @Autowired
    protected IndexTemplateService indexTemplateService;

    @Autowired
    protected AppService appService;

    public abstract void handleRequest(QueryContext queryContext) throws Exception;

    public abstract String name();

    protected void checkFlowLimit(QueryContext queryContext) {
        if (rateLimitService.isTrafficDataOverflow(queryContext.getAppDetail().getId(), queryContext.getSearchId())) {
            throw new FlowLimitException("query flow limit, please try again!");
        }
    }

    protected void checkIndices(QueryContext queryContext) {
        List<String> indices = queryContext.getIndices();
        indexTemplateService.checkTemplateExist(indices);
        appService.checkIndices(queryContext, indices);
    }

    protected void checkIndicesAndTemplateBlockRead(QueryContext queryContext) {
        List<String> indices = queryContext.getIndices();
        indexTemplateService.checkTemplateBlock(indices, queryContext.getAppDetail(), TemplateBlockTypeEnum.READ_BLOCK_TYPE);
        appService.checkIndices(queryContext, indices);
    }

    protected boolean isOriginCluster(QueryContext queryContext){
        return queryContext.getSearchType() == AppDetail.RequestType.ORIGIN_CLUSTER.getType();
    }

    protected void checkWriteIndicesAndTemplateBlockWrite(QueryContext queryContext) {
        List<String> indices = queryContext.getIndices();
        indexTemplateService.checkTemplateBlock(indices, queryContext.getAppDetail(), TemplateBlockTypeEnum.WRITE_WRITE_TYPE);
        appService.checkWriteIndices(queryContext, indices);
    }

    protected void logSearchResponse(QueryContext queryContext, ESSearchResponse queryResponse) {
        if (queryResponse == null) {
            return ;
        }

        metricsService.addSearchResponseMetrics(queryContext.getAppid(), queryResponse.getTook(), queryResponse.getHits().getTotal(), queryResponse.getShards().getTotalShard(), queryResponse.getShards().getFailedShard());

        if (queryResponse.getShards().getFailedShard() > 0) {
            StringBuilder stringBuilder = new StringBuilder("search response has some failed,appid="+queryContext.getAppid()+",requestId="+queryContext.getRequestId()+",number="+queryResponse.getShards().getFailedShard()+" reasons:\n");
            int count = 0;
            for (Failure failure : queryResponse.getShards().getFailures()) {
                stringBuilder.append(Convert.getPrefix(failure.getReason().getReason()));
                stringBuilder.append("\n");
                count++;
                if (count > 2) {
                    stringBuilder.append("...\n");
                    break;
                }
            }
            logger.warn(stringBuilder.toString());
        }

        buildSearchResponseLog(queryContext, queryResponse);

        if (queryResponse.getTook() > queryConfig.getSearchSlowlogThresholdMills()) {
            buildSearchSlowlog(queryContext, queryResponse);
        }

        if (queryContext.getAppDetail().isAnalyzeResponseEnable()) {
            buildSearchIndex(queryContext, queryResponse);
        }

        if (queryContext.isDetailLog()) {
            JoinLogContext joinLogContext = queryContext.getJoinLogContext();
            joinLogContext.setAriusType("type");
            joinLogContext.setClusterName(queryContext.getClient().getClusterName());
            joinLogContext.setClientVersion(queryContext.getClient().getEsVersion());
            joinLogContext.setLogicId(queryContext.getIndexTemplate() != null ? queryContext.getIndexTemplate().getId() : -1);
            joinLogContext.setTotalCost(System.currentTimeMillis() - queryContext.getRequestTime());
            joinLogContext.setInternalCost( joinLogContext.getTotalCost() - joinLogContext.getEsCost());
            joinLogContext.setSinkTime(System.currentTimeMillis());
            joinLogContext.setSearchCost(System.currentTimeMillis() - queryContext.getPreQueryEsTime());
            if (queryContext.getIndexTemplate() != null) {
                joinLogContext.setDestTemplateName(queryContext.getIndexTemplate().getName());
            }
        }
    }

    protected RestActionListenerImpl<ESSearchResponse> newSearchListener(QueryContext queryContext) {
        queryContext.setPreQueryEsTime(System.currentTimeMillis());
        return new RestActionListenerImpl<ESSearchResponse>(queryContext) {
            @Override
            public void onResponse(ESSearchResponse queryResponse) {
                statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_search_query||requestId={}||clusterName={}||logicId={}||cost={}", queryContext.getRequestId(), queryContext.getClusterName(),
                        queryContext.getIndexTemplate() != null ? queryContext.getIndexTemplate().getId() : -1, (System.currentTimeMillis()-queryContext.getRequestTime()));

                logSearchResponse(queryContext, queryResponse);

                queryResponse = dealKibanaResponse(queryContext, queryResponse);

                if (!Strings.isEmpty(queryResponse.getScrollId())
                        && queryContext.getClient() != null
                        && queryContext.getAppDetail().getSearchType() == AppDetail.RequestType.INDEX) {
                    String encode = Base64.getEncoder().encodeToString(queryContext.getClient().getClusterName().getBytes());
                    queryResponse.setScrollId(encode + SCROLL_SPLIT + queryResponse.getScrollId());
                }
                super.onResponse(queryResponse);
            }
        };
    }

    protected RestActionListenerImpl<DirectResponse> newDirectSearchListener(QueryContext queryContext) {
        return new RestActionListenerImpl<DirectResponse>(queryContext) {
            @Override
            public void onResponse(DirectResponse response) {
                try {
                    XContentParser parser = JsonXContent.jsonXContent.createParser(response.getResponseContent());
                    ESSearchResponse queryResponse =  ESSearchResponse.fromXContent(parser);
                    logSearchResponse(queryContext, queryResponse);
                    queryResponse = dealKibanaResponse(queryContext, queryResponse);
                    if (!Strings.isEmpty(queryResponse.getScrollId())
                            && queryContext.getClient() != null
                            && queryContext.getAppDetail().getSearchType() == AppDetail.RequestType.INDEX) {
                        String encode = Base64.getEncoder().encodeToString(queryContext.getClient().getClusterName().getBytes());
                        queryResponse.setScrollId(encode + SCROLL_SPLIT + queryResponse.getScrollId());
                    }
                    super.onResponse(response);
                } catch (Exception e) {
                    onFailure(e);
                }
            }
        };
    }

    protected RestActionListenerImpl<DirectResponse> newDirectWriteListener(QueryContext queryContext) {
        return new RestActionListenerImpl<DirectResponse>(queryContext) {
            @Override
            public void onResponse(DirectResponse response) {
                long currentTime = System.currentTimeMillis();
                setWriteLog(queryContext, null, response,
                        currentTime, queryConfig.isWriteLogContentOpen());

                metricsService.addIndexMetrics(null, name(), currentTime - queryContext.getRequestTime(), queryContext.getPostBody().length(), response.getResponseContent().length());

                super.onResponse(response);
            }
        };
    }

    protected void directRequest(ESClient client, QueryContext queryContext) {
        RestActionListenerImpl<DirectResponse> listener = new RestActionListenerImpl<>(queryContext);
        if (this instanceof RestSearchAction) {
            dslAuditService.auditDSL(queryContext, queryContext.getRequest().content(), queryContext.getIndices().toArray(new String[]{}));
            listener = newDirectSearchListener(queryContext);
        } else if (this instanceof RestBaseWriteAction) {
            listener = newDirectWriteListener(queryContext);
        }
        directRequest(client, queryContext, listener);
    }

    protected void directRequest(ESClient client, QueryContext queryContext, DirectRequest directRequest) {
        RestActionListenerImpl<DirectResponse> listener = new RestActionListenerImpl<>(queryContext);
        if (this instanceof RestSearchAction) {
            dslAuditService.auditDSL(queryContext, queryContext.getRequest().content(), queryContext.getIndices().toArray(new String[] {}));
            listener = newDirectSearchListener(queryContext);
        } else if (this instanceof RestBaseWriteAction) {
            listener = newDirectWriteListener(queryContext);
        }
        client.direct(directRequest, listener);
    }

    protected void directRequest(ESClient client, QueryContext queryContext,
                                 RestActionListenerImpl<DirectResponse> listener) {
        DirectRequest directRequest = buildDirectRequest(queryContext, queryContext.getUri());
        client.direct(directRequest, listener);
    }

    protected DirectRequest buildDirectRequest(QueryContext queryContext, String uri) {
        String queryString = queryContext.getQueryString() == null ? "" : queryContext.getQueryString();

        Map<String, String> paramsMap = new HashMap<>();
        RestUtils.decodeQueryString(queryString, 0, paramsMap);

        DirectRequest directRequest = new DirectRequest(queryContext.getMethod().toString(), uri);
        setSocketTimeout(paramsMap, directRequest);
        directRequest.setPostContent(queryContext.getPostBody());
        directRequest.setParams(paramsMap);

        directRequest.putHeader("requestId", queryContext.getRequestId());
        directRequest.putHeader("Authorization", queryContext.getRequest().getHeader("Authorization"));
        return directRequest;
    }

    protected void preSearchProcess(QueryContext queryContext, ESClient client, ESSearchRequest esSearchRequest) {
        String dslTemplateMd5;
        if (this instanceof SQLAction) {
            dslTemplateMd5 = dslAuditService.auditSQL(queryContext, queryContext.getPostBody(), esSearchRequest.indices());
        } else {
            dslTemplateMd5 = dslAuditService.auditDSL(queryContext, esSearchRequest.source(), esSearchRequest.indices());
        }

        esSearchRequest.putHeader(RestConsts.DSL_MD5_PARAMS, dslTemplateMd5);

        dslAggsAnalyzerService.analyzeAggs(queryContext, esSearchRequest.source(), esSearchRequest.indices());

        BytesReference source = dslRewriterService.rewriteRequest(queryContext, client.getEsVersion(), esSearchRequest.source());
        esSearchRequest.source(source);

        setSocketTimeout(esSearchRequest.getParams(), esSearchRequest);
    }

    protected void sendDirectResponse(QueryContext queryContext, RestResponse restResponse) {
        RestActionListenerImpl<ESSearchResponse> listener = new RestActionListenerImpl<>(queryContext);
        listener.onResponse(restResponse);
    }

    protected ESSearchResponse dealKibanaResponse(QueryContext queryContext, ESSearchResponse queryResponse) {
        if (queryContext.isNewKibana()) {
            if (queryContext.getRequest().rawPath().equals(queryConfig.getKibanaSearchUri())) {
                List<Hit> hitsList = new ArrayList<>();
                List<Hit> hits = queryResponse.getHits().getHits();
                for (Hit hit : hits) {
                    ESSearchResponse queryResponse1 = getEsSearchResponse(queryContext, queryResponse, hitsList, hit);
                    if (queryResponse1 != null) return queryResponse1;
                }
                queryResponse.getHits().setHits(hitsList);
                return queryResponse;
            } else {
                return queryResponse;
            }
        } else {
            return queryResponse;
        }
    }

    private ESSearchResponse getEsSearchResponse(QueryContext queryContext, ESSearchResponse queryResponse, List<Hit> hitsList, Hit hit) {
        try {
            // kibana索引名称字段为title，过滤出有权限访问的索引
            if (hit.getId().contains("index-pattern")) {
                Map<String, String> map = (Map<String, String>) hit.getSource().get("index-pattern");
                if (indexTemplateService.checkIndex(map.get("title"), queryContext.getAppDetail().getIndexExp())) {
                    hitsList.add(hit);
                }
            } else {
                return queryResponse;
            }
        } catch (Exception e) {
            return queryResponse;
        }
        return null;
    }

    protected boolean isNeededCheckIndices() {
        return !isNotCheckAction();
    }

    private boolean isNotCheckAction() {
        return this instanceof RestSearchScrollAction
                || this instanceof RestClearScrollAction
                || this instanceof RestMultiGetAction
                || this instanceof RestMultiSearchAction
                || this instanceof RestBulkAction
                || this instanceof RestSpatialMultiSearchAction;
    }

    protected boolean isNeededCheckTemplateSearchBlockAction() {
        return this instanceof RestBaseGetAction
                || this instanceof RestCountAction
                || this instanceof RestHeadAction
                || this instanceof RestSearchAction
                || this instanceof RestUpdateByQueryAction
                || this instanceof RestDeleteByQueryAction;
    }

    /**
     * 是否需要替换查询的索引名称
     *
     * @param queryContext
     * @param indexTemplate
     * @return
     */
    public boolean isNeedChangeIndexName(QueryContext queryContext, IndexTemplate indexTemplate) {

        if (Objects.nonNull(indexTemplate) &&
                Objects.nonNull(indexTemplate.getMasterInfo()) &&
                MapUtils.isNotEmpty(indexTemplate.getMasterInfo().getTypeIndexMapping())) {

            // 如果该索引启用type名称映射功能，或者该appid是在白名单中的，则需要替换查询的索引名称
            if (indexTemplate.getMasterInfo().getMappingIndexNameEnable().booleanValue() ||
                    dynamicConfigService.isWhiteAppid(queryContext.getAppid())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 替换索引名称的具体实现
     *
     * @param queryContext
     * @param indexTemplate
     * @param sourceIndexNames
     * @param types
     * @return
     */
    public Tuple<IndexTemplate/*dest template*/, String[]/*dest indexNames*/> handleChangeIndexName(QueryContext queryContext, IndexTemplate indexTemplate, String[] sourceIndexNames, String[] types) {
        String sourceTemplateName = indexTemplate.getName();
        List<String> indexList = Lists.newArrayList();
        String destTemplateName = null;
        Map<String/*typeName*/,String/*templateName*/> typeIndexMapping = indexTemplate.getMasterInfo().getTypeIndexMapping();

        // 用户不指定type方式查询时，gateway需要将该多type索引映射为多个单type索引，然后转发到es，数据聚合功能由es完成。例如 GET indexName/_search  改写为  GET type1@indexName,type2@indexName/_search
        if (Objects.isNull(types) || types.length == 0) {
            for (String name : typeIndexMapping.values()) {
                destTemplateName = name;
                for (String indexName : sourceIndexNames) {
                    indexList.add(indexName.replaceAll(sourceTemplateName, destTemplateName));
                }
            }

        } else {
            // 用户指定type方式查询时，gateway需要将该多type索引和指定的type名称映射为对应的单type索引，然后转发到es。例如GET indexName/type1/_search   改写为 GET type1@indexName/type1/_search。
            String typeName = types[0];
            destTemplateName = typeIndexMapping.get(typeName);
            if (StringUtils.isNoneBlank(destTemplateName)) {
                String name = destTemplateName;
                indexList = Arrays.asList(sourceIndexNames).stream().map(item -> item.replaceAll(sourceTemplateName, name)).collect(Collectors.toList());
            } else {
                // 找不到type对应的索引名称时，使用原索引
                destTemplateName = sourceTemplateName;
                indexList = Arrays.asList(sourceIndexNames);
            }
        }

        // 替换查询语句中的索引名称
        String[] destIndexName = indexList.toArray(new String[]{});
        // 再替换索引模板对象
        IndexTemplate destIndexTemplate = indexTemplateService.getIndexTemplate(destTemplateName);

        if (queryContext.isDetailLog()) {
            JoinLogContext joinLogContext = queryContext.getJoinLogContext();
            joinLogContext.setSourceIndexNames(StringUtils.join(sourceIndexNames, ","));
            joinLogContext.setTypeName(StringUtils.join(types, ","));
            joinLogContext.setDestIndexName(StringUtils.join(destIndexName, ","));
            joinLogContext.setSourceTemplateName(sourceTemplateName);
            joinLogContext.setDestTemplateName(destTemplateName);
        }

        return new Tuple<>(destIndexTemplate, destIndexName);
    }

    /************************************************************** private method **************************************************************/
    protected void buildSearchSlowlog(QueryContext queryContext, ESSearchResponse queryResponse) {
        JoinLogContext joinLogContext = queryContext.getJoinLogContext();
        joinLogContext.setEsCost(queryResponse.getTook());
        joinLogContext.setScrollId(queryResponse.getScrollId());
        joinLogContext.setTotalShards(queryResponse.getShards().getTotalShard());
        joinLogContext.setFailedShards(queryResponse.getShards().getFailedShard());
        joinLogContext.setIsTimedOut(queryResponse.getTimeOut());
        joinLogContext.setTotalHits(queryResponse.getHits().getTotal());
    }

    protected void buildSearchResponseLog(QueryContext queryContext, ESSearchResponse queryResponse) {
        JoinLogContext joinLogContext = queryContext.getJoinLogContext();
        joinLogContext.setEsCost(queryResponse.getTook());
        joinLogContext.setScrollId(queryResponse.getScrollId());
        joinLogContext.setTotalShards(queryResponse.getShards().getTotalShard());
        joinLogContext.setFailedShards(queryResponse.getShards().getFailedShard());
        joinLogContext.setIsTimedOut(queryResponse.getTimeOut());
        joinLogContext.setTotalHits(queryResponse.getHits().getTotal());
    }

    private void setSocketTimeout(Map<String, String> params, ESActionRequest request) {
        if (params.containsKey(RestConsts.SOCKET_TIMEOUT_PARAMS)) {
            String strSocketTimeout = params.remove(RestConsts.SOCKET_TIMEOUT_PARAMS);
            try {
                int socketTimeout = Integer.parseInt(strSocketTimeout);
                if (socketTimeout > 0 && socketTimeout <= QueryConsts.MAX_SOCKET_TIMEOUT) {
                    request.setSocketTimeout(socketTimeout);
                }
            } catch (Exception e) {
                // pass
            }
        }
    }

    protected void handleOriginClusterRequest(QueryContext queryContext){
        logger.info("handleOriginClusterRequest||uri={}||indices={}", queryContext.getUri(),queryContext.getIndices());

        ESClient client = esClusterService.getClient(queryContext, actionName);
        directRequest(client, queryContext);
    }
}
