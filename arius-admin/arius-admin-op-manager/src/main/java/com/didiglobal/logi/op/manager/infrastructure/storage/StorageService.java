package com.didiglobal.logi.op.manager.infrastructure.storage;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author didi
 * @date 2022-07-05 10:20 上午
 */
public interface StorageService {

    /**
     * 上传文件
     * @param fileName
     * @param uploadFile
     * @return
     */
    Result<String> upload(String fileName, MultipartFile uploadFile);
    /**
     * 删除文件
     * @param fileName
     * @return
     */
    Result<String> remove(String fileName);
}
