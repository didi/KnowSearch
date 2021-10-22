package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;

/**
 *
 *
 * @author d06679
 * @date 2019/2/21
 */
public class ESOperateException extends AdminOperateException {

    public ESOperateException(String message, Throwable cause) {
        super(message, cause, ResultType.ES_OPERATE_ERROR);
    }

    public ESOperateException(String message) {
        super(message, ResultType.ES_OPERATE_ERROR);
    }

}
