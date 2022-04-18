package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 *
 *
 * @author d06679
 * @date 2019/2/21
 */
public class AdminTaskException extends AriusRunTimeException {

    public AdminTaskException(String message) {
        super(message, ResultType.ADMIN_TASK_ERROR);
    }

}
