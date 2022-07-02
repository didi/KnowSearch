package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 * @author d06679
 * @date 2019/3/18
 */
public class ThirdPartRemoteException extends AdminOperateException {

    public ThirdPartRemoteException(String message, Throwable cause, ResultType resultType) {
        super(message, cause, resultType);
    }

    public ThirdPartRemoteException(String message, ResultType resultType) {
        super(message, resultType);
    }

}
