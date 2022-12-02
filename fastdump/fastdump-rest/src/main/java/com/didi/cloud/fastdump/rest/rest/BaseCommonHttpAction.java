package com.didi.cloud.fastdump.rest.rest;

import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestStatus;

import com.alibaba.fastjson.JSON;
import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.content.metadata.QueryContext;

/**
 * Created by linyunan on 2022/8/4
 */
public abstract class BaseCommonHttpAction extends BaseRestHttpAction {
    @Override
    protected void handleRequest(QueryContext queryContext, RestChannel channel) throws Exception {

    }
    protected void sendResponse(RestChannel channel, Result dataType){
        channel.sendResponse(new BytesRestResponse(RestStatus.OK, JSON.toJSONString(dataType)));
    }
}
