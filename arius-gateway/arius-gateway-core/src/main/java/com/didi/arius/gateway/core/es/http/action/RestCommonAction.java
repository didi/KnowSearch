package com.didi.arius.gateway.core.es.http.action;

import java.util.List;

import com.didi.arius.gateway.common.exception.AccessForbiddenException;
import com.didi.arius.gateway.common.exception.InvalidParameterException;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.utils.AppUtil;
import com.didi.arius.gateway.core.es.http.HttpRestHandler;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.google.common.collect.Lists;
import org.elasticsearch.common.Strings;
import org.springframework.stereotype.Component;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

@Component("restCommonAction")
public class RestCommonAction extends HttpRestHandler {

    @Override
    public String name() {
        return "common";
    }

    @Override
    public void handleRequest(QueryContext queryContext) throws Exception {
        if (isOriginCluster(queryContext)) {
            handleOriginClusterRequest(queryContext);
        } else {
            handleInterRequest(queryContext);
        }
    }

    public void handleInterRequest(QueryContext queryContext) throws Exception {
        String uri = queryContext.getUri();
        String[] uriUnit = Strings.splitStringToArray(uri, '/');
        if (uriUnit.length <= 0) {
            throw new InvalidParameterException("uri(" + uri + ") error");
        }

        ESClient client = esClusterService.getClient(queryContext, actionName);

        if (queryContext.getRequest().param("index") != null) {
            String index = queryContext.getRequest().param("index");
            String[] indicesArr = Strings.splitStringByCommaToArray(index);
            List<String> indices = Lists.newArrayList(indicesArr);
            queryContext.setIndices(indices);

            checkIndices(queryContext);

            if (isIndexType(queryContext)) {
                IndexTemplate indexTemplate = getTemplateByIndexTire(indices, queryContext);

                client = esClusterService.getClient(queryContext, indexTemplate, actionName);
            }
        } else if (!AppUtil.isAdminAppid(queryContext.getAppDetail())
                && !uri.startsWith("/.")
                && !queryContext.isNewKibana()) {
            throw new AccessForbiddenException("action(" + queryContext.getUri() + ") forbidden");
        }

        directRequest(client, queryContext);
    }
}
