package com.didi.arius.gateway.core.es.http.document;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.exception.InvalidParameterException;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESIndexRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESIndexResponse;
import com.didichuxing.tunnel.util.log.LogGather;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.support.RestActions;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("restIndexAction")
public class RestIndexAction extends RestBaseWriteAction {
    public static final String NAME = "index";

    @Override
    public String name() {
        return NAME;
    }


    @Override
    public void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel)
            throws Exception {
        if (request.param("_create") != null) {
            if (request.param("_create").equals("_create")) {
                request.params().put("op_type", "create");
            } else {
                throw new IllegalArgumentException("Can't handle [" + request.method() + "] for path [" + request.rawPath() + "]");
            }
        }

        if (request.hasContent() == false) {
            throw  new InvalidParameterException("no source to write");
        }

        String index = request.param("index");
        IndexTemplate indexTemplate = getAndCheckIndexTemplate(index, queryContext);

        String strSource;
        if (XContentType.JSON != XContentFactory.xContentType(request.content())) {
            strSource = XContentHelper.convertToJson(request.content(), false);
        } else {
            strSource = request.content().toUtf8();
        }

        Map<String, Object> source = JSON.parseObject(strSource, HashMap.class);

        if (false == indexTemplate.isInternal()) {
            source.put(WRITE_TIME_FIELD, System.currentTimeMillis());
        }

        // 生成索引名称
        String indexName = getIndexName(indexTemplate, source);

        ESIndexRequest indexRequest = new ESIndexRequest();
        indexRequest.index(indexName);
        indexRequest.type(request.param("type") == null ? "_doc" : request.param("type"));
        indexRequest.id(request.param("id"));
        indexRequest.routing(request.param("routing"));
        indexRequest.parent(request.param("parent")); // order is important, set it after routing, so it will set the routing
        indexRequest.setPipeline(request.param("pipeline"));

        indexRequest.source(JSON.toJSONString(source));
        indexRequest.timeout(request.paramAsTime("timeout", IndexRequest.DEFAULT_TIMEOUT));

        indexRequest.version(RestActions.parseVersion(request));
        indexRequest.versionType(VersionType.fromString(request.param("version_type"), indexRequest.versionType()));
        String sOpType = request.param("op_type");
        if (sOpType != null) {
            indexRequest.opType(IndexRequest.OpType.fromString(sOpType));
        }

        indexRequest.setConsistencyLevel(request.param("consistency"));
        indexRequest.setWaitForActiveShards(request.param("wait_for_active_shards"));
        indexRequest.setRefresh(request.param("refresh"));

        indexRequest.putHeader("requestId", queryContext.getRequestId());
        indexRequest.putHeader("Authorization", request.getHeader("Authorization"));

        // 获取写入的client
        ESClient writeClient = esClusterService.getWriteClient(indexTemplate);

        if (logger.isDebugEnabled()) {
            logger.debug("rest index data:index={}, type={}, id={}", indexRequest.index(), indexRequest.type(), indexRequest.id());
        }

        LogGather.recordInfoLog( indexRequest.index() + "_" + OPER_INDEX, String.format("%s write index, type=%s, id=%s", indexRequest.index(), indexRequest.type(), indexRequest.id()));

        // 生成listener
        ActionListener<ESIndexResponse> listener = new RestActionListenerImpl<ESIndexResponse>(queryContext) {
            @Override
            public void onResponse(ESIndexResponse response) {
                long currentTime = System.currentTimeMillis();

                if (statLogger.isDebugEnabled()) {
                    statLogger.debug(QueryConsts.DLFLAG_PREFIX + "index_es_response||requestId={}||cost={}", queryContext.getRequestId(), currentTime - queryContext.getRequestTime());
                }

                metricsService.addIndexMetrics(indexTemplate.getExpression(), name(), currentTime - queryContext.getRequestTime(), queryContext.getPostBody().length(), 0);

                super.onResponse(response);
            }
        };

        // 异步写入
        writeClient.index(indexRequest, listener);
    }
}
