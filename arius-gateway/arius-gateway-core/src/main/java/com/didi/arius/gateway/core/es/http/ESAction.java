package com.didi.arius.gateway.core.es.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.JoinLogContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectRequest;
import com.google.common.collect.Lists;

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
            List<String> finalIndices = indices;
            List<String> list = new ArrayList<String>(6) {
                {
                    add("/");
                    add(StringUtils.join(finalIndices, ","));
                }
            };
            if (StringUtils.isNotBlank(type)) {
                list.add("/");
                list.add(type);
            }
            list.add(api);
            path = StringUtils.join(list, "");
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
}
