package com.didi.cloud.fastdump.common.exception;

import com.didi.cloud.fastdump.common.content.ResultType;

public class NotExistFastDumpException extends BaseException {

    public NotExistFastDumpException(String message) {
        super(message, ResultType.FAST_DUMP_NOT_EXIST);
    }

    public NotExistFastDumpException(String message, Throwable cause) {
        super(message, cause, ResultType.FAST_DUMP_NOT_EXIST);
    }

    public NotExistFastDumpException(String message, ResultType resultType) {
        super(message, resultType);
    }

    public NotExistFastDumpException(String message, Throwable cause, ResultType resultType) {
        super(message, cause, resultType);
    }

}
