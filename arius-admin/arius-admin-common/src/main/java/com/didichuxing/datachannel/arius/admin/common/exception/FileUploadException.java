package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 * @author d06679
 * @date 2019/3/18
 */
public class FileUploadException extends ThirdPartRemoteException {

    public FileUploadException(String message) {
        super(message, ResultType.FILE_UPLOAD_ERROR);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause, ResultType.FILE_UPLOAD_ERROR);
    }

}
