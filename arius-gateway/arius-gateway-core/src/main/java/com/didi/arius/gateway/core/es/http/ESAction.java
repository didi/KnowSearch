package com.didi.arius.gateway.core.es.http;

import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.JoinLogContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectRequest;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

public abstract class ESAction extends HttpRestHandler {

	private static final String PATH_DELIMITER = "/";

	@Override
	public void handleRequest(QueryContext queryContext) throws Exception {
		RestRequest request = queryContext.getRequest();
		RestChannel channel = queryContext.getChannel();

		String[]  indicesArr = Strings.splitStringByCommaToArray(request.param("index"));
		List<String> indices = Lists.newArrayList(indicesArr);
		queryContext.setIndices(indices);
		queryContext.setTypeNames(request.param("type"));

		if (queryContext.isDetailLog()) {
			JoinLogContext joinLogContext = queryContext.getJoinLogContext();
			joinLogContext.setBeforeCost(System.currentTimeMillis() - queryContext.getRequestTime());
			joinLogContext.setIndices(StringUtils.join(indices, ","));
			joinLogContext.setTypeName(request.param("type"));
		}

		checkFlowLimit(queryContext);

		if (isOriginCluster(queryContext)) {
			handleOriginClusterRequest(queryContext);
		} else {
			if (isNeededCheckIndices()) {
				if (isNeededCheckTemplateSearchBlockAction()) {
					checkIndicesAndTemplateBlockRead(queryContext);
				} else {
					checkIndices(queryContext);
				}
			}

			handleInterRequest(queryContext, request, channel);
		}
	}

	protected abstract void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception;

    protected void indexAction(QueryContext queryContext, String index) {
        IndexTemplate indexTemplate = preIndexAction(queryContext, index);
        ESClient client = esClusterService.getClient(queryContext, indexTemplate, actionName);
        directRequest(client, queryContext);
    }

    protected void indexAction(QueryContext queryContext, String index, String api) {
        IndexTemplate indexTemplate = preIndexAction(queryContext, index);
        List<String> indices = Lists.newArrayList();
        //传入索引，且模版为分区模板或者不分区模板升了版本，则将传入索引加上'*'
        if (StringUtils.isNotBlank(index) && null != indexTemplate
            && (indexTemplate.getExpression().endsWith("*") || indexTemplate.getVersion() > 0)) {
            String[] indicesArr = Strings.splitStringByCommaToArray(index);
            indices = Lists.newArrayList(Convert.convertIndices(indicesArr));
            queryContext.setIndices(indices);
        }
        ESClient client = esClusterService.getClient(queryContext, indexTemplate, actionName);
        String path = queryContext.getUri();
        String type = queryContext.getTypeNames();
        if (CollectionUtils.isNotEmpty(indices)) {
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append(PATH_DELIMITER).append(indices.stream().distinct().collect(Collectors.joining(",")));
            if (StringUtils.isNotBlank(type)) {
                pathBuilder.append(PATH_DELIMITER).append(type);
            }
            if (null != api && api.trim().length() > 0 && !api.startsWith(PATH_DELIMITER)) {
                pathBuilder.append(PATH_DELIMITER);
            }
            pathBuilder.append(api);
            path = pathBuilder.toString();
        }
        DirectRequest directRequest = buildDirectRequest(queryContext, path);
        directRequest(client, queryContext, directRequest);
    }

	protected IndexTemplate preIndexAction(QueryContext queryContext, String index) {
		String[] indicesArr = Strings.splitStringByCommaToArray(index);
		List<String> indices = Lists.newArrayList(indicesArr);
		queryContext.setIndices(indices);
		checkIndices(queryContext);
		IndexTemplate indexTemplate = null;
		if (!queryContext.isFromKibana()) {
			// kibana的请求就不需要搜索模版了
			indexTemplate = getTemplateByIndexTire(indices, queryContext);
			queryContext.setIndexTemplate(indexTemplate);
		}
		return indexTemplate;
	}

	protected boolean isAliasGet(IndexTemplate indexTemplate, List<String> indexs){
		if(CollectionUtils.isEmpty(indexTemplate.getAliases())){
			return false;
		}

		for(String index : indexs){
			if(indexTemplate.getAliases().contains( index )){
				return true;
			}
		}

		return false;
	}
	
	/**
	 * 如果存在 filter_path 参数，则将 take、timed_out、_shards 和 hits 添加到 filter_path
	 *
	 * @param params 要传递给 Elasticsearch API 的参数。
	 */
	protected void addFilterPathDefaultValue(Map<String, String> params) {
		if (params.containsKey("filter_path")) {
			Set<String> filterPath = Sets.newHashSet(StringUtils.split(params.get("filter_path"), ","));
			filterPath.add("took");
			filterPath.add("timed_out");
			filterPath.add("_shards");
			filterPath.add("hits");
			params.put("filter_path", String.join(",", filterPath));
		}
	}
}