package com.didi.arius.gateway.elasticsearch.client.utils;

import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.JoinLogContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;

/**
 * @author didi
 * @date 2021-09-26 3:31 下午
 */
public class LogUtils {

    public static final String KIBANA_LOG = "_arius_query or _arius_write is from kibana";

    public static void setWriteLog(QueryContext queryContext, IndexTemplate indexTemplate,
                                     ESActionResponse response, long currentTime, boolean contentOpen) {
        JoinLogContext joinLogContext = queryContext.getJoinLogContext();
        joinLogContext.setClientNode(response.getHost().getHostName() + ":" + response.getHost().getPort());
        joinLogContext.setTotalCost(currentTime - queryContext.getRequestTime());
        joinLogContext.setInternalCost( joinLogContext.getTotalCost() - joinLogContext.getEsCost());
        joinLogContext.setSinkTime(System.currentTimeMillis());
        joinLogContext.setQueryRequest(false);
        joinLogContext.setDestTemplateName(null != indexTemplate ? indexTemplate.getName() : "none");
        if (!contentOpen) {
            joinLogContext.setDsl(null);
        }
    }
}
