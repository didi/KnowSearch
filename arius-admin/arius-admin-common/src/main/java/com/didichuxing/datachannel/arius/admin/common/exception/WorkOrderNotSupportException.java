package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 *
 *
 * @author d06679
 * @date 2019/2/21
 */
public class WorkOrderNotSupportException extends AriusRunTimeException {

    public WorkOrderNotSupportException(String message) {
        super(message, ResultType.WORK_ORDER_NOT_SUPPORT_ERROR);
    }

}
