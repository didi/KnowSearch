package com.didi.cloud.fastdump.common.exception;


import com.didi.cloud.fastdump.common.content.ResultType;

public class BaseException extends Exception {

    private ResultType resultType;

    public BaseException(String message, Throwable cause, ResultType resultType) {
        super(message, cause);
        this.resultType = resultType;
    }

    public BaseException(String message, ResultType resultType) {
        super(message);
        this.resultType = resultType;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }
}
