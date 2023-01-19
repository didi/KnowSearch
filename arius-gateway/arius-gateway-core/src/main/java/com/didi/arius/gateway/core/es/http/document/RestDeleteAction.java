package com.didi.arius.gateway.core.es.http.document;

import static com.didi.arius.gateway.elasticsearch.client.utils.LogUtils.setWriteLog;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.replication.ReplicationRequest;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.support.RestActions;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.exception.AccessForbiddenException;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.AppUtil;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESDeleteRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESDeleteResponse;
import com.didiglobal.knowframework.log.LogGather;

@Component("restDeleteAction")
public class RestDeleteAction extends RestBaseWriteAction {

    @Override
    public String name() {
        return "delete";
    }


    @Override
    public void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel)
            throws Exception {
        String index = request.param("index");
        IndexTemplate indexTemplate = getAndCheckIndexTemplate(index, queryContext);

        String indexName = index;
        // delete 在索引不按时间分割的时候，可以支持不传递source
        if (StringUtils.isBlank(indexTemplate.getDateFormat())) {
            String indexExpression = indexTemplate.getExpression();
            if (indexExpression.endsWith("*")) {
                indexExpression = indexExpression.substring(0, indexExpression.length()-1);
            }
            indexName = getIndexVersionName(indexExpression.toLowerCase(), indexTemplate.getVersion());
        } else {
            if(request.hasContent()) {
                String strSource;
                if (XContentType.JSON != XContentFactory.xContentType(request.content())) {
                    strSource = XContentHelper.convertToJson(request.content(), false);
                } else {
                    strSource = request.content().toUtf8();
                }
                Map<String, Object> source = JSON.parseObject(strSource, HashMap.class);
                indexName = getIndexName(indexTemplate, source);
            }
        }

        if (indexName.startsWith(".") && !AppUtil.isAdminAppid(queryContext.getAppDetail())) {
            throw new AccessForbiddenException("action(" + queryContext.getUri() + ") forbidden");
        }

        ESDeleteRequest deleteRequest = new ESDeleteRequest();
        deleteRequest.index(indexName);
        deleteRequest.type(request.param("type") == null ? "_doc" : request.param("type"));
        deleteRequest.id(request.param("id"));
        deleteRequest.routing(request.param("routing"));
        deleteRequest.parent(request.param("parent")); // order is important, set it after routing, so it will set the routing
        deleteRequest.timeout(request.paramAsTime("timeout", ReplicationRequest.DEFAULT_TIMEOUT));
        deleteRequest.setRefresh(request.param("refresh"));
        deleteRequest.version(RestActions.parseVersion(request));
        deleteRequest.versionType(VersionType.fromString(request.param("version_type"), deleteRequest.versionType()));
        deleteRequest.setConsistencyLevel(request.param("consistency"));
        deleteRequest.setWaitForActiveShards(request.param("wait_for_active_shards"));

        deleteRequest.putHeader("requestId", queryContext.getRequestId());
        deleteRequest.putHeader("Authorization", request.getHeader("Authorization"));

        ESClient writeClient = esClusterService.getWriteClient(indexTemplate, actionName);

        if (logger.isDebugEnabled()) {
            logger.debug("rest delete data:index={}, type={}, id={}", deleteRequest.index(), deleteRequest.type(), deleteRequest.id());
        }

        LogGather.recordInfoLog( deleteRequest.index() + "_" + OPER_DELETE, String.format("%s delete index, type=%s, id=%s", deleteRequest.index(), deleteRequest.type(), deleteRequest.id()));

        ActionListener<ESDeleteResponse> listener = new RestActionListenerImpl<ESDeleteResponse>(queryContext) {
            @Override
            public void onResponse(ESDeleteResponse response) {
                long currentTime = System.currentTimeMillis();

                setWriteLog(queryContext, indexTemplate,
                        response, currentTime, queryConfig.isWriteLogContentOpen());

                metricsService.addIndexMetrics(indexTemplate.getExpression(), name(), currentTime - queryContext.getRequestTime(), queryContext.getPostBody().length(), 0);

                super.onResponse(response);
            }


        };
        writeClient.delete(deleteRequest, listener);
    }
}
