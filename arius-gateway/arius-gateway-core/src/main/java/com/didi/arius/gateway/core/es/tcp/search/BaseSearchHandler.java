package com.didi.arius.gateway.core.es.tcp.search;

import com.didi.arius.gateway.core.es.tcp.ActionHandler;
import com.didi.arius.gateway.core.es.tcp.ActionListenerImpl;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.utils.Convert;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.io.IOException;

/**
* @author weizijun
* @date：2016年9月21日
* 
*/
public abstract class BaseSearchHandler extends ActionHandler {
	public static final String APP_ID = "appid=";
	public static final String REQUEST_ID = "requestId=";
	public static final String INDICES = "||indices=";
	public static final String TYPES = "||types=";
	public static final String SOURCE = "||source=";
	public static final String EXTRA_SOURCE = "||extra_source=";
	public static final String TOOK_MILLIS = "tookInMillis=";
	public static final String SCROLL_ID = "scrollId=";
	public static final String TOTAL_SHARDS = "totalShards=";
	public static final String FAILED_SHARDS = "failedShards=";
	public static final String IS_TIMEOUT = "isTimedOut=";
	public static final String TOTAL_HIT = "totalHits=";

	protected String buildSearchRequestLog(ActionContext actionContext, SearchRequest searchRequest) {
		StringBuilder sb = new StringBuilder();
		sb.append(QueryConsts.DLFLAG_PREFIX + "query_tcp_search_request||");

		sb.append(APP_ID).append(actionContext.getAppid())
			.append("||");

		sb.append(REQUEST_ID).append(actionContext.getRequestId())
			.append("||");

		if (searchRequest.indices() != null
				&& searchRequest.indices().length > 0) {
			String[] indices = searchRequest.indices();
			sb.append(INDICES);
			for (String index : indices) {
				sb.append(index).append(",");
			}
		} else {
			sb.append(INDICES);
		}
		dealType(searchRequest, sb);

		sb.append("||routing=");
		sb.append(searchRequest.routing());

		if (searchRequest.source() != null
				&& searchRequest.source().length() > 0) {
			try {
				sb.append(SOURCE)
						.append(XContentHelper.convertToJson(
								searchRequest.source(), true).replace("\n", ""));
			} catch (IOException e) {
				sb.append("||source=_failed_to_convert_");
			}
		} else {
			sb.append(SOURCE);
		}
		if (searchRequest.extraSource() != null
				&& searchRequest.extraSource().length() > 0) {
			try {
				sb.append(EXTRA_SOURCE).append(
						XContentHelper.convertToJson(
								searchRequest.extraSource(), true).replaceAll("\n", ""));
			} catch (IOException e) {
				sb.append("||extra_source=_failed_to_convert_");
			}
		} else {
			sb.append(EXTRA_SOURCE);
		}

		return sb.toString();
	}

	private void dealType(SearchRequest searchRequest, StringBuilder sb) {
		if (searchRequest.types() != null && searchRequest.types().length > 0) {
			String[] types = searchRequest.types();
			sb.append(TYPES);
			for (String type : types) {
				sb.append(type).append(",");
			}
		} else {
			sb.append(TYPES);
		}
	}

	protected String buildSearchResponseLog(ActionContext actionContext, SearchResponse searchResponse) {
		metricsService.addSearchResponseMetrics(actionContext.getAppid(), searchResponse.getTookInMillis(), searchResponse.getHits().getTotalHits(), searchResponse.getTotalShards(), searchResponse.getFailedShards());
		
		StringBuilder sb = new StringBuilder();
		sb.append(QueryConsts.DLFLAG_PREFIX + "query_tcp_search_response||");

		sb.append(APP_ID).append(actionContext.getAppid())
				.append("||");
		
		sb.append(REQUEST_ID).append(actionContext.getRequestId())
				.append("||");

		sb.append(TOOK_MILLIS).append(searchResponse.getTookInMillis())
				.append("||");

		sb.append(SCROLL_ID).append(searchResponse.getScrollId())
				.append("||");

		sb.append(TOTAL_SHARDS).append(searchResponse.getTotalShards())
				.append("||");

		sb.append(FAILED_SHARDS)
		.append(searchResponse.getTotalShards()-searchResponse.getSuccessfulShards()).append("||");

		sb.append(IS_TIMEOUT)
				.append(searchResponse.isTimedOut()).append("||");		
		
		sb.append(TOTAL_HIT).append(searchResponse.getHits().getTotalHits());

		return sb.toString();
	}
	
