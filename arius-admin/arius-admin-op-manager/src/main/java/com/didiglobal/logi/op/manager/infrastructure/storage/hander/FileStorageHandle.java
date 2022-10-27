package com.didiglobal.logi.op.manager.infrastructure.storage.hander;

import com.didiglobal.logi.op.manager.infrastructure.exception.FileStorageException;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author didi
 * @date 2022-07-05 10:20 上午
 */
public interface FileStorageHandle {

    /**
     * 上传文件
     * @param fileName
     * @param uploadFile
     * @return
     * @throws FileStorageException
     */
    String upload(String fileName, MultipartFile uploadFile) throws FileStorageException;

    /**
     * 下载文件
     * @param fileName
     * @return
     * @throws FileStorageException
     */
    MultipartFile download(String fileName) throws FileStorageException;

    /**
     * 删除文件
     * @param fileName
     * @throws FileStorageException
     */
    void remove(String fileName) throws FileStorageException;
}
