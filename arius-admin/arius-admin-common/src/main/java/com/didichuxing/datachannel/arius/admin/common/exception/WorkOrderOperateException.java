package com.didichuxing.datachannel.arius.admin.common.exception;

/**
 *
 *
 * @author d06679
 * @date 2019/2/21
 */
public class WorkOrderOperateException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public WorkOrderOperateException(String message) {
        super(message);
    }
}