	protected String buildScrollSearchSlowlog(ActionContext actionContext, SearchResponse searchResponse) {
		StringBuilder sb = new StringBuilder();
		sb.append(QueryConsts.DLFLAG_PREFIX + "query_tcp_scroll_search_slowlog||");

		sb.append(APP_ID).append(actionContext.getAppid())
				.append("||");
		
		sb.append(REQUEST_ID).append(actionContext.getRequestId())
				.append("||");

		sb.append(TOOK_MILLIS).append(searchResponse.getTookInMillis())
				.append("||");

		sb.append(SCROLL_ID).append(searchResponse.getScrollId())
				.append("||");

		sb.append(TOTAL_SHARDS).append(searchResponse.getTotalShards())
				.append("||");

		sb.append(FAILED_SHARDS)
		.append(searchResponse.getTotalShards()-searchResponse.getSuccessfulShards()).append("||");

		sb.append(IS_TIMEOUT)
				.append(searchResponse.isTimedOut()).append("||");		
		
		sb.append(TOTAL_HIT).append(searchResponse.getHits().getTotalHits());

		return sb.toString();
	}	
	
    protected String buildSearchSlowlog(ActionContext actionContext, SearchRequest searchRequest, SearchResponse searchResponse) {
		StringBuilder sb = new StringBuilder();
		sb.append(QueryConsts.DLFLAG_PREFIX + "query_tcp_search_slowlog||");    
		
		sb.append(APP_ID).append(actionContext.getAppid())
			.append("||");		
		
		sb.append(REQUEST_ID).append(actionContext.getRequestId());

		if (searchRequest.indices() != null
				&& searchRequest.indices().length > 0) {
			String[] indices = searchRequest.indices();
			sb.append(INDICES);
			for (String index : indices) {
				sb.append(index).append(",");
			}
		} else {
			sb.append(INDICES);
		}
		dealType(searchRequest, sb);

		if (searchRequest.source() != null
				&& searchRequest.source().length() > 0) {
			try {
				sb.append(SOURCE)
						.append(XContentHelper.convertToJson(
								searchRequest.source(), true));
			} catch (IOException e) {
				sb.append("||source=_failed_to_convert_");
			}
		} else {
			sb.append(SOURCE);
		}
		if (searchRequest.extraSource() != null
				&& searchRequest.extraSource().length() > 0) {
			try {
				sb.append(EXTRA_SOURCE).append(
						XContentHelper.convertToJson(
								searchRequest.extraSource(), true));
			} catch (IOException e) {
				sb.append("||extra_source=_failed_to_convert_");
			}
		} else {
			sb.append(EXTRA_SOURCE);
		}
		
		sb.append("||");
		
		sb.append(TOOK_MILLIS).append(searchResponse.getTookInMillis())
				.append("||");
		
		sb.append(SCROLL_ID).append(searchResponse.getScrollId())
				.append("||");
		
		sb.append(TOTAL_SHARDS).append(searchResponse.getTotalShards())
				.append("||");
		
		sb.append(FAILED_SHARDS)
		.append(searchResponse.getTotalShards()-searchResponse.getSuccessfulShards()).append("||");
		
		sb.append(IS_TIMEOUT)
				.append(searchResponse.isTimedOut()).append("||");		
		
		sb.append(TOTAL_HIT).append(searchResponse.getHits().getTotalHits());
		
		return sb.toString();
    }	
	
	protected ActionListener<SearchResponse> newListener(ActionContext actionContext) {
		return new ActionListenerImpl<SearchResponse>(actionContext) {
			@Override
			public void onResponse(SearchResponse searchResponse) {
				statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_tcp_search_query||appid={}||requestId={}||cost={}", actionContext.getAppid(), actionContext.getRequestId(), (System.currentTimeMillis()-actionContext.getRequestTime()));
				
				if (searchResponse.getFailedShards() > 0) {
					StringBuilder stringBuilder = new StringBuilder("search response has some failed,appid="+actionContext.getAppid()+",requestId="+actionContext.getRequestId()+",number="+searchResponse.getFailedShards()+" reasons:\n");
					int count = 0;
					for (ShardSearchFailure shardSearchFailure : searchResponse.getShardFailures()) {
						stringBuilder.append(Convert.getPrefix(shardSearchFailure.reason()));
						stringBuilder.append("\n");
						count++;
						if (count > 2) {
							stringBuilder.append("...\n");
							break;
						}
					}
					logger.warn(stringBuilder.toString());
				}

				statLogger.info(buildSearchResponseLog(actionContext, searchResponse));
				
				if (searchResponse.getTookInMillis() > queryConfig.getSearchSlowlogThresholdMills()) {
					if (actionContext.getRequest() instanceof SearchRequest) {
						SearchRequest searchRequest = (SearchRequest) actionContext.getRequest();
						statLogger.warn(buildSearchSlowlog(actionContext, searchRequest, searchResponse));	
					} else if (actionContext.getRequest() instanceof SearchScrollRequest) {
						statLogger.warn(buildScrollSearchSlowlog(actionContext, searchResponse));						
					}

					metricsService.addSlowlogCost(actionContext.getAppid(), actionContext.getCostTime());
				}

				super.onResponse(searchResponse);
			}
		};
	}
}
