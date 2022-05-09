package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.AriusRunTimeException;

/**
 * @author d06679
 * @date 2019-06-25
 */
public class ResourceNotEnoughException extends AriusRunTimeException {
    public ResourceNotEnoughException(String message) {
        super(message, ResultType.RESOURCE_NOT_ENOUGH);
    }
}
