package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;

/**
 * @author d06679
 * @date 2019/3/18
 */
public class EcmRemoteException extends ThirdPartRemoteException {

    public EcmRemoteException(String message) {
        super(message, ResultType.ECM_SERVER_ERROR);
    }

}
