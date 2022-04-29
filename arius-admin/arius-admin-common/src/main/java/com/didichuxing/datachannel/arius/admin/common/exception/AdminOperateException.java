package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 *
 *
 * @author d06679
 * @date 2019/2/21
 */
public class AdminOperateException extends BaseException {

    public AdminOperateException(String message) {
        super(message, ResultType.ADMIN_OPERATE_ERROR);
    }

    public AdminOperateException(String message, Throwable cause) {
        super(message, cause, ResultType.ADMIN_OPERATE_ERROR);
    }

    public AdminOperateException(String message, ResultType resultType) {
        super(message, resultType);
    }

    public AdminOperateException(String message, Throwable cause, ResultType resultType) {
        super(message, cause, resultType);
    }

}
