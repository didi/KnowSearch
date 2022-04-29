package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 * @author d06679
 * @date 2019/3/18
 */
public class AmsRemoteException extends ThirdPartRemoteException {

    public AmsRemoteException(String message) {
        super(message, ResultType.AMS_SERVER_ERROR);
    }

}
