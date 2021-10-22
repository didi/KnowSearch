package com.didi.arius.gateway.core.es.http;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.exception.InvalidParameterException;
import com.didi.arius.gateway.common.exception.SQLNotPermittedException;
import com.didi.arius.gateway.common.exception.TooManyIndexException;
import com.didi.arius.gateway.common.metadata.BaseContext;
import com.didi.arius.gateway.common.metadata.FetchFields;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.common.utils.DateUtil;
import com.didi.arius.gateway.common.utils.IndexTire;
import com.didi.arius.gateway.common.metadata.WrapESGetResponse;
import com.didi.arius.gateway.core.service.arius.*;
import com.didi.arius.gateway.core.service.dsl.DslAggsAnalyzerService;
import com.didi.arius.gateway.core.service.dsl.DslAuditService;
import com.didi.arius.gateway.core.service.dsl.DslRewriterService;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESGetResponse;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESMultiGetRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESMultiGetResponse;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchResponse;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.response.src.Hit;
import com.didi.arius.gateway.query.AggregationQueryAction;
import com.didi.arius.gateway.query.DefaultQueryAction;
import com.didi.arius.gateway.query.QueryAction;
import com.didi.arius.gateway.query.SqlElasticRequestBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.elasticsearch.action.*;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.elasticsearch.threadpool.ThreadPool;
import org.nlpcn.es4sql.domain.Select;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.parse.ElasticSqlExprParser;
import org.nlpcn.es4sql.parse.SqlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author weizijun
* @date：2017年2月13日
*
*/
public abstract class ESBase {
	protected static final Logger logger = LoggerFactory.getLogger(ESBase.class);

	protected static final String SCROLL_SPLIT = "!";

	@Autowired
	protected DslAggsAnalyzerService dslAggsAnalyzerService;
	@Autowired
	protected DslAuditService dslAuditService;
	@Autowired
	protected DslRewriterService dslRewriterService;

	@Autowired
	protected IndexTemplateService indexTemplateService;

	@Autowired
	protected AppService appService;

	protected ESGetResponse getVersionResponse(int indexVersion, RestRequest request, ESClient client) {
        ESMultiGetRequest multiGetRequest = new ESMultiGetRequest();
        multiGetRequest.refresh(request.param("refresh"));
        multiGetRequest.preference(request.param("preference"));
        multiGetRequest.realtime(request.paramAsBoolean("realtime", null));
        multiGetRequest.ignoreErrorsOnGeneratedFields(request.paramAsBoolean("ignore_errors_on_generated_fields", false));

        String[] sFields = null;
        String sField = request.param("fields");
        if (sField != null) {
            sFields = Strings.splitStringByCommaToArray(sField);
        }

        FetchSourceContext defaultFetchSource = FetchSourceContext.parseFromRestRequest(request);
        for (int i = indexVersion; i >= 0; i--) {
        	String index = request.param("index");
        	if (i > 0) {
        		index = request.param("index")+"_v"+i;
        	}
			ESMultiGetRequest.Item item = new ESMultiGetRequest.Item(index, request.param("type"), request.param("id"));
        	item.routing(request.param("routing"));
        	item.parent(request.param("parent"));
        	item.fields(sFields);
        	item.fetchSourceContext(defaultFetchSource);

        	multiGetRequest.add(item);
        }

		ESMultiGetResponse response = client.multiGet(multiGetRequest).actionGet();

		ESGetResponse getResponse = null;
        for (ESMultiGetResponse.Item item : response.getResponses()) {
        	ESGetResponse inResponse = item.getResponse();
        	if (inResponse == null) {
        		continue;
        	}

        	if (inResponse.isExists()) {
        		getResponse = inResponse;
        		break;
        	}

        	getResponse = inResponse;
        }

        if (getResponse == null) {
			getResponse = new ESGetResponse();
			getResponse.setIndex(request.param("index"));
			getResponse.setType(request.param("type"));
			getResponse.setId(request.param("id"));
			getResponse.setExists(false);
        }

        return getResponse;
	}

