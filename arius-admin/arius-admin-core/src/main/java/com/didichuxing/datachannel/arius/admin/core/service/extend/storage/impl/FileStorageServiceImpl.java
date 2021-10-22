package com.didichuxing.datachannel.arius.admin.core.service.extend.storage.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.extend.storage.FileStorageService;
import com.didichuxing.datachannel.arius.admin.remote.storage.FileStorageHandle;
import com.didichuxing.datachannel.arius.admin.remote.storage.content.FileStorageTypeEnum;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 * @author linyunan
 * @date 2021-05-19
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final ILog LOGGER = LogFactory.getLog(FileStorageServiceImpl.class);

    @Autowired
    private HandleFactory     handleFactory;

    @Override
    public Result<String> upload(String fileName, String fileMd5, MultipartFile uploadFile,
                                 FileStorageTypeEnum typeEnum) {

        Result<String> getFileStorageTypeResult = getFileStorageType(typeEnum);
        if (getFileStorageTypeResult.failed()) {
            return getFileStorageTypeResult;
        }

        String fileStorageType = getFileStorageTypeResult.getData();

        LOGGER.info("class=FileStorageServiceImpl||method=upload||fileStorageType={}", fileStorageType);

        FileStorageHandle fileStorageHandle = (FileStorageHandle) handleFactory.getByHandlerNamePer(fileStorageType);

        return fileStorageHandle.upload(fileName, fileMd5, uploadFile);
    }

    @Override
    public Result download(String fileName, String fileMd5, FileStorageTypeEnum typeEnum) {

        Result<String> getFileStorageTypeResult = getFileStorageType(typeEnum);
        if (getFileStorageTypeResult.failed()) {
            return getFileStorageTypeResult;
        }

        String fileStorageType = getFileStorageTypeResult.getData();

        LOGGER.info("class=FileStorageServiceImpl||method=download||fileStorageType={}", fileStorageType);

        FileStorageHandle fileStorageHandle = (FileStorageHandle) handleFactory.getByHandlerNamePer(fileStorageType);

        return fileStorageHandle.download(fileName, fileMd5);
    }

    @Override
    public Result<String> getDownloadBaseUrl(FileStorageTypeEnum typeEnum) {

        Result<String> getFileStorageTypeResult = getFileStorageType(typeEnum);
        if (getFileStorageTypeResult.failed()) {
            return getFileStorageTypeResult;
        }

        String fileStorageType = getFileStorageTypeResult.getData();

        LOGGER.info("class=FileStorageServiceImpl||method=getDownloadBaseUrl||fileStorageType={}", fileStorageType);

        FileStorageHandle fileStorageHandle = (FileStorageHandle) handleFactory.getByHandlerNamePer(fileStorageType);

        return Result.build(Boolean.TRUE, fileStorageHandle.getDownloadBaseUrl());
    }

    /*****************************************private*************************************************************/

    private Result<String> getFileStorageType(FileStorageTypeEnum typeEnum) {
        if (AriusObjUtils.isNull(typeEnum)) {
            return Result.build(Boolean.TRUE, FileStorageTypeEnum.DEFAULT.getType());
        }

        if (FileStorageTypeEnum.valueOfCode(typeEnum.getCode()).getCode() == -1) {
            return Result.buildFail(String.format("获取 %s 类型出错", typeEnum.getType()));
        }

        return Result.build(Boolean.TRUE, FileStorageTypeEnum.valueOfCode(typeEnum.getCode()).getType());
    }
}
