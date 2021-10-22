package com.didi.arius.gateway.rest.controller.es.admin.indices;

import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.rest.controller.AdminController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.google.common.collect.Lists;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.springframework.stereotype.Controller;

import java.util.List;

import static com.didi.arius.gateway.common.utils.CommonUtil.isIndexType;

/**
 * @author fitz
 * @date 2021/5/26 1:36 下午
 */
@Controller
public class RestDeleteIndexController extends AdminController {

    public static final String NAME = "restDeleteIndex";

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.DELETE, "/{index}", this);
    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {

        String index = request.param("index");
        if (Strings.hasText(index) == false) {
            throw new IllegalArgumentException("index must not be null");
        }

        String[] indicesArr = Strings.splitStringByCommaToArray(index);
        List<String> indices = Lists.newArrayList(indicesArr);
        queryContext.setIndices(indices);
        checkIndices(queryContext);
        if (isIndexType(queryContext)) {
            IndexTemplate indexTemplate = indexTemplateService.getTemplateByIndexTire(indices, queryContext);
            client = esClusterService.getClient(queryContext, indexTemplate);
        }

        directRequest(client, queryContext);

    }
}