	protected void getVersionResponse(QueryContext queryContext, int indexVersion, String indexName, final RestRequest request, ESClient client, final WrapESGetResponse.ResultType resultType) {
		ESMultiGetRequest multiGetRequest = new ESMultiGetRequest();
		multiGetRequest.refresh(request.param("refresh"));
		multiGetRequest.preference(request.param("preference"));
		multiGetRequest.realtime(request.paramAsBoolean("realtime", null));
		multiGetRequest.ignoreErrorsOnGeneratedFields(request.paramAsBoolean("ignore_errors_on_generated_fields", false));

		multiGetRequest.putHeader("requestId", queryContext.getRequestId());
		multiGetRequest.putHeader("Authorization", queryContext.getRequest().getHeader("Authorization"));

		String[] sFields = null;
		String sField = request.param("fields");
		if (sField != null) {
			sFields = Strings.splitStringByCommaToArray(sField);
		}

		FetchSourceContext defaultFetchSource = FetchSourceContext.parseFromRestRequest(request);
		for (int i = indexVersion; i >= 0; i--) {
			String index = indexName;
			if (i > 0) {
				index = indexName + "_v" + i;
			}
			ESMultiGetRequest.Item item = new ESMultiGetRequest.Item(index, request.param("type"), request.param("id"));
			item.routing(request.param("routing"));
			item.parent(request.param("parent"));
			item.fields(sFields);
			item.fetchSourceContext(defaultFetchSource);

			multiGetRequest.add(item);
		}

		ActionListener<ESMultiGetResponse> listener = new RestActionListenerImpl<ESMultiGetResponse>(queryContext) {
			@Override
			public void onResponse(ESMultiGetResponse response) {
				ESGetResponse getResponse = null;
				for (ESMultiGetResponse.Item item : response.getResponses()) {
					ESGetResponse inResponse = item.getResponse();
					if (inResponse == null) {
						continue;
					}

					if (inResponse.isExists()) {
						getResponse = inResponse;
						break;
					}

					getResponse = inResponse;
				}

				if (getResponse == null) {
					getResponse = new ESGetResponse();
					getResponse.setIndex(request.param("index"));
					getResponse.setType(request.param("type"));
					getResponse.setId(request.param("id"));
					getResponse.setExists(false);
				}

				RestActionListenerImpl<ESGetResponse> innerListener = new RestActionListenerImpl<>(queryContext);
				switch (resultType) {
					case ALL:
						innerListener.onResponse(getResponse);
						break;
					case HEAD:
						if (getResponse.isExists()) {
							innerListener.onResponse(new BytesRestResponse(RestStatus.OK));
						} else {
							innerListener.onResponse(new BytesRestResponse(RestStatus.NOT_FOUND));
						}
						break;
					case SOURCE:
						if (getResponse.isExists()) {
							innerListener.onResponse(new BytesRestResponse(RestStatus.OK, XContentType.JSON.restContentType(), ((JSONObject)getResponse.getSource()).toJSONString()));
						} else {
							innerListener.onResponse(new BytesRestResponse(RestStatus.NOT_FOUND));
						}

						break;
				}

			}
		};

		client.multiGet(multiGetRequest, listener);
	}

	protected MultiGetRequest getVersionRequest(int indexVersion, GetRequest request) {
        MultiGetRequest multiGetRequest = new MultiGetRequest();
        multiGetRequest.refresh(request.refresh());
        multiGetRequest.preference(request.preference());
        multiGetRequest.realtime(request.realtime());
        multiGetRequest.ignoreErrorsOnGeneratedFields(request.ignoreErrorsOnGeneratedFields());

        for (int i = indexVersion; i >= 0; i--) {
        	String index = request.index();
        	if (i > 0) {
        		index = request.index()+"_v"+i;
        	}
        	MultiGetRequest.Item item = new MultiGetRequest.Item(index, request.type(), request.id());
        	item.routing(request.routing());
        	item.fields(request.fields());
        	item.fetchSourceContext(request.fetchSourceContext());

        	multiGetRequest.add(item);
        }

        return multiGetRequest;
	}

	protected FetchFields formFetchFields(SearchRequest searchRequest) {
		FetchFields fetchFields = new FetchFields();
		formFetchFields(searchRequest.source(), fetchFields);
		formFetchFields(searchRequest.extraSource(), fetchFields);
		return fetchFields;
	}

