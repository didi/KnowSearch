package com.didi.cloud.fastdump.common.exception;

import com.didi.cloud.fastdump.common.content.ResultType;

/**
 * Created by linyunan on 2022/8/29
 */
public class NotFindESClientException extends BaseException{
    public NotFindESClientException(String message) {
        super(message, ResultType.NOT_FIND_ES_CLIENT);
    }
}
