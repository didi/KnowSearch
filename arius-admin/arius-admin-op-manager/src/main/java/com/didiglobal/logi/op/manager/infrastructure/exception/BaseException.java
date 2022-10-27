package com.didiglobal.logi.op.manager.infrastructure.exception;

import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import lombok.Data;

/**
 * @author didi
 * @date 2022-07-05 3:17 下午
 */
@Data
public class BaseException extends Exception {

    private Integer code = ResultCode.UNKNOW.getCode();

    private String message = "unKnow Exception";

    public BaseException(ResultCode code, String message) {
        this.code = code.getCode();
        this.message = message;
    }

    public BaseException(ResultCode code, Exception e) {
        super(e);
        this.code = code.getCode();
        this.message = e.getMessage();
    }

    public BaseException(ResultCode code) {
        this.code = code.getCode();
        this.message = code.getMessage();
    }
}
