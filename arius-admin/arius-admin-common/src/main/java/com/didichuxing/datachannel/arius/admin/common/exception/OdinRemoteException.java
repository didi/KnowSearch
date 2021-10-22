package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;

/**
 * @author d06679
 * @date 2019/3/18
 */
public class OdinRemoteException extends ThirdPartRemoteException {

    public OdinRemoteException(String message) {
        super(message, ResultType.ODIN_SERVER_ERROR);
    }

}