	protected FetchFields formFetchFields(ESSearchRequest esSearchRequest) {
		FetchFields fetchFields = new FetchFields();
		formFetchFields(esSearchRequest.source(), fetchFields);
		formFetchFields(esSearchRequest.extraSource(), fetchFields);

		return fetchFields;
	}

	private void formFetchFields(BytesReference source, FetchFields fetchFields) {
		if (source == null || source.length() == 0) {
            return ;
        }

		try {
			String sourceStr = XContentHelper.convertToJson(source, true);
			formFetchFields(sourceStr, fetchFields);
		} catch (IOException e) {
			logger.warn("source_convertToJson_error||source={}||exception={}", source, Convert.logExceptionStack(e));
		}
	}

	private void formFetchFields(String source, FetchFields fetchFields) {
		if (source == null || source.length() == 0) {
            return ;
        }

    	JsonParser jsonParser = new JsonParser();
    	JsonObject jsonObject = jsonParser.parse(source).getAsJsonObject();

    	JsonElement sourceContext = jsonObject.get("_source");

    	if (sourceContext != null) {
    		FetchSourceContext fetchSourceContext = Convert.parseFetchSourceContext(sourceContext);
    	    fetchFields.setFetchSourceContext(fetchSourceContext);
    	}


    	JsonElement fields = jsonObject.get("fields");
    	if (fields != null) {
    		String[] strFields = Convert.parseFields(fields);
    		fetchFields.setFields(strFields);

    		for (String field : strFields) {
    			if (field.equals(QueryConsts.MESSAGE_FIELD)) {
    				fetchFields.setHasMessageField(true);
    				break;
    			}
    		}
    	}
    }

	protected String buildSearchIndex(BaseContext context, ESSearchResponse queryResponse) {
		StringBuilder sb = new StringBuilder();
		sb.append(QueryConsts.DLFLAG_PREFIX + "response_index||requestId=");
		sb.append(context.getRequestId());

		List<Hit> hits = queryResponse.getHits().getHits();
		Map<String, Integer> indexCount = new HashMap<>();
		if (hits != null) {
			for (Hit hit : hits) {
				String index = hit.getIndex();
				if (indexCount.containsKey(index)) {
					indexCount.put(index, indexCount.get(index) + 1);
				} else {
					indexCount.put(index, 1);
				}
			}
		}

		sb.append("||index=");
		sb.append(JSON.toJSONString(indexCount));
		return sb.toString();
	}


	protected Select parseSql(String sql) throws SqlParseException {
		if (sql == null || sql.isEmpty()) {
			throw new InvalidParameterException("no SQL content found");
		}

		if (!sql.trim().toLowerCase().startsWith("select")) {
			throw new SQLNotPermittedException("gateway only support SELECT SQL");
		}

		SQLQueryExpr sqlExpr = (SQLQueryExpr) toSqlExpr(sql);
		if(isJoin(sqlExpr,sql)){
			throw new SQLNotPermittedException("join SQL not allow");
		}

		Select select = new SqlParser().parseSelect(sqlExpr);
		if (select.containsSubQueries()) {
			throw new SQLNotPermittedException("subQueries SQL not allow");
		}

		return select;
	}

