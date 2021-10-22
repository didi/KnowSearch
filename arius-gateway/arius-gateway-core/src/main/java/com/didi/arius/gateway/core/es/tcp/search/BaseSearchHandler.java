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
	protected String buildSearchRequestLog(ActionContext actionContext, SearchRequest searchRequest) {
		StringBuilder sb = new StringBuilder();
		sb.append(QueryConsts.DLFLAG_PREFIX + "query_tcp_search_request||");

		sb.append("appid=").append(actionContext.getAppid())
			.append("||");

		sb.append("requestId=").append(actionContext.getRequestId())
			.append("||");		
		
		if (searchRequest.indices() != null
				&& searchRequest.indices().length > 0) {
			String indices[] = searchRequest.indices();
			sb.append("||indices=");
			for (String index : indices) {
				sb.append(index).append(",");
			}
		} else {
			sb.append("||indices=");
		}
		if (searchRequest.types() != null && searchRequest.types().length > 0) {
			String types[] = searchRequest.types();
			sb.append("||types=");
			for (String type : types) {
				sb.append(type).append(",");
			}
		} else {
			sb.append("||types=");
		}
		
		sb.append("||routing=");
		sb.append(searchRequest.routing());

		if (searchRequest.source() != null
				&& searchRequest.source().length() > 0) {
			try {
				sb.append("||source=")
						.append(XContentHelper.convertToJson(
								searchRequest.source(), true).replaceAll("\\n", ""));
			} catch (IOException e) {
				sb.append("||source=_failed_to_convert_");
			}
		} else {
			sb.append("||source=");
		}
		if (searchRequest.extraSource() != null
				&& searchRequest.extraSource().length() > 0) {
			try {
				sb.append("||extra_source=").append(
						XContentHelper.convertToJson(
								searchRequest.extraSource(), true).replaceAll("\\n", ""));
			} catch (IOException e) {
				sb.append("||extra_source=_failed_to_convert_");
			}
		} else {
			sb.append("||extra_source=");
		}

		return sb.toString();
	}

	protected String buildSearchResponseLog(ActionContext actionContext, SearchResponse searchResponse) {
		metricsService.addSearchResponseMetrics(actionContext.getAppid(), searchResponse.getTookInMillis(), searchResponse.getHits().getTotalHits(), searchResponse.getTotalShards(), searchResponse.getFailedShards());
		
		StringBuilder sb = new StringBuilder();
		sb.append(QueryConsts.DLFLAG_PREFIX + "query_tcp_search_response||");

		sb.append("appid=").append(actionContext.getAppid())
				.append("||");
		
		sb.append("requestId=").append(actionContext.getRequestId())
				.append("||");

		sb.append("tookInMillis=").append(searchResponse.getTookInMillis())
				.append("||");

		sb.append("scrollId=").append(searchResponse.getScrollId())
				.append("||");

		sb.append("totalShards=").append(searchResponse.getTotalShards())
				.append("||");

		sb.append("failedShards=")
		.append(searchResponse.getTotalShards()-searchResponse.getSuccessfulShards()).append("||");

		sb.append("isTimedOut=")
				.append(searchResponse.isTimedOut()).append("||");		
		
		sb.append("totalHits=").append(searchResponse.getHits().getTotalHits());

		return sb.toString();
	}
	
	protected String buildScrollSearchSlowlog(ActionContext actionContext, SearchResponse searchResponse) {
		StringBuilder sb = new StringBuilder();
		sb.append(QueryConsts.DLFLAG_PREFIX + "query_tcp_scroll_search_slowlog||");

		sb.append("appid=").append(actionContext.getAppid())
				.append("||");
		
		sb.append("requestId=").append(actionContext.getRequestId())
				.append("||");

		sb.append("tookInMillis=").append(searchResponse.getTookInMillis())
				.append("||");

		sb.append("scrollId=").append(searchResponse.getScrollId())
				.append("||");

		sb.append("totalShards=").append(searchResponse.getTotalShards())
				.append("||");

		sb.append("failedShards=")
		.append(searchResponse.getTotalShards()-searchResponse.getSuccessfulShards()).append("||");

		sb.append("isTimedOut=")
				.append(searchResponse.isTimedOut()).append("||");		
		
		sb.append("totalHits=").append(searchResponse.getHits().getTotalHits());

		return sb.toString();
	}	
	
    protected String buildSearchSlowlog(ActionContext actionContext, SearchRequest searchRequest, SearchResponse searchResponse) {
		StringBuilder sb = new StringBuilder();
		sb.append(QueryConsts.DLFLAG_PREFIX + "query_tcp_search_slowlog||");    
		
		sb.append("appid=").append(actionContext.getAppid())
			.append("||");		
		
		sb.append("requestId=").append(actionContext.getRequestId());

		if (searchRequest.indices() != null
				&& searchRequest.indices().length > 0) {
			String indices[] = searchRequest.indices();
			sb.append("||indices=");
			for (String index : indices) {
				sb.append(index).append(",");
			}
		} else {
			sb.append("||indices=");
		}
		if (searchRequest.types() != null && searchRequest.types().length > 0) {
			String types[] = searchRequest.types();
			sb.append("||types=");
			for (String type : types) {
				sb.append(type).append(",");
			}
		} else {
			sb.append("||types=");
		}

		if (searchRequest.source() != null
				&& searchRequest.source().length() > 0) {
			try {
				sb.append("||source=")
						.append(XContentHelper.convertToJson(
								searchRequest.source(), true));
			} catch (IOException e) {
				sb.append("||source=_failed_to_convert_");
			}
		} else {
			sb.append("||source=");
		}
		if (searchRequest.extraSource() != null
				&& searchRequest.extraSource().length() > 0) {
			try {
				sb.append("||extra_source=").append(
						XContentHelper.convertToJson(
								searchRequest.extraSource(), true));
			} catch (IOException e) {
				sb.append("||extra_source=_failed_to_convert_");
			}
		} else {
			sb.append("||extra_source=");
		}
		
		sb.append("||");
		
		sb.append("tookInMillis=").append(searchResponse.getTookInMillis())
				.append("||");
		
		sb.append("scrollId=").append(searchResponse.getScrollId())
				.append("||");
		
		sb.append("totalShards=").append(searchResponse.getTotalShards())
				.append("||");
		
		sb.append("failedShards=")
		.append(searchResponse.getTotalShards()-searchResponse.getSuccessfulShards()).append("||");
		
		sb.append("isTimedOut=")
				.append(searchResponse.isTimedOut()).append("||");		
		
		sb.append("totalHits=").append(searchResponse.getHits().getTotalHits());
		
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

				// analysisManager.offerContxtToAnalysis(queryContext);
				super.onResponse(searchResponse);
			}
		};
	}
}
