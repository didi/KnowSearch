package com.didi.arius.gateway.core.es.http;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.event.QueryPostResponseEvent;
import com.didi.arius.gateway.common.exception.ResponseTooLargeException;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.CommonUtil;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.component.SpringTool;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didichuxing.tunnel.util.log.LogGather;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestActionListenerImpl<T extends ESActionResponse> implements ActionListener<T> {
	protected static final Logger logger = LoggerFactory.getLogger(RestActionListenerImpl.class);
	private static final Logger statLogger = LoggerFactory.getLogger(QueryConsts.STAT_LOGGER);
	protected static final Logger traceLogger = LoggerFactory.getLogger(QueryConsts.TRACE_LOGGER);
	
	protected QueryContext queryContext;
	
	public RestActionListenerImpl(QueryContext queryContext) {
		this.queryContext = queryContext;
	}

	public void onResponse(RestResponse restResponse) {

		if (queryContext.getRequest().method() == RestRequest.Method.HEAD) {
			restResponse = new BytesRestResponse(restResponse.status());
		}

		if (restResponse.content() != null && restResponse.content().length() > queryContext.getMaxHttpResponseLength()) {
			throw new ResponseTooLargeException(String.format("response length(%d) > %d exception", restResponse.content().length(), queryContext.getMaxHttpResponseLength()));
		}

		queryContext.setResponse(restResponse);
		postResponse(queryContext);

		queryContext.getChannel().sendResponse(restResponse);
	}
	
	@Override
	public void onResponse(T response) {

		RestResponse restResponse;
		if (queryContext.getRequest().method() == RestRequest.Method.HEAD) {
			restResponse = new BytesRestResponse(response.getRestStatus());
		} else {
			restResponse = response.buildRestResponse(queryContext.getChannel());

			if (restResponse.content() != null && restResponse.content().length() > queryContext.getMaxHttpResponseLength()) {
				throw new ResponseTooLargeException(String.format("response length(%d) > %d exception", restResponse.content().length(), queryContext.getMaxHttpResponseLength()));
			}
		}

		queryContext.setResponse(restResponse);
		postResponse(queryContext);

		queryContext.getChannel().sendResponse(restResponse);
	}

	@Override
	public void onFailure(Throwable e) {

		try {
			RestResponse restResponse;
			if (e instanceof ResponseException) {
				ResponseException responseException = (ResponseException) e;
				Response response = responseException.getResponse();
				int statusCode = response.getStatusLine().getStatusCode();

				if (queryContext.getRequest().method() == RestRequest.Method.HEAD) {
					restResponse = new BytesRestResponse( CommonUtil.fromCode(statusCode));
				} else {
					String responseBody = EntityUtils.toString(response.getEntity());
					restResponse = new BytesRestResponse(CommonUtil.fromCode(statusCode), XContentType.JSON.restContentType(), responseBody);
				}
			} else {
				if (queryContext.getRequest().method() == RestRequest.Method.HEAD) {
					restResponse = new BytesRestResponse(ExceptionsHelper.status(e));
				} else {
					restResponse = new BytesRestResponse(queryContext.getChannel(), e);
				}
			}

			postResponse(queryContext, e);

			queryContext.getChannel().sendResponse(restResponse);
		} catch (Throwable ioe) {
			BytesRestResponse response = new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, XContentType.JSON.restContentType(), "{\"error\":\"unknown\"}");
			queryContext.getChannel().sendResponse(response);
		}
	}

	protected void postResponse(QueryContext queryContext) {

        SpringTool.publish(new QueryPostResponseEvent(this, queryContext));

		queryContext.setResponseTime(System.currentTimeMillis());

		int responseBodyLen = queryContext.getResponse() != null ? queryContext.getResponse().content().length() : 0;
		int status = queryContext.getResponse() != null && queryContext.getResponse().status() != null ? queryContext.getResponse().status().getStatus() : -1;

		if (queryContext.isDetailLog()) {
			statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_response||requestId={}||appid={}||uri={}||cost={}||status={}||responseLen={}", queryContext.getRequestId(), queryContext.getAppid(), queryContext.getUri(), queryContext.getCostTime(), status, responseBodyLen);

			traceLogger.info("_com_request_out||traceid={}||spanid={}||type=http||appid={}||requestId={}||uri={}||responseLen={}||status={}||proc_time={}",
					queryContext.getTraceid(), queryContext.getSpanid(), queryContext.getAppid(), queryContext.getRequestId(), queryContext.getUri(), responseBodyLen, status, queryContext.getCostTime());

			if (queryContext.getCostTime() > queryContext.getRequestSlowlogThresholdMills()) {
				statLogger.warn(QueryConsts.DLFLAG_PREFIX + "query_slowlog||requestId={}||appid={}||method={}||searchId={}||uri={}||queryString={}||remoteAddr={}||cost={}||status={}||responseLen={}||postBodyLen={}||postBody={}",
						queryContext.getRequestId(), queryContext.getAppid(), queryContext.getMethod(), queryContext.getSearchId(),
						queryContext.getUri(), queryContext.getQueryString(), queryContext.getRemoteAddr(),
						queryContext.getCostTime(), status, responseBodyLen,
						queryContext.getPostBody().length(),
						Convert.getPrefix(queryContext.getPostBody()));
			}
		} else {
			LogGather.recordInfoLog(QueryConsts.DLFLAG_PREFIX + "query_response_" + queryContext.getAppid() + "_" + queryContext.getRestName(), String.format(
					"requestId=%s||uri=%s||cost=%d||status=%d||responseLen=%d", queryContext.getRequestId(), queryContext.getUri(), queryContext.getCostTime(), status, responseBodyLen
			));
		}

	}

	protected void postResponse(QueryContext queryContext, Throwable e) {
        SpringTool.publish(new QueryPostResponseEvent(this, queryContext));

		if (queryContext.isDetailLog()) {
			statLogger.error(QueryConsts.DLFLAG_PREFIX + "exception||name={}||appid={}||requestId={}||stack={}", e.getClass().getName(), queryContext.getAppid(), queryContext.getRequestId(), Convert.logExceptionStack(e));

			traceLogger.info("_com_request_out||traceid={}||spanid={}||type=http||appid={}||requestId={}||errname={}",
					queryContext.getTraceid(), queryContext.getSpanid(), queryContext.getAppid(), queryContext.getRequestId(), e.getClass().getName());
		} else {
			LogGather.recordErrorLog(e.getClass().getName() + "_" + queryContext.getAppid(), String.format("http_exception||requestId=%s||appid=%d||uri=%s||postBody=%s",
					queryContext.getRequestId(), queryContext.getAppid(), queryContext.getUri(), queryContext.getPostBody()), e);
		}

	}

	protected String exceptionToJsonString(Throwable t) {
		try {
			if (t instanceof ResponseException) {
				ResponseException responseException = (ResponseException) t;
				Response response = responseException.getResponse();
				return EntityUtils.toString(response.getEntity());
			} else {
				XContentBuilder builder = JsonXContent.contentBuilder().startObject();
				if (t == null) {
					builder.field("error", "unknown");
				} else {
					builder.field("error", t.getMessage());
				}
				builder.field("status", RestStatus.INTERNAL_SERVER_ERROR);
				builder.endObject();

				return builder.bytes().toUtf8();
			}
		} catch (Throwable ioe) {
			return "{\"error\":{}}";
		}
	}
}
