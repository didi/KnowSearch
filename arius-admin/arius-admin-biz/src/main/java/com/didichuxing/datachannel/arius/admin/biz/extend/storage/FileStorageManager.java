package com.didichuxing.datachannel.arius.admin.biz.extend.storage;

import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.core.service.extend.storage.FileStorageService;
import com.didichuxing.datachannel.arius.admin.remote.storage.content.FileStorageTypeEnum;

/**
 * @author linyunan
 * @date 2021-05-19
 */
public class FileStorageManager {
    @Autowired
    private FileStorageService               fileStorageService;

    /**
     * 兼容外部不同的文件存储系统
     * @see FileStorageTypeEnum
     */
    private static final FileStorageTypeEnum DEPARTMENT_TYPE = null;

    public Result getDownloadBaseUrl() {
        return fileStorageService.getDownloadBaseUrl(DEPARTMENT_TYPE);
    }
}
