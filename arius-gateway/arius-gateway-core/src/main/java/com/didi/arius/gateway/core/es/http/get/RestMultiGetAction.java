/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.didi.arius.gateway.core.es.http.get;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.FetchFields;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.JoinLogContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.ESAction;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESMultiGetRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESMultiGetResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.support.RestActions;
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

@Component("restMultiGetAction")
public class RestMultiGetAction extends ESAction {

	public static final String AUTHORIZATION = "Authorization";
	
	@Override
	public String name() {
		return "multiGet";
	}
	
    private static final boolean allowExplicitIndex = true;

    @Override
	public void handleInterRequest(final QueryContext queryContext, RestRequest request, RestChannel channel)
			throws Exception {
		ESMultiGetRequest esMultiGetRequest = new ESMultiGetRequest();

		Map<String, String> params = request.params();
		esMultiGetRequest.setParams(params);

		esMultiGetRequest.refresh(request.param("refresh"));
		esMultiGetRequest.preference(request.param("preference"));
		esMultiGetRequest.realtime(request.paramAsBoolean("realtime", null));
		esMultiGetRequest.ignoreErrorsOnGeneratedFields(request.paramAsBoolean("ignore_errors_on_generated_fields", false));

        FetchSourceContext defaultFetchSource = FetchSourceContext.parseFromRestRequest(request);
		esMultiGetRequest.add(request.param("index"), request.param("type"), null, defaultFetchSource, request.param("routing"), RestActions.getRestContent(request), allowExplicitIndex);

        // multi get body may contain index, so this will check indices again
        addIndices(queryContext, esMultiGetRequest);
        checkIndicesAndTemplateBlockRead(queryContext);

		List<String> indices = queryContext.getIndices();
		IndexTemplate indexTemplate = null;
		if (isIndexType(queryContext)) {
			indexTemplate = getTemplateByIndexTire(indices, queryContext);

			// 该索引模板需要根据type名称进行映射到对应的索引模板
			if (isNeedChangeIndexName(queryContext, indexTemplate)) {

				ESMultiGetRequest resetMultiGetRequest = new ESMultiGetRequest();
				resetMultiGetRequest.setParams(params);
				resetMultiGetRequest.refresh(request.param("refresh"));
				resetMultiGetRequest.preference(request.param("preference"));
				resetMultiGetRequest.realtime(request.paramAsBoolean("realtime", null));
				resetMultiGetRequest.ignoreErrorsOnGeneratedFields(request.paramAsBoolean("ignore_errors_on_generated_fields", false));

				Map<String/*typeName*/,String/*templateName*/> typeIndexMapping = indexTemplate.getMasterInfo().getTypeIndexMapping();
				String sourceTemplateName = indexTemplate.getName();
				String destTemplateName = null;

				List<String> sourceIndexNameList = Lists.newArrayList();
				List<String> typeNameList = Lists.newArrayList();
				List<String> destIndexNameList = Lists.newArrayList();

				// 取出每一个get请求中的index和type信息
				for (ESMultiGetRequest.Item item : esMultiGetRequest.getItems()) {
					String typeName = item.type();
					String sourceIndexName = item.index();
					String destIndexName = sourceIndexName;
					sourceIndexNameList.add(sourceIndexName);
					typeNameList.add(typeName);

					// 用户指定type方式查询时，gateway需要将该多type索引和指定的type名称映射为对应的单type索引，然后转发到es。
					if (StringUtils.isNoneBlank(typeName)) {
						destTemplateName = typeIndexMapping.get(typeName);
						if (StringUtils.isNoneBlank(destTemplateName)) {
							destIndexName = sourceIndexName.replace(sourceTemplateName, destTemplateName);
							indexTemplate = indexTemplateService.getIndexTemplate(destTemplateName);
						}
						destIndexNameList.add(destIndexName);
						item.index(destIndexName).type(typeName);
						resetMultiGetRequest.add(item);

					} else {
						// 用户不指定type方式查询时，gateway需要将该多type索引映射为多个单type索引，然后转发到es，数据聚合功能由es完成。
						for (String name : typeIndexMapping.values()) {
							destTemplateName = name;
							destIndexName = sourceIndexName.replace(sourceTemplateName, name);
							destIndexNameList.add(destIndexName);
							item.index(destIndexName).type(typeName);
							resetMultiGetRequest.add(item);
						}
						indexTemplate = indexTemplateService.getIndexTemplate(destTemplateName);
					}
				}
				// 替换mget请求
				esMultiGetRequest = resetMultiGetRequest;
				if (queryContext.isDetailLog()) {
					JoinLogContext joinLogContext = queryContext.getJoinLogContext();
					joinLogContext.setSourceIndexNames(StringUtils.join(sourceIndexNameList, ","));
					joinLogContext.setTypeName(StringUtils.join(typeNameList, ","));
					joinLogContext.setDestIndexName(StringUtils.join(destIndexNameList, ","));
					joinLogContext.setSourceTemplateName(sourceTemplateName);
					joinLogContext.setDestTemplateName(destTemplateName);
				}
			}
		}

		ESClient readClient = esClusterService.getClient(queryContext, indexTemplate, actionName);

        //for kibana
        for (String index : indices) {
        	if (index.startsWith(".kibana")) {
        		readClient = esRestClientService.getAdminClient(actionName);
        		break;
        	}
        }

		final List<FetchFields> fetchFieldsList = new ArrayList<>(esMultiGetRequest.getItems().size());
        for (ESMultiGetRequest.Item item : esMultiGetRequest.getItems()) {
        	FetchFields fetchFields = new FetchFields();
			fetchFields.setFields(item.fields());
        	fetchFields.setFetchSourceContext(item.fetchSourceContext());
            if (fetchFields.getFields() != null) {
                for (String field : fetchFields.getFields()) {
        			if (field.equals(QueryConsts.MESSAGE_FIELD)) {
        				fetchFields.setHasMessageField(true);
        				break;
        			}
        		}
            }
            
            fetchFieldsList.add(fetchFields);
        }
        
        boolean needGetVersion = false;
		ESMultiGetRequest newMultiGetRequest = new ESMultiGetRequest();
        final List<Integer> versionPos = new ArrayList<>();
        final List<Integer> versionValue = new ArrayList<>();
        int pos = 0;
        for (ESMultiGetRequest.Item item : esMultiGetRequest.getItems()) {
        	String index = item.index();
        	int indexVersion = indexTemplateService.getIndexVersion(index, queryContext.getCluster());
        	if (indexVersion > 0) {
        		versionPos.add(pos);
        		versionValue.add(indexVersion);
        		needGetVersion = true;
        		for (int i = indexVersion; i >= 0; i--) {
        			String inIndex = item.index();
                	if (i > 0) {
                		inIndex = index+"_v"+i;
                	}

					ESMultiGetRequest.Item newItem = new ESMultiGetRequest.Item(inIndex, item.type(), item.id());

					newItem.fetchSourceContext(item.fetchSourceContext());
        			newItem.version(item.version());
					newItem.versionType(item.versionType());
        			newItem.fields(item.fields());
        			newItem.routing(item.routing());
        			
        			newMultiGetRequest.add(newItem);
        			
        			++pos;
        		}
        	} else {
        		newMultiGetRequest.add(item);
        		
        		++pos;
        	}
        }
        
        if (needGetVersion) {
			ActionListener<ESMultiGetResponse> listener = new RestActionListenerImpl<ESMultiGetResponse>(queryContext) {
				@Override
				public void onResponse(ESMultiGetResponse response) {
					List<ESMultiGetResponse.Item> itemList = new ArrayList<>();
					Iterator<Integer> posIter = versionPos.iterator();
					Iterator<Integer> versionIter = versionValue.iterator();
					int itemPos = 0;
					int currentVersion = 0;
					if (posIter.hasNext()) {
						itemPos = posIter.next();
						currentVersion = versionIter.next();
					}

					for (int i = 0; i < response.getResponses().size(); ++i) {
						ESMultiGetResponse.Item item = response.getResponses().get(i);
						if (i == itemPos) {
							ESMultiGetResponse.Item newGetItemResponse = null;
							for (int p = 0; p <= currentVersion; p++) {
								newGetItemResponse = response.getResponses().get(i + p);
								if (newGetItemResponse.getResponse() != null && newGetItemResponse.getResponse().isExists()) {
									break;
								}
							}

							itemList.add(newGetItemResponse);

							for (int p = 1; p <= currentVersion; p++) {
								i++;
							}

							if (posIter.hasNext()) {
								itemPos = posIter.next();
								currentVersion = versionIter.next();
							}
						} else {
							itemList.add(item);
						}
					}

					ESMultiGetResponse newResponse = new ESMultiGetResponse();
					newResponse.setResponses(itemList);
					super.onResponse(newResponse);
				}
			};

			newMultiGetRequest.putHeader("requestId", queryContext.getRequestId());
			newMultiGetRequest.putHeader(AUTHORIZATION, queryContext.getRequest().getHeader(AUTHORIZATION));

			readClient.multiGet(newMultiGetRequest, listener);
        } else {
			ActionListener<ESMultiGetResponse> listener = new RestActionListenerImpl<>(queryContext);
			esMultiGetRequest.putHeader("requestId", queryContext.getRequestId());
			esMultiGetRequest.putHeader(AUTHORIZATION, queryContext.getRequest().getHeader(AUTHORIZATION));
			readClient.multiGet(esMultiGetRequest, listener);
        }
        
    }
    
    private void addIndices(QueryContext queryContext, ESMultiGetRequest request) {
    	Set<String> typeNames = Sets.newHashSet();
    	for (ESMultiGetRequest.Item item : request.getItems()) {
    		queryContext.addIndex(item.index());
			typeNames.add(item.type());
    	}
		queryContext.setTypeNames(typeNames);
    }
}
