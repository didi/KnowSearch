package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

public class EventException extends BaseException{

    public EventException(String message) {
        super(message, ResultType.EVENT_ERROR);
    }

    public EventException(String message, Throwable cause) {
        super(message, cause, ResultType.EVENT_ERROR);
    }

    public EventException(String message, ResultType resultType) {
        super(message, resultType);
    }

    public EventException(String message, Throwable cause, ResultType resultType) {
        super(message, cause, resultType);
    }

}
