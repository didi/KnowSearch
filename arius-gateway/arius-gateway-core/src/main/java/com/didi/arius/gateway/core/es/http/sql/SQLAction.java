package com.didi.arius.gateway.core.es.http.sql;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.consts.RestConsts;
import com.didi.arius.gateway.common.metadata.FetchFields;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.es.http.HttpRestHandler;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchResponse;
import com.didi.arius.gateway.query.SqlElasticRequestBuilder;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.collect.Tuple;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

@Component("sqlAction")
public class SQLAction extends HttpRestHandler {

	public static final String NAME = "sql";

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void handleRequest(QueryContext queryContext) throws Exception {

		checkFlowLimit(queryContext);

		String sql = queryContext.getPostBody();
		if (sql.endsWith(";")) {
			sql = sql.substring(0, sql.length() - 1);
		}

		SqlElasticRequestBuilder requestBuilder = buildRequest(sql);
		SearchRequest searchRequest = (SearchRequest) requestBuilder.request();

		List<String> indices = Arrays.asList(searchRequest.indices());
		queryContext.setIndices(indices);
		queryContext.setTypeNames(searchRequest.types());
		checkIndices(queryContext);

        ESSearchRequest esSearchRequest = new ESSearchRequest();
        esSearchRequest.indices(searchRequest.indices());

		IndexTemplate indexTemplate = null;
		if (isIndexType(queryContext)) {
		    if (indices.size() == 1) {
                indexTemplate = getTemplateByIndex(indices, queryContext);
            }

		    if (indexTemplate != null) {
                String dateFrom = queryContext.getRequest().param(RestConsts.SEARCH_DATE_FROM_PARAMS);
                String dateTo = queryContext.getRequest().param(RestConsts.SEARCH_DATE_TO_PARAMS);

                String[] newIndices = getQueryIndices(indexTemplate, dateFrom, dateTo);

                esSearchRequest.indices(newIndices);
            } else {
		        indexTemplate = getTemplateByIndexTire(indices, queryContext);
            }

		    // 该索引模板需要根据type名称进行映射到对应的索引模板
		    if (isNeedChangeIndexName(queryContext, indexTemplate)) {
				Tuple<IndexTemplate/*dest template*/, String[]/*dest indexNames*/> changeResult =
						handleChangeIndexName(queryContext, indexTemplate, searchRequest.indices(), searchRequest.types());

				// 替换查询语句中的索引名称
				esSearchRequest.indices(changeResult.v2());
				// 再替换索引模板对象
				indexTemplate = changeResult.v1();
			}

		}

        esSearchRequest.types(searchRequest.types());
        esSearchRequest.source(searchRequest.source());
		Map<String, String> params = queryContext.getRequest().params();
		params.remove("source");
		params.remove("index");
		params.remove("type");
		params.remove("filter_path");
		params.remove(RestConsts.SEARCH_DATE_FROM_PARAMS);
		params.remove(RestConsts.SEARCH_DATE_TO_PARAMS);
		params.put(QueryConsts.SEARCH_IGNORE_THROTTLED, "false");
		if (searchRequest.routing() != null && !params.containsKey("routing")) {
			params.put("routing", searchRequest.routing());
		}

		if (searchRequest.scroll() != null && searchRequest.scroll().keepAlive() != null && !params.containsKey("scroll")) {
			params.put("scroll", searchRequest.scroll().keepAlive().toString());
		}

		esSearchRequest.setParams(params);


//		FetchFields fetchFields = new FetchFields();
//		String strSource = XContentHelper.convertToJson(searchRequest.source(), false);
//		BytesReference separateSource = separateFields(strSource, fetchFields);
//		searchRequest.source(separateSource);

        esSearchRequest.putHeader("requestId", queryContext.getRequestId());
        esSearchRequest.putHeader("Authorization", queryContext.getRequest().getHeader("Authorization"));

		// 日期索引加上*号后缀，支持异常索引修复方案
		Convert.convertIndices(esSearchRequest);

		ESClient client = esClusterService.getClient(queryContext, indexTemplate);

		// pre process
		preSearchProcess(queryContext, client, esSearchRequest);

		FetchFields fetchFields = formFetchFields(esSearchRequest);
		queryContext.setFetchFields(fetchFields);

		if (queryContext.isDetailLog()) {
			statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_request_before||requestId={}||before_cost={}",
					queryContext.getRequestId(), (System.currentTimeMillis() - queryContext.getRequestTime()));
		}

		ActionListener<ESSearchResponse> listener = newSearchListener(queryContext);
		client.search(esSearchRequest, listener);
	}
}
