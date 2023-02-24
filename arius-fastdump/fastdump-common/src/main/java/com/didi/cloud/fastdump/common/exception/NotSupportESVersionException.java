package com.didi.cloud.fastdump.common.exception;

import com.didi.cloud.fastdump.common.content.ResultType;

/**
 * Created by linyunan on 2022/8/24
 */
public class NotSupportESVersionException extends BaseException{
    public NotSupportESVersionException(String message) {
        super(message, ResultType.NOT_SUPPORT_ES_VERSION);
    }
}
