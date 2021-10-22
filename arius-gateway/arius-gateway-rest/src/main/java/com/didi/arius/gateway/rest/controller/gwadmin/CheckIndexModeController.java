package com.didi.arius.gateway.rest.controller.gwadmin;

import com.didi.arius.gateway.common.exception.TooManyIndexException;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.IndexTire;
import com.didi.arius.gateway.rest.controller.AdminController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author fitz
 * @date 2021/5/25 2:10 下午
 */
@Controller
public class CheckIndexModeController extends AdminController {
    public static final String NAME = "checkIndexMode";

    @Value("${gateway.log.index.name}")
    private String indexNames;

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_gwadmin/checkIndexMode/{appid}", this);
        controller.registerHandler(RestRequest.Method.GET, "/_gwadmin/checkIndexMode", this);
    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {

        int appid = request.paramAsInt("appid", 0);
        Map<Integer, Map<String, AtomicInteger>> checkResult = new TreeMap<>();

        if (appid == 0) {
            for (int inAppid : appService.getAppDetails().keySet()) {
                Map<String, AtomicInteger> appidCount = checkAppidResult(inAppid, client, queryContext, request);
                checkResult.put(inAppid, appidCount);
            }
        } else {
            Map<String, AtomicInteger> appidCount = checkAppidResult(appid, client, queryContext, request);
            checkResult.put(appid, appidCount);
        }

        StringBuilder builder = new StringBuilder("final check result:\n");
        builder.append(String.format("appid:%40s:count\n", "index"));
        for (Map.Entry<Integer, Map<String, AtomicInteger>> entry : checkResult.entrySet()) {
            int outAppid = entry.getKey();
            for (Map.Entry<String, AtomicInteger> inEntry : entry.getValue().entrySet()) {
                builder.append(String.format("%5d:%40s:%d\n", outAppid, inEntry.getKey(), inEntry.getValue().get()));
            }
        }

        logger.info(builder.toString());

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, builder.toString()));

    }

    private Map<String, AtomicInteger> checkAppidResult(int inAppid, ESClient client, QueryContext queryContext, RestRequest request) {
        ESSearchRequest esSearchRequest = new ESSearchRequest();
        esSearchRequest.indices(indexNames);
        esSearchRequest.putHeader("requestId", queryContext.getRequestId());
        esSearchRequest.putHeader("Authorization", request.getHeader("Authorization"));

        esSearchRequest.source(String.format("{\n" +
                "  \"size\": 0, \n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"dltag\": {\n" +
                "              \"value\": \"_arius_query_request_indices\"\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"appid\": {\n" +
                "              \"value\": \"%d\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"must_not\": [\n" +
                "        {\n" +
                "          \"terms\": {\n" +
                "            \"clientHost\": [\n" +
                "              \"bigdata-arius-ser207.gz01\",\n" +
                "              \"arius-gateway-pre-sf-cf92c-0.docker.ys\",\n" +
                "              \"arius-gateway-console-sf-cf92c-0.docker.ys\",\n" +
                "              \"arius-gateway-console-sf-cf92c-1.docker.ys\",\n" +
                "              \"arius-gateway-console-sf-cf92c-2.docker.ys\"\n" +
                "            ]\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }, \n" +
                "  \"aggs\": {\n" +
                "    \"2\": {\n" +
                "      \"terms\": {\n" +
                "        \"field\": \"indices\",\n" +
                "        \"size\": 10000\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}", inAppid));

        ESSearchResponse response = client.search(esSearchRequest)
                .actionGet();

        List<Object> buckets = (List<Object>) ((Map<String, Object>)response.getAggregations().get("2")).get("buckets");
        Map<String, AtomicInteger> appidCount = new LinkedHashMap<>();

        for (Object obj : buckets) {
            try {
                Map<String, Object> bucket = (Map<String, Object>) obj;
                String indices = (String) bucket.get("key");
                int count = Integer.valueOf(bucket.get("doc_count").toString());

                Set<String> indicesSet = Strings.splitStringToSet(indices, ',');
                Iterator<String> it = indicesSet.iterator();
                while (it.hasNext()) {
                    String str = it.next();
                    if(str.startsWith(".")){
                        it.remove();
                    }
                }

                if (indicesSet.size() == 0) {
                    continue;
                }

                Iterator<String> iter = indicesSet.iterator();
                String checkIndex = iter.next();
                try {
                    IndexTemplate indexTemplate = indexTemplateService.getIndexTemplateByTire(checkIndex);
                    if (indexTemplate == null) {
                        String alias = indexTemplateService.getIndexAlias(checkIndex);
                        if (alias != null) {
                            indexTemplate = indexTemplateService.getIndexTemplateByTire(alias);
                        }
                    }

                    if (indexTemplate == null) {
                        AtomicInteger notFoundCount = appidCount.get("index_not_found_" + checkIndex);
                        if (notFoundCount == null) {
                            notFoundCount = new AtomicInteger(0);
                            appidCount.put("index_not_found_" + checkIndex, notFoundCount);
                        }

                        notFoundCount.addAndGet(count);
                        logger.warn("appid={} index_not_found, index={}", inAppid, indices);
                        continue;
                    }

                    boolean same = true;
                    AtomicInteger moreCount = appidCount.get("index_more_then_one");
                    if (moreCount == null) {
                        moreCount = new AtomicInteger(0);
                        appidCount.put("index_more_then_one", moreCount);
                    }

                    while (iter.hasNext()) {
                        String otherIndex = iter.next();
                        boolean check = IndexTire.checkIndexMatchTemplate(otherIndex, indexTemplate);
                        if (!check) {
                            String alias = indexTemplateService.getIndexAlias(otherIndex);
                            if (alias != null) {
                                check = IndexTire.checkIndexMatchTemplate(alias, indexTemplate);
                            }
                        }

                        if (!check) {
                            logger.warn("appid={} index_more_then_one, index={}, indices={}", inAppid, otherIndex, indices);
                            same = false;
                            break;
                        }
                    }

                    if (!same) {
                        moreCount.addAndGet(count);
                        continue;
                    }

                    AtomicInteger indexCount = appidCount.get(indexTemplate.getExpression());
                    if (indexCount == null) {
                        appidCount.put(indexTemplate.getExpression(), new AtomicInteger(count));
                    } else {
                        indexCount.addAndGet(count);
                    }
                } catch (TooManyIndexException e) {
                    AtomicInteger moreCount = appidCount.get("index_more_then_one");
                    if (moreCount == null) {
                        moreCount = new AtomicInteger(0);
                        appidCount.put("index_more_then_one", moreCount);
                    }

                    moreCount.addAndGet(count);

                    logger.warn("appid={} index_more_then_one, indices={}", inAppid, indices);
                }
            } catch (Throwable e) {
                logger.warn("appid={} exception obj={}", inAppid, obj, e);
            }
        }

        return appidCount;
    }
}