	private boolean isJoin(SQLQueryExpr sqlExpr, String sql) {
		MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) sqlExpr.getSubQuery().getQuery();
		return query.getFrom() instanceof SQLJoinTableSource && sql.toLowerCase().contains(" join ");
	}

	private SQLExpr toSqlExpr(String sql) {
		SQLExprParser parser = new ElasticSqlExprParser(sql);
		SQLExpr expr = parser.expr();

		if (parser.getLexer().token() != Token.EOF) {
			throw new ParserException("illegal sql expr : " + sql);
		}

		return expr;
	}

	protected SqlElasticRequestBuilder buildRequest(String sql) throws SqlParseException {
		Select select = parseSql(sql);

		QueryAction queryAction;
		if (select.isAgg) {
			queryAction = new AggregationQueryAction(select);
		} else {
			queryAction = new DefaultQueryAction(select);
		}

		return queryAction.explain(new SearchRequestBuilder(new ElasticsearchClient() {
			@Override
			public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> ActionFuture<Response> execute(Action<Request, Response, RequestBuilder> action, Request request) {
				return null;
			}

			@Override
			public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> void execute(Action<Request, Response, RequestBuilder> action, Request request, ActionListener<Response> listener) {

			}

			@Override
			public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> RequestBuilder prepareExecute(Action<Request, Response, RequestBuilder> action) {
				return null;
			}

			@Override
			public ThreadPool threadPool() {
				return null;
			}
		}, SearchAction.INSTANCE));
	}

	protected IndexTemplate getTemplateByIndex(List<String> indices, QueryContext queryContext) {
        if (indices.size() != 1) {
            throw new InvalidParameterException("index size overflow, you can only search one index, index size=" + indices.size());
        }

        String index = indices.get(0);
        if (index.endsWith("*")) {
            index = index.substring(0, index.length()-1);
        }

		IndexTemplate indexTemplate = indexTemplateService.getIndexTemplate(index);
        if (indexTemplate != null) {
			queryContext.setIndexTemplate(indexTemplate);
		}

        return indexTemplate;
    }

    protected IndexTemplate getTemplateByIndexTire(List<String> indices, QueryContext queryContext) throws IndexNotFoundException, TooManyIndexException{
        String index = indices.get(0);

        IndexTemplate indexTemplate = indexTemplateService.getIndexTemplateByTire(index);
        if (indexTemplate == null) {
        	String alias = indexTemplateService.getIndexAlias(index);
        	if (alias != null) {
				indexTemplate = indexTemplateService.getIndexTemplateByTire(alias);
			}
        }

		if (indexTemplate == null) {
			throw new IndexNotFoundException("query can't find index template exception,index=" + index);
		}

        for (int i = 1; i < indices.size(); ++i) {
            boolean check = IndexTire.checkIndexMatchTemplate(indices.get(i), indexTemplate);
            if (!check) {
				String alias = indexTemplateService.getIndexAlias(indices.get(i));
				if (alias != null) {
					check = IndexTire.checkIndexMatchTemplate(alias, indexTemplate);
				}
			}

            if (!check) {
                throw new TooManyIndexException(String.format("search query indices don't have the same index template, index1=%s, index2=%s", index, indices.get(i)));
            }
        }

		if (indexTemplate != null) {
			queryContext.setIndexTemplate(indexTemplate);
		}

        return indexTemplate;
    }

    protected String[] getQueryIndices(IndexTemplate indexTemplate, String dateFrom, String dateTo) {
        if (Strings.isEmpty(indexTemplate.getDateFormat()) ||
				(Strings.isEmpty(dateFrom) && Strings.isEmpty(dateTo))) {
            return new String[]{indexTemplate.getExpression()};
        } else {
        	long end = System.currentTimeMillis();

			long gap = QueryConsts.DAY_MILLIS * (indexTemplate.getExpireTime() <= 0 ? QueryConsts.DEFALUT_INDEX_DAY :  Math.min(indexTemplate.getExpireTime(), QueryConsts.DEFALUT_INDEX_DAY));
			long start = end - gap;

        	if (!Strings.isEmpty(dateFrom)) {
				start = DateUtil.transformToMillis(dateFrom);
			}

			if (!Strings.isEmpty(dateTo)) {
				end = DateUtil.transformToMillis(dateTo);
			}

			List<String> suffixes = DateUtil.getDateFormatSuffix(start, end, indexTemplate.getDateFormat());
			if (suffixes.isEmpty()) {
				throw new InvalidParameterException(String.format("time range error, from > end, from=%s, end=%s", dateFrom, dateTo));
			}

			if (suffixes.size() > QueryConsts.MAX_INDEX_COUNT) {
				throw new InvalidParameterException(String.format("time range error, get more then %d index, from=%s, end=%s, format=%s", QueryConsts.MAX_INDEX_COUNT, dateFrom, dateTo, indexTemplate.getDateFormat()));
			}

			String expression = indexTemplate.getExpression();
			expression = expression.replace("*", "");
			String[] newIndices = new String[suffixes.size()];
			int i = 0;
			for (String suffix : suffixes) {
				newIndices[i] = expression + suffix;
				i++;
			}

			return newIndices;
        }
    }
}
