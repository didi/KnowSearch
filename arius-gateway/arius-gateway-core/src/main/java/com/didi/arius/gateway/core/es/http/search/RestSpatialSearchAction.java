package com.didi.arius.gateway.core.es.http.search;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.FetchFields;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.ESAction;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchResponse;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.support.RestActions;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

@Component("restSpatialSearchAction")
public class RestSpatialSearchAction extends ESAction {

    @Override
    public String name() {
        return "spatial_search";
    }

    @Override
    public void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) {
        String index = queryContext.getRequest().param("index");
        if (Strings.hasText(index) == false) {
            throw new IllegalArgumentException("index must not be null");
        }

        if (index.endsWith("*")) {
            throw new IllegalArgumentException("index must not contain the following characters [ , \\\", *, \\\\, <, |, ,, >, /, ?]");
        }

        String[] indicesArr = Strings.splitStringByCommaToArray(index);
        if (indicesArr == null || indicesArr.length != 1) {
            throw new IllegalArgumentException("only one index");
        }

        handle(queryContext, request, new ESSearchRequest("/_spatial_search"));
    }

    private void handle(QueryContext queryContext, RestRequest request, ESSearchRequest esSearchRequest) {
        long start = System.currentTimeMillis();
        esSearchRequest.indices(Strings.splitStringByCommaToArray(request.param("index")));
        esSearchRequest.types(Strings.splitStringByCommaToArray(request.param("type")));
        esSearchRequest.setTemplateRequest(request.path().endsWith("/template"));
        esSearchRequest.source(RestActions.getRestContent(request));
        Map<String, String> params = request.params();
        params.remove("source");
        params.remove("index");
        params.remove("type");
        params.remove("filter_path");
        params.put(QueryConsts.SEARCH_IGNORE_THROTTLED, "false");
        esSearchRequest.setParams(params);

        esSearchRequest.extraSource(RestSearchAction.parseSearchExtraSource(request));

        FetchFields fetchFields = formFetchFields(esSearchRequest);
        queryContext.setFetchFields(fetchFields);

        esSearchRequest.putHeader("requestId", queryContext.getRequestId());
        esSearchRequest.putHeader("Authorization", request.getHeader("Authorization"));

        long paramTime = System.currentTimeMillis();

        IndexTemplate indexTemplate = null;
        if (isIndexType(queryContext)) {
            List<String> indices = queryContext.getIndices();
            if (indices.size() == 1) {
                indexTemplate = getTemplateByIndex(indices, queryContext);
            }

            if (indexTemplate == null) {
                indexTemplate = getTemplateByIndexTire(indices, queryContext);
            }
        }

        long indexTemplateTime = System.currentTimeMillis();

        ESClient readClient = esClusterService.getClient(queryContext, indexTemplate);

        long getClientTime = System.currentTimeMillis();

        // pre process
        preSearchProcess(queryContext, readClient, esSearchRequest);

        long preProcessTime = System.currentTimeMillis();

        ActionListener<ESSearchResponse> listener = newSearchListener(queryContext);
        readClient.search(esSearchRequest, listener);

        statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_search_internal||requestId={}||paramCost={}||indexTemplateCost={}||getClientCost={}||preProcessCost={}||searchCost={}",
                queryContext.getRequestId(), paramTime - start, indexTemplateTime - paramTime, getClientTime - indexTemplateTime, preProcessTime - getClientTime, System.currentTimeMillis() - preProcessTime);
    }
}
