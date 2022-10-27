package com.didiglobal.logi.op.manager.infrastructure.exception;

import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;

/**
 * @author didi
 * @date 2022-07-05 2:43 下午
 */
public class ComponentHandlerException extends BaseException {

    public ComponentHandlerException(String message) {
        super(ResultCode.HANDLER_OPERATE_ERROR, message);
    }

    public ComponentHandlerException() {
        super(ResultCode.HANDLER_OPERATE_ERROR);
    }

    public ComponentHandlerException(Exception e) {
        super(ResultCode.HANDLER_OPERATE_ERROR, e);
    }
}
