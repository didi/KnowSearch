package com.didi.arius.gateway.core.es.http.document;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.exception.InvalidParameterException;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESUpdateRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESUpdateResponse;
import com.didichuxing.tunnel.util.log.LogGather;
import org.elasticsearch.action.ActionListener;
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

@Component("restUpdateAction")
public class RestUpdateAction extends RestBaseWriteAction {
    public static final String NAME = "update";

    @Override
    public String name() {
        return NAME;
    }


    @Override
    public void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel)
            throws Exception {
        if (request.hasContent() == false) {
            throw  new InvalidParameterException("no source to update");
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
        Map<String, Object> doc = (Map<String, Object>) source.get("doc");
        if (doc == null && indexTemplate.getDeployStatus() == IndexTemplate.DeployStatus.MASTER_AND_SLAVE) {
            throw new InvalidParameterException("kakfa update only support doc update");
        }

        if (doc != null) {
            if (false == indexTemplate.isInternal()) {
                doc.put(WRITE_TIME_FIELD, System.currentTimeMillis());
            }
        }

        String indexName = getIndexName(indexTemplate, doc);

        ESUpdateRequest updateRequest = new ESUpdateRequest();
        updateRequest.index(indexName);
        updateRequest.type(request.param("type"));
        updateRequest.id(request.param("id"));
        updateRequest.routing(request.param("routing"));
        updateRequest.parent(request.param("parent"));
        updateRequest.timeout(request.paramAsTime("timeout", updateRequest.timeout()));
        updateRequest.setRefresh(request.param("refresh"));
        updateRequest.setConsistencyLevel(request.param("consistency"));
        updateRequest.setWaitForActiveShards(request.param("wait_for_active_shards"));

        updateRequest.docAsUpsert(request.paramAsBoolean("doc_as_upsert", updateRequest.docAsUpsert()));

        updateRequest.retryOnConflict(request.paramAsInt("retry_on_conflict", updateRequest.retryOnConflict()));
        updateRequest.version(RestActions.parseVersion(request));
        updateRequest.versionType(VersionType.fromString(request.param("version_type"), updateRequest.versionType()));

        updateRequest.source(JSON.toJSONString(source));

        updateRequest.putHeader("requestId", queryContext.getRequestId());
        updateRequest.putHeader("Authorization", request.getHeader("Authorization"));

        ESClient writeClient = esClusterService.getWriteClient(indexTemplate);

        LogGather.recordInfoLog( updateRequest.index() + "_" + OPER_UPDATE, String.format("%s update index, type=%s, id=%s", updateRequest.index(), updateRequest.type(), updateRequest.id()));

        ActionListener<ESUpdateResponse> listener = new RestActionListenerImpl<ESUpdateResponse>(queryContext) {
            @Override
            public void onResponse(ESUpdateResponse response) {
                long currentTime = System.currentTimeMillis();

                if (statLogger.isDebugEnabled()) {
                    statLogger.debug(QueryConsts.DLFLAG_PREFIX + "update_es_response||requestId={}||cost={}", queryContext.getRequestId(), currentTime - queryContext.getRequestTime());
                }

                metricsService.addIndexMetrics(indexTemplate.getExpression(), name(), currentTime - queryContext.getRequestTime(), queryContext.getPostBody().length(), 0);

                super.onResponse(response);
            }
        };
        writeClient.update(updateRequest, listener);
    }
}
