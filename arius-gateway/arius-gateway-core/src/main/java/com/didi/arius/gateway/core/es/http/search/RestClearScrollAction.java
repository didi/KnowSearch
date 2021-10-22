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

package com.didi.arius.gateway.core.es.http.search;

import com.didi.arius.gateway.common.exception.InvalidParameterException;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.ESAction;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESClearScrollRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESClearScrollResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.support.RestActions;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

/**
 */
@Component("restClearScrollAction")
public class RestClearScrollAction extends ESAction {

	public static final String NAME = "clearScroll";
	
	@Override
	public String name() {
		return NAME;
	}
	
    @Override
	public void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel)
			throws Exception {
        String scrollIds = request.param("scroll_id");

        ClearScrollRequest clearRequest = new ClearScrollRequest();
        clearRequest.setScrollIds(Arrays.asList(splitScrollIds(scrollIds)));
        if (RestActions.hasBodyContent(request)) {
            XContentType type = RestActions.guessBodyContentType(request);
           if (type == null) {
               scrollIds = RestActions.getRestContent(request).toUtf8();
               clearRequest.setScrollIds(Arrays.asList(splitScrollIds(scrollIds)));
           } else {
               // NOTE: if rest request with xcontent body has request parameters, these parameters does not override xcontent value
               clearRequest.setScrollIds(null);
               buildFromContent(RestActions.getRestContent(request), clearRequest);
           }
        }

        ESClient readClient;
        List<String> scrolls;
        if (isIndexType(queryContext)) {
            String cluster = null;

            scrolls = new ArrayList<>();
            for (String scrollIdWrap : clearRequest.getScrollIds()) {
                int pos = scrollIdWrap.indexOf(SCROLL_SPLIT);
                if (pos <= 0) {
                    throw new InvalidParameterException("scrollId format error, scrollId=" +  scrollIdWrap);
                }

                String clusterEncode = scrollIdWrap.substring(0, pos);
                byte[] bytes = Base64.getUrlDecoder().decode(clusterEncode);
                String inCluster = new String(bytes);

                if (cluster == null) {
                    cluster = inCluster;
                } else if (false == cluster.equals(inCluster)) {
                    throw new InvalidParameterException("scrollId cluster error, scrollId=" +  scrollIdWrap);
                }

                String realScrollId = scrollIdWrap.substring(pos+1);
                scrolls.add(realScrollId);
            }

            readClient = esClusterService.getClientFromCluster(queryContext, cluster);
        } else {
            readClient = esClusterService.getClient(queryContext);
            scrolls = clearRequest.getScrollIds();
        }

        ESClearScrollRequest esClearScrollRequest = new ESClearScrollRequest();
        esClearScrollRequest.setScrollIds(scrolls);

        esClearScrollRequest.putHeader("requestId", queryContext.getRequestId());
        esClearScrollRequest.putHeader("Authorization", request.getHeader("Authorization"));

        RestActionListenerImpl<ESClearScrollResponse> listener = new RestActionListenerImpl<>(queryContext);
        readClient.clearScroll(esClearScrollRequest, listener);
    }

    public static String[] splitScrollIds(String scrollIds) {
        if (scrollIds == null) {
            return Strings.EMPTY_ARRAY;
        }
        return Strings.splitStringByCommaToArray(scrollIds);
    }

    public static void buildFromContent(BytesReference content, ClearScrollRequest clearScrollRequest) {
        try (XContentParser parser = XContentHelper.createParser(content)) {
            if (parser.nextToken() != XContentParser.Token.START_OBJECT) {
                throw new IllegalArgumentException("Malformed content, must start with an object");
            } else {
                XContentParser.Token token;
                String currentFieldName = null;
                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        currentFieldName = parser.currentName();
                    } else if ("scroll_id".equals(currentFieldName) && token == XContentParser.Token.START_ARRAY) {
                        while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                            if (token.isValue() == false) {
                                throw new IllegalArgumentException("scroll_id array element should only contain scroll_id");
                            }
                            clearScrollRequest.addScrollId(parser.text());
                        }
                    } else {
                        throw new IllegalArgumentException("Unknown parameter [" + currentFieldName + "] in request body or parameter is of the wrong type[" + token + "] ");
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse request body", e);
        }
    }

}
