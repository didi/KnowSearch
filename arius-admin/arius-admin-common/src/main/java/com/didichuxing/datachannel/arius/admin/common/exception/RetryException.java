package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;

public class RetryException extends AriusRunTimeException {

    public RetryException(String message, Throwable cause) {
        super(message, cause, ResultType.ES_OPERATE_ERROR);
    }

    public RetryException(String message) {
        super(message, ResultType.ES_OPERATE_ERROR);
    }
}
