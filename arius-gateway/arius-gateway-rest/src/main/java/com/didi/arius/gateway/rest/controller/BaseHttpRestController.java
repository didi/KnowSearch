package com.didi.arius.gateway.rest.controller;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.consts.RestConsts;
import com.didi.arius.gateway.common.exception.QueryDslLengthException;
import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.JoinLogContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.AppUtil;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.core.service.ESRestClientService;
import com.didi.arius.gateway.core.service.RateLimitService;
import com.didi.arius.gateway.core.service.RequestStatsService;
import com.didi.arius.gateway.core.service.arius.AppService;
import com.didi.arius.gateway.core.service.arius.DynamicConfigService;
import com.didi.arius.gateway.core.service.arius.ESClusterService;
import com.didi.arius.gateway.core.service.arius.IndexTemplateService;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectResponse;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchResponse;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.rest.http.IRestHandler;
import com.didi.arius.gateway.rest.http.RestController;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.log.LogGather;
import com.didiglobal.knowframework.observability.Observability;
import com.didiglobal.knowframework.observability.common.constant.Constant;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.*;
import org.elasticsearch.rest.support.RestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class BaseHttpRestController implements IRestHandler {

    protected static final ILog logger = LogFactory.getLog(BaseHttpRestController.class);
    protected static final ILog statLogger = LogFactory.getLog(QueryConsts.STAT_LOGGER);
    protected static final ILog traceLogger = LogFactory.getLog(QueryConsts.TRACE_LOGGER);
    protected static final ILog auditLogger = LogFactory.getLog(QueryConsts.AUDIT_LOGGER);
    protected static final String AUTHORIZATION = "Authorization";

    private static final TextMapPropagator TEXT_MAP_PROPAGATOR = Observability.getTextMapPropagator();
    private static final Tracer tracer = Observability.getTracer(BaseHttpRestController.class.getName());

    private static Set<Integer> validHttpStatusCodeSet = new HashSet<>();

    static {
        validHttpStatusCodeSet.add(HttpStatus.SC_OK);
    }

    @Autowired
    protected DynamicConfigService dynamicConfigService;

    @Autowired
    protected IndexTemplateService indexTemplateService;

    @Autowired
    protected RequestStatsService requestStatsService;

    @Autowired
    protected ESClusterService esClusterService;

    @Autowired
    protected AppService appService;

    @Autowired
    protected ESRestClientService esRestClientService;

    @Autowired
    protected QueryConfig queryConfig;

    @Autowired
    protected RestController controller;

    @Autowired
    protected RateLimitService rateLimitService;

    protected String actionName = this.getClass().getSimpleName();

    @PostConstruct
    public void init() {
        register();
    }

    @Override
    public void dispatchRequest(RestRequest request, RestChannel channel) {
        Span span = buildSpan(request);
        QueryContext queryContext = parseContext(request, channel);
        try (Scope scope = span.makeCurrent()) {
            // Process the request
            checkToken(queryContext);
            preRequest(queryContext);
            handleRequest(queryContext);
            postRequest(queryContext);
            //handle response status code
            RestResponse response = queryContext.getResponse();
            if(null == response || null == response.status()) {
                //注：业务方表示 response 可能为 null 或 response.status 为空，此时，如未抛异常，表示成功
                span.setStatus(StatusCode.OK);
            } else {
                //set span status
                int httpStatus = response.status().getStatus();
                setSpanStatus(span, httpStatus);
            }
        } catch (Exception ex) {
            span.setStatus(StatusCode.ERROR, ex.getMessage());
            preException(queryContext, ex);
            try {
                channel.sendResponse(new BytesRestResponse(channel, ex));
            } catch (IOException ioe) {
                BytesRestResponse response = new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR);
                channel.sendResponse(response);
            }
        } finally {
            // Close the span
            span.end();
        }
    }

    /**
     * 根据 http status 设置 span 状态
     * @param span Span 对象
     * @param httpStatus http status code
     */
    private void setSpanStatus(Span span, int httpStatus) {
        if(!validHttpStatusCodeSet.contains(httpStatus)) {
            span.setStatus(
                    StatusCode.ERROR,
                    String.format(
                            "http状态码%d不在合法http状态码集%s内",
                            httpStatus,
                            JSON.toJSONString(validHttpStatusCodeSet)
                    )
            );
        } else {
            span.setStatus(StatusCode.OK);
        }
    }

    /**
     * 根据 request 对象，构建 span 对象，并注入 http 请求头相关信息
     * @param request RestRequest 对象
     * @return Span 对象
     */
    private Span buildSpan(RestRequest request) {
        Context context = TEXT_MAP_PROPAGATOR.extract(Context.current(), request, getter);
        Span span = tracer.spanBuilder(
                String.format("%s.%s", this.getClass().getName(), "dispatchRequest")
        ).setParent(context).setSpanKind(SpanKind.SERVER).startSpan();
        span.setAttribute(Constant.ATTRIBUTE_KEY_COMPONENT, Constant.ATTRIBUTE_VALUE_COMPONENT_HTTP);
        span.setAttribute(Constant.ATTRIBUTE_KEY_HTTP_METHOD, request.method().name());
        span.setAttribute(Constant.ATTRIBUTE_KEY_HTTP_SCHEMA, Constant.ATTRIBUTE_VALUE_COMPONENT_HTTP);
        span.setAttribute(Constant.ATTRIBUTE_KEY_HTTP_HOST, request.getLocalAddress().toString());
        span.setAttribute(Constant.ATTRIBUTE_KEY_HTTP_TARGET, request.uri());
        return span;
    }

    /*
     * extract the context from http headers
     */
    private static final TextMapGetter<RestRequest> getter =
            new TextMapGetter<RestRequest>() {
                @Override
                public Iterable<String> keys(RestRequest carrier) {
                    List<String> iterable = new ArrayList<>();
                    Set<String> headers = carrier.getHeaders();
                    if(CollectionUtils.isNotEmpty(headers)) {
                        iterable.addAll(headers);
                    }
                    return iterable;
                }
                @Override
                public String get(RestRequest carrier, String key) {
                    String headerValue = carrier.getHeader(key);
                    return headerValue == null ? StringUtils.EMPTY : headerValue;
                }
            };

    /************************************************************** abstract method **************************************************************/
    /**
     *
     */
    protected abstract void register();

    /**
     * @return
     */
    protected abstract String name();

    /**
     * @param queryContext
     * @throws Exception
     */
    protected abstract void handleRequest(QueryContext queryContext) throws Exception;

    /************************************************************** protected method **************************************************************/
    /**
     * @param queryContext
     * @param restResponse
     */
    protected void sendDirectResponse(QueryContext queryContext, RestResponse restResponse) {
        RestActionListenerImpl<ESSearchResponse> listener = new RestActionListenerImpl<>(queryContext);
        listener.onResponse(restResponse);
    }

    protected void checkIndices(QueryContext queryContext) {
        List<String> indices = queryContext.getIndices();
        appService.checkIndices(queryContext, indices);
    }

    protected void checkToken(QueryContext queryContext) {
        appService.checkToken(queryContext);
        String encode = Base64.getEncoder().encodeToString(String.format("%s", "user_" + queryContext.getAppid() + ":" + queryContext.getAppDetail().getVerifyCode()).getBytes(StandardCharsets.UTF_8));
        queryContext.getRequest().putHeader(AUTHORIZATION, "Basic " + encode);
    }

    protected void preRequest(QueryContext queryContext) {
        if (queryContext.isFromKibana() || (AppUtil.isAdminAppid(queryContext.getAppDetail())
                                            && StringUtils.isNotBlank(queryContext.getClusterId()))) {
            // 如果是来自 kibana 的请求，则设置为原生查询
            queryContext.setSearchType(AppDetail.RequestType.ORIGIN_CLUSTER.getType());
        }
        if (queryContext.getPostBody() != null && queryContext.getPostBody().length() > queryConfig.getDslMaxLength()) {
            throw new QueryDslLengthException(String.format("query length(%d) > %d exception", queryContext.getPostBody().length(), queryConfig.getDslMaxLength()));
        }

        queryContext.setRequestTime(System.currentTimeMillis());

        if (queryContext.isDetailLog()) {
            JoinLogContext joinLogContext = queryContext.getJoinLogContext();
            joinLogContext.setAppid(queryContext.getAppid());
            joinLogContext.setTraceid(queryContext.getTraceid());
            joinLogContext.setRequestId(queryContext.getRequestId());
            joinLogContext.setRequestType(queryContext.getAppDetail().getSearchType());
            joinLogContext.setMethod(queryContext.getMethod());
            joinLogContext.setClusterId(queryContext.getClusterId());
            joinLogContext.setUser(queryContext.getUser());
            joinLogContext.setUri(queryContext.getUri());
            joinLogContext.setQueryString(queryContext.getQueryString());
            joinLogContext.setRemoteAddr(queryContext.getRemoteAddr());
            joinLogContext.setDslLen(queryContext.getPostBody().length());
            joinLogContext.setDsl(queryContext.getPostBody().replaceAll("\\n", " "));
            joinLogContext.setTimeStamp(System.currentTimeMillis());
            joinLogContext.setProjectId(queryContext.getProjectId());
            traceLogger.info("_com_request_in||traceid={}||spanid={}||type=http||appid={}||projectId={}||requestId={}||uri={}||remoteAddr={}||requestLen={}||name={}",
                    queryContext.getTraceid(), queryContext.getSpanid(), queryContext.getAppid(), queryContext.getProjectId(),
                    queryContext.getRequestId(), queryContext.getUri(), queryContext.getRemoteAddr(), queryContext.getPostBody().length(), name());
        } else {
            LogGather.recordInfoLog(QueryConsts.DLFLAG_PREFIX + "query_request_" + queryContext.getAppid() + "_" + name(), String.format("requestId=%s||method=%s||uri=%s||queryString=%s||remoteAddr=%s||postBodyLen=%d",
                    queryContext.getRequestId(), queryContext.getMethod(), queryContext.getUri(), queryContext.getQueryString(), queryContext.getRemoteAddr(), queryContext.getPostBody().length()));
        }

        if (queryContext.getXUserName() != null) {
            auditLogger.info("auditlog||system=arius||hostIp=127.0.0.1||userName={}||url={}||getParams={}||postParams={}||userIp={}||timestamp=||respose=",
                    queryContext.getXUserName(), queryContext.getUri(), queryContext.getQueryString(), queryContext.getPostBody(), queryContext.getRemoteAddr());
        }

        rateLimitService.addByteIn(queryContext.getPostBody().length());
    }

    protected void preException(QueryContext queryContext, Throwable e) {
        if (queryContext.isDetailLog()) {
            JoinLogContext joinLogContext = queryContext.getJoinLogContext();
            joinLogContext.setAriusType("error");
            joinLogContext.setExceptionName(e.getClass().getName());
            joinLogContext.setStack(Convert.logExceptionStack(e));
            joinLogContext.setTotalCost(System.currentTimeMillis() - queryContext.getRequestTime());
            joinLogContext.setInternalCost(joinLogContext.getTotalCost() - joinLogContext.getEsCost());
            joinLogContext.setSinkTime(System.currentTimeMillis());

            String log = joinLogContext.toString();
            statLogger.error(log);

            traceLogger.info("_com_request_out||traceid={}||spanid={}||type=http||appid={}||requestId={}||errname={}",
                    queryContext.getTraceid(), queryContext.getSpanid(), queryContext.getAppid(), queryContext.getRequestId(), e.getClass().getName());
        }

        LogGather.recordErrorLog(e.getClass().getName() + "_" + queryContext.getAppid(), String.format("http_exception||requestId=%s||appid=%d||uri=%s||postBody=%s",
                queryContext.getRequestId(), queryContext.getAppid(), queryContext.getUri(), queryContext.getPostBody()), e);

        requestStatsService.removeQueryContext(queryContext.getRequestId());

        rateLimitService.removeByteIn(queryContext.getPostBody().length());
    }

    protected void postRequest(QueryContext queryContext) {
        int appid = queryContext.getAppDetail() != null ? queryContext.getAppDetail().getId() : QueryConsts.TOTAL_APPID_ID;
        requestStatsService.statsAdd(name(), appid, queryContext.getSearchId(), queryContext.getCostTime(), RestStatus.OK);

        String searchId = queryContext.getSearchId() != null ? queryContext.getSearchId() : QueryConsts.TOTAL_SEARCH_ID;
        try {
            rateLimitService.addUp(appid, searchId, 0, 0);
        } catch (Exception e) {
            logger.warn("rateLimitService.addUp exception", e);
        }

    }

    protected void directRequest(ESClient client, QueryContext queryContext, RestActionListenerImpl<DirectResponse> listener) {
        String uri = queryContext.getUri();
        String queryString = queryContext.getQueryString() == null ? "" : queryContext.getQueryString();

        Map<String, String> params = new HashMap<>();
        RestUtils.decodeQueryString(queryString, 0, params);

        DirectRequest directRequest = new DirectRequest(queryContext.getMethod().toString(), uri);
        setSocketTimeout(params, directRequest);
        directRequest.setPostContent(queryContext.getPostBody());
        directRequest.setParams(params);

        directRequest.putHeader(AUTHORIZATION, queryContext.getRequest().getHeader(AUTHORIZATION));
        directRequest.putHeader("requestId", queryContext.getRequestId());

        client.direct(directRequest, listener);
    }

    protected void directRequest(ESClient client, QueryContext queryContext) {
        RestActionListenerImpl<DirectResponse> listener = new RestActionListenerImpl<>(queryContext);
        directRequest(client, queryContext, listener);
    }

    /************************************************************** private method **************************************************************/
    private QueryContext parseContext(RestRequest request, RestChannel channel) {
        QueryContext context = new QueryContext();
        context.setRequestTime(System.currentTimeMillis());
        context.setRestName(name());
        context.setJoinLogContext(new JoinLogContext());

        String searchId = request.header(QueryConsts.HEAD_SEARCH_ID);
        String clusterId = request.header(QueryConsts.HEAD_CLUSTER_ID);
        String user = request.header(QueryConsts.HEAD_USER);
        String authentication = request.header(QueryConsts.HEAD_AUTHORIZATION);
        String xUserName = request.header(QueryConsts.HEAD_USERNAME);
        String ssoUserName = request.header(QueryConsts.HEAD_SSO_USERNAME);
        String clientVersion = request.header(QueryConsts.HEAD_CLIENT_VERSION);

        String traceid = request.header(QueryConsts.TRACE_ID);
        String spanid = request.header(QueryConsts.SPAN_ID);

        if (traceid == null) {
            traceid = "";
        }

        if (spanid == null) {
            spanid = "";
        }

        String postBody = request.content().toUtf8();

        if (postBody == null) {
            postBody = "";
        }

        String remoteAddr = Convert.getClientIP(request);

        if (searchId == null) {
            searchId = QueryConsts.TOTAL_SEARCH_ID;
        }

        if (clusterId == null) {
            clusterId = request.param(QueryConsts.GET_CLUSTER_ID);
            request.params().remove(QueryConsts.GET_CLUSTER_ID);
        }

        context.setTraceid(traceid);
        context.setRequestId(UUID.randomUUID().toString());
        context.setMethod(request.method());
        context.setSearchId(searchId);
        context.setClusterId(clusterId);
        context.setUser(user);
        context.setClientVersion(clientVersion);

        String kibanaVersion = request.header(QueryConsts.HEAD_KIBANA_VERSION);

        // 保存 kibana 数据的索引一般是：.kibana_task_manager、.reporting、.kibana_arius 等
        context.setFromKibana(request.rawPath().startsWith("/.") || request.rawPath().startsWith("."));
        context.setNewKibana(kibanaVersion != null && kibanaVersion.startsWith(QueryConsts.NEW_KIBANA_VERSION_START));

        String uri = request.uri();
        int pathEndPos = uri.indexOf('?');
        if (pathEndPos > 0 && pathEndPos < uri.length()) {
            context.setQueryString(uri.substring(pathEndPos + 1));
        } else {
            context.setQueryString("");
        }

        context.setPostBody(postBody);
        context.setRemoteAddr(remoteAddr);
        context.setAuthentication(authentication);

        if (user != null) {
            context.setXUserName(user);
        } else if (xUserName != null) {
            context.setXUserName(xUserName);
        } else if (ssoUserName != null) {
            context.setXUserName(ssoUserName);
        }

        context.setTraceid(traceid);
        context.setSpanid(spanid);

        context.setRequest(request);
        context.setChannel(channel);

        context.setSemaphore(queryConfig.getHttpSemaphore());
        context.setRequestSlowlogThresholdMills(queryConfig.getRequestSlowlogThresholdMills());
        context.setMaxHttpResponseLength(queryConfig.getMaxHttpResponseLength());

        requestStatsService.putQueryContext(context.getRequestId(), context);

        context.setTypedKeys(request.paramAsBoolean("typed_keys", false));

        if (dynamicConfigService.getDetailLogFlag()
                || context.isFromKibana()) {
            context.setDetailLog(true);
//        } else if (this instanceof RestBaseWriteAction) {
//            context.setDetailLog(false);
        } else {
            context.setDetailLog(true);
        }

        return context;
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
}