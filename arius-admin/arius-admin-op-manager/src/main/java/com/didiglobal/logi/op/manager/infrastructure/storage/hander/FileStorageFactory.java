package com.didiglobal.logi.op.manager.infrastructure.storage.hander;

import com.didiglobal.logi.op.manager.infrastructure.common.enums.FileStorageTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author didi
 * @date 2022-07-06 1:57 下午
 */
@Component
public class FileStorageFactory {
    @Autowired
    private LocalFileStorageHandle localFileStorageHandle;

    @Autowired
    private S3FileStorageHandle s3FileStorageHandle;

    public FileStorageHandle getHandlerByType(String type) {
        FileStorageTypeEnum fileStorageTypeEnum = FileStorageTypeEnum.valueOfType(type);
        switch (fileStorageTypeEnum) {
            case LOCAL:
                return localFileStorageHandle;
            case S3:
            default:
                return s3FileStorageHandle;
        }
    }

}
