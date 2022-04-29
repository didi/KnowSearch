package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 * ES错误基类
 *
 * @author d06679
 * @date 2019/3/13
 */
public class ESRunTimeException extends RuntimeException {

    private ResultType resultType;

    public ESRunTimeException(String message, Throwable cause, ResultType resultType) {
        super(message, cause);
        this.resultType = resultType;
    }

    public ESRunTimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ESRunTimeException(String message, ResultType resultType) {
        super(message);
        this.resultType = resultType;
    }

    public ESRunTimeException(String message) {
        super(message);
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }
}
