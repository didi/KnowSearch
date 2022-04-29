package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 * 平台错误基类
 *
 * @author d06679
 * @date 2019/3/13
 */
public class AriusRunTimeException extends RuntimeException {

    private ResultType resultType;

    public AriusRunTimeException(String message, Throwable cause, ResultType resultType) {
        super(message, cause);
        this.resultType = resultType;
    }

    public AriusRunTimeException(String message, ResultType resultType) {
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
