package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 * Arius gateway 异常
 *
 * @author d06679
 * @date 2019/3/13
 */
public class AriusGatewayException extends AriusRunTimeException {

    public AriusGatewayException(String message) {
        super(message, ResultType.ARIUS_GATEWAY_ERROR);
    }
}
