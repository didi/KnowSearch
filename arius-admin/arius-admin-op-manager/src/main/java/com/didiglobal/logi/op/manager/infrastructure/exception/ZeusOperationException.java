package com.didiglobal.logi.op.manager.infrastructure.exception;

import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;

/**
 * @author didi
 * @date 2022-07-09 8:44 上午
 */
public class ZeusOperationException extends BaseException {

    public ZeusOperationException(String message) {
        super(ResultCode.ZEUS_OPERATE_ERROR, message);
    }

    public ZeusOperationException() {
        super(ResultCode.ZEUS_OPERATE_ERROR);
    }

    public ZeusOperationException(Exception e) {
        super(ResultCode.ZEUS_OPERATE_ERROR, e);
    }
}
