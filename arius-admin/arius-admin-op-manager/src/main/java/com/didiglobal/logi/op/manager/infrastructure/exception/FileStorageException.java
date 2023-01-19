package com.didiglobal.logi.op.manager.infrastructure.exception;

import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;

/**
 * @author didi
 * @date 2022-07-05 2:43 下午
 */
public class FileStorageException extends BaseException {

    public FileStorageException(String message) {
        super(ResultCode.FILE_OPERATE_ERROR, message);
    }

    public FileStorageException() {
        super(ResultCode.FILE_OPERATE_ERROR);
    }

    public FileStorageException(Exception e) {
        super(ResultCode.FILE_OPERATE_ERROR, e);
    }
}
