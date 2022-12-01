package com.didi.cloud.fastdump.common.exception;

import com.didi.cloud.fastdump.common.content.ResultType;

public class FastDumpTransportException extends BaseException {

    public FastDumpTransportException(String message) {
        super(message, ResultType.TRANSPORT_ERROR);
    }

    public FastDumpTransportException(String message, Throwable cause) {
        super(message, cause, ResultType.TRANSPORT_ERROR);
    }

    public FastDumpTransportException(String message, ResultType resultType) {
        super(message, resultType);
    }

    public FastDumpTransportException(String message, Throwable cause, ResultType resultType) {
        super(message, cause, resultType);
    }

}
