package com.didi.arius.gateway.core.es.http;

import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.JoinLogContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.CommonUtil;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

import java.util.List;

public abstract class ESAction extends HttpRestHandler {
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
		preIndexAction(queryContext, index);
		doIndexAction(queryContext, queryContext.getIndexTemplate());
	}

	protected void preIndexAction(QueryContext queryContext, String index) {
		String[] indicesArr = Strings.splitStringByCommaToArray(index);
		List<String> indices = Lists.newArrayList(indicesArr);
		queryContext.setIndices(indices);
		checkIndices(queryContext);

		if (!queryContext.isFromKibana()) {
			// kibana的请求就不需要搜索模版了
			IndexTemplate indexTemplate = getTemplateByIndexTire(indices, queryContext);
			queryContext.setIndexTemplate(indexTemplate);
		}
	}

	protected void doIndexAction(QueryContext queryContext, IndexTemplate indexTemplate) {
		ESClient client = esClusterService.getClient(queryContext, indexTemplate, actionName);

		directRequest(client, queryContext);
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
}
