package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;

/**
 * @author d06679
 * @date 2019/3/18
 */
public class N9eRemoteException extends ThirdPartRemoteException {

    public N9eRemoteException(String message) {
        super(message, ResultType.N9E_SERVER_ERROR);
    }

}
