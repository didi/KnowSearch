package com.didichuxing.datachannel.arius.admin.biz.extend.storage;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.core.service.extend.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by linyunan on 2021-07-26
 */
@Component
public class FileStorageManagerImpl implements FileStorageManager {

    @Autowired
    private FileStorageService               fileStorageService;

    @Override
    public Result getDownloadBaseUrl() {
        return fileStorageService.getDownloadBaseUrl();
    }
}
