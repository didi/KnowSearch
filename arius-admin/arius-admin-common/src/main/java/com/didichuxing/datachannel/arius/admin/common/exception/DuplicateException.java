package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 * 已存在异常
 * @author d06679
 */
public class DuplicateException extends BaseException {

    public DuplicateException(String message) {
        super(message, ResultType.DUPLICATION);
    }

}
