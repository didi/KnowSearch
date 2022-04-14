package com.didichuxing.datachannel.arius.admin.core.service.extend.storage.impl;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.remote.storage.FileStorageHandle;

/**
 * 默认没有对接文件存储系统
 * @author linyunan
 * @date 2021-05-19
 */
@Component
public class DefaultFileStorageHandle implements FileStorageHandle {

    private static final String MSG = "仅商业版支持";
    @Override
    public Result<String> upload(String fileName, String fileMd5, MultipartFile uploadFile) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public Result<MultipartFile> download(String fileName) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public Result<Void> remove(String fileName) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public String getDownloadBaseUrl() {
        throw new UnsupportedOperationException(MSG);
    }
}
