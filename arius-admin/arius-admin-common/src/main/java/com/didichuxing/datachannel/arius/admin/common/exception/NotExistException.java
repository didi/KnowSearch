package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 * 不存在异常
 *
 * @author d06679
 */
public class NotExistException extends BaseException {
    public NotExistException(String message) {
        super(message, ResultType.NOT_EXIST);
    }
}
