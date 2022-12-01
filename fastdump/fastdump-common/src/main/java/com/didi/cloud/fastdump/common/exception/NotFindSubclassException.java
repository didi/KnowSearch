package com.didi.cloud.fastdump.common.exception;

import com.didi.cloud.fastdump.common.content.ResultType;

/**
 * @author linyunan
 * @date 2021-04-25
 */
public class NotFindSubclassException extends BaseException {

    public NotFindSubclassException(String message) {
        super(message, ResultType.NOT_FIND_SUB_CLASS);
    }
}
