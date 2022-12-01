package com.didi.cloud.fastdump.common.exception;

import com.didi.cloud.fastdump.common.content.ResultType;

/**
 *
 *
 * @author d06679
 * @date 2019/2/21
 */
public class ESOperateException extends BaseException {

    public ESOperateException(String message, Throwable cause) {
        super(message, cause, ResultType.ES_OPERATE_ERROR);
    }

    public ESOperateException(String message) {
        super(message, ResultType.ES_OPERATE_ERROR);
    }

}
