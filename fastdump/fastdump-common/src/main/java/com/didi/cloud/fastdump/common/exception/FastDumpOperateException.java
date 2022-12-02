package com.didi.cloud.fastdump.common.exception;

import com.didi.cloud.fastdump.common.content.ResultType;

public class FastDumpOperateException extends BaseException {

    public FastDumpOperateException(String message) {
        super(message, ResultType.FAIL);
    }

    public FastDumpOperateException(String message, Throwable cause) {
        super(message, cause, ResultType.FAIL);
    }

    public FastDumpOperateException(String message, ResultType resultType) {
        super(message, resultType);
    }

    public FastDumpOperateException(String message, Throwable cause, ResultType resultType) {
        super(message, cause, resultType);
    }

}
