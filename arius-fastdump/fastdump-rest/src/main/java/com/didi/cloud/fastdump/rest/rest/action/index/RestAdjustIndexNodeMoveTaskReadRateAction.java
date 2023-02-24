package com.didi.cloud.fastdump.rest.rest.action.index;

import static org.elasticsearch.rest.RestRequest.Method.PUT;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didi.cloud.fastdump.common.bean.common.Result;
import com.didi.cloud.fastdump.common.bean.readrate.ReadFileRateInfo;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.content.metadata.QueryContext;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.core.action.metadata.AdjustIndexNodeMoveTaskReadRateAction;
import com.didi.cloud.fastdump.rest.rest.BaseCommonHttpAction;

/**
 * Created by linyunan on 2022/8/24
 */
@Component
public class RestAdjustIndexNodeMoveTaskReadRateAction extends BaseCommonHttpAction {
    @Autowired
    private AdjustIndexNodeMoveTaskReadRateAction adjustIndexNodeMoveTaskReadRateAction;
    @Override
    protected void register() {
        restHandlerFactory.registerHandler(PUT, "/index-node-move/adjust-readRate", this);
    }

    @Override
    protected String name() { return "index-move-node-adjust-readRate";}

    @Override
    protected void handleRequest(QueryContext queryContext, RestChannel channel) throws Exception {
        ReadFileRateInfo readFileRateInfo = JSON.parseObject(queryContext.getPostBody(), ReadFileRateInfo.class);

        if (null == readFileRateInfo) {
            throw new BaseException("readFileRateInfo is null", ResultType.ILLEGAL_PARAMS);
        }

        if (StringUtils.isBlank(readFileRateInfo.getTaskId())) {
            throw new BaseException(String.format("taskId[%s] is illegal", readFileRateInfo.getTaskId()), ResultType.ILLEGAL_PARAMS);
        }

        if (null == readFileRateInfo.getReadFileRateLimit()) {
            throw new BaseException(String.format("taskId[%s] readRate[%s] is illegal", readFileRateInfo.getTaskId()
                    , readFileRateInfo), ResultType.ILLEGAL_PARAMS);
        }

        adjustIndexNodeMoveTaskReadRateAction.doAction(readFileRateInfo);
        sendResponse(channel, Result.buildSucc());
    }
}
