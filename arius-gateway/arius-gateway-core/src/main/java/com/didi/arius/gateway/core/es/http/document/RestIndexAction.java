package com.didi.arius.gateway.core.es.http.document;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.exception.InvalidParameterException;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESIndexRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESIndexResponse;
import com.didiglobal.knowframework.log.LogGather;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.replication.ReplicationRequest;
import org.elasticsearch.common.Strings;
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

import static com.didi.arius.gateway.elasticsearch.client.utils.LogUtils.setWriteLog;

@Component("restIndexAction")
public class RestIndexAction extends RestBaseWriteAction {
    private static final String CREATE_NAME = "_create";

    @Override
    public String name() {
        return "index";
    }


    @Override
    public void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel)
            throws Exception {
        if (request.param(CREATE_NAME) != null) {
            if (request.param(CREATE_NAME).equals(CREATE_NAME)) {
                request.params().put("op_type", "create");
            } else {
                throw new IllegalArgumentException("Can't handle [" + request.method() + "] for path [" + request.rawPath() + "]");
            }
        }

        if (!request.hasContent()) {
            throw  new InvalidParameterException("no source to write");
        }

        String index = request.param(name());
        IndexTemplate indexTemplate = getAndCheckIndexTemplate(index, queryContext);

        String strSource;
        if (XContentType.JSON != XContentFactory.xContentType(request.content())) {
            strSource = XContentHelper.convertToJson(request.content(), false);
        } else {
            strSource = request.content().toUtf8();
        }

        Map<String, Object> source = JSON.parseObject(strSource, HashMap.class);

        if (!indexTemplate.isInternal()) {
            source.put(WRITE_TIME_FIELD, System.currentTimeMillis());
        }
        ESIndexRequest indexRequest = new ESIndexRequest();
        indexRequest.index(index);
        indexRequest.type(request.param("type") == null ? "_doc" : request.param("type"));
        indexRequest.id(request.param("id"));
        indexRequest.routing(request.param("routing"));
        indexRequest.parent(request.param("parent")); // order is important, set it after routing, so it will set the routing
        if (!Strings.isEmpty(indexTemplate.getIngestPipeline())) {
            indexRequest.setPipeline(indexTemplate.getIngestPipeline());
        }
        indexRequest.source(JSON.toJSONString(source));
        indexRequest.timeout(request.paramAsTime("timeout", ReplicationRequest.DEFAULT_TIMEOUT));

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
        ESClient writeClient = esClusterService.getWriteClient(indexTemplate, actionName);

        if (logger.isDebugEnabled()) {
            logger.debug("rest index data:index={}, type={}, id={}", indexRequest.index(), indexRequest.type(), indexRequest.id());
        }

        LogGather.recordInfoLog( indexRequest.index() + "_" + OPER_INDEX, String.format("%s write index, type=%s, id=%s", indexRequest.index(), indexRequest.type(), indexRequest.id()));

        // 生成listener
        ActionListener<ESIndexResponse> listener = new RestActionListenerImpl<ESIndexResponse>(queryContext) {
            @Override
            public void onResponse(ESIndexResponse response) {
                long currentTime = System.currentTimeMillis();

                setWriteLog(queryContext, indexTemplate, response,
                        currentTime, queryConfig.isWriteLogContentOpen());

                metricsService.addIndexMetrics(indexTemplate.getExpression(), name(), currentTime - queryContext.getRequestTime(), queryContext.getPostBody().length(), 0);

                super.onResponse(response);
            }
        };

        // 异步写入
        writeClient.index(indexRequest, listener);
    }
}
