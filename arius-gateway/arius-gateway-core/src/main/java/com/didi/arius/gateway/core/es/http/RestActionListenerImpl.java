package com.didi.arius.gateway.core.es.http;

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

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.event.QueryPostResponseEvent;
import com.didi.arius.gateway.common.exception.ResponseTooLargeException;
import com.didi.arius.gateway.common.metadata.JoinLogContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.CommonUtil;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.component.SpringTool;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.log.LogGather;

public class RestActionListenerImpl<T extends ESActionResponse> implements ActionListener<T> {
	protected static final ILog logger = LogFactory.getLog(RestActionListenerImpl.class);
	protected static final Logger statLogger = LoggerFactory.getLogger(QueryConsts.STAT_LOGGER);
	protected static final ILog traceLogger = LogFactory.getLog(QueryConsts.TRACE_LOGGER);
	protected static final String ERROR = "error";
	
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
		if(response.getHost() != null) {
			queryContext.getJoinLogContext().setClientNode(response.getHost().getHostName() + ":" + response.getHost().getPort());
		}
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
		} catch (Exception ioe) {
			BytesRestResponse response = new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, XContentType.JSON.restContentType(), "{\"error\":\"unknown\"}");
			queryContext.getChannel().sendResponse(response);
		}finally {
			if (queryContext.isDetailLog()) {
				JoinLogContext joinLogContext = queryContext.getJoinLogContext();
				joinLogContext.setAriusType(ERROR);
				if (queryContext.getClient() != null) {
					joinLogContext.setClusterName(queryContext.getClient().getClusterName());
					joinLogContext.setClientVersion(queryContext.getClient().getEsVersion());
				}
				joinLogContext.setLogicId(queryContext.getIndexTemplate() != null ? queryContext.getIndexTemplate().getId() : -1);
				joinLogContext.setTotalCost(System.currentTimeMillis() - queryContext.getRequestTime());
				joinLogContext.setInternalCost( joinLogContext.getTotalCost() - joinLogContext.getEsCost());
				joinLogContext.setSinkTime(System.currentTimeMillis());
				String log = joinLogContext.toString();

				statLogger.error(log);
			}
		}
	}

	protected void postResponse(QueryContext queryContext) {

        SpringTool.publish(new QueryPostResponseEvent(this, queryContext));

		queryContext.setResponseTime(System.currentTimeMillis());

		int responseBodyLen = queryContext.getResponse() != null ? queryContext.getResponse().content().length() : 0;
		int status = queryContext.getResponse() != null && queryContext.getResponse().status() != null ? queryContext.getResponse().status().getStatus() : -1;

		if (queryContext.isDetailLog()) {
			JoinLogContext joinLogContext = queryContext.getJoinLogContext();
			joinLogContext.setStatus(status);
			joinLogContext.setResponseLen(responseBodyLen);
			String log = joinLogContext.toString();
			statLogger.info(log);

			traceLogger.info("_com_request_out||traceid={}||spanid={}||type=http||appid={}||requestId={}||uri={}||responseLen={}||status={}||proc_time={}",
					queryContext.getTraceid(), queryContext.getSpanid(), queryContext.getAppid(), queryContext.getRequestId(), queryContext.getUri(), responseBodyLen, status, queryContext.getCostTime());
		} else {
			LogGather.recordInfoLog(QueryConsts.DLFLAG_PREFIX + "query_response_" + queryContext.getAppid() + "_" + queryContext.getRestName(), String.format(
					"requestId=%s||uri=%s||cost=%d||status=%d||responseLen=%d", queryContext.getRequestId(), queryContext.getUri(), queryContext.getCostTime(), status, responseBodyLen
			));
		}

	}

	protected void postResponse(QueryContext queryContext, Throwable e) {
        SpringTool.publish(new QueryPostResponseEvent(this, queryContext));

		if (queryContext.isDetailLog()) {
			JoinLogContext joinLogContext = queryContext.getJoinLogContext();
			joinLogContext.setExceptionName(e.getClass().getName());
			joinLogContext.setStack(Convert.logExceptionStack(e));

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
					builder.field(ERROR, "unknown");
				} else {
					builder.field(ERROR, t.getMessage());
				}
				builder.field("status", RestStatus.INTERNAL_SERVER_ERROR);
				builder.endObject();

				return builder.bytes().toUtf8();
			}
		} catch (Exception ioe) {
			return "{\"error\":{}}";
		}
	}
}
