package com.didiglobal.logi.op.manager.infrastructure.storage;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.exception.FileStorageException;
import com.didiglobal.logi.op.manager.infrastructure.storage.hander.FileStorageFactory;
import com.didiglobal.logi.op.manager.infrastructure.storage.hander.FileStorageHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author didi
 * @date 2022-07-05 10:20 上午
 */
@Component
public class StorageServiceImpl implements StorageService {

    @Autowired
    private FileStorageFactory fileStorageFactory;

    @Value("${file.storage.type:s3}")
    private String type;

    @Override
    public Result<String> upload(String fileName, MultipartFile uploadFile) {
        try {
            FileStorageHandle handle = fileStorageFactory.getHandlerByType(type);
            String url = handle.upload(fileName, uploadFile);
            return Result.success(url);
        } catch (FileStorageException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    @Override
    public Result<String> remove(String fileName) {
        try {
            FileStorageHandle handle = fileStorageFactory.getHandlerByType(type);
            handle.remove(fileName);
            return Result.success();
        } catch (FileStorageException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

}
