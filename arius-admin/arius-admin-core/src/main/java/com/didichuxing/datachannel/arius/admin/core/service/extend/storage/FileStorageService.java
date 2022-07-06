package com.didichuxing.datachannel.arius.admin.core.service.extend.storage;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.remote.storage.content.FileStorageTypeEnum;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author linyunan
 * @date 2021-05-19
 */
public interface FileStorageService {
    /**
     * 根据 FileStorageTypeEnum 获取接口实现类 ,实现上传
     *
     * @param fileName   文件名
     * @param fileMd5    文件md5
     * @param uploadFile 文件
     * @return 上传结果
     * @see FileStorageTypeEnum
     */
    Result<String> upload(String fileName, String fileMd5, MultipartFile uploadFile) throws NotFindSubclassException;

    Result<Void> remove(String fileName) throws NotFindSubclassException;

    /**
     * 根据 FileStorageTypeEnum 获取接口实现类, 实现下载文件
     *
     * @param fileName 文件名
     * @see  FileStorageTypeEnum
     * @return 文件
     */
    Result<MultipartFile> download(String fileName) throws NotFindSubclassException;

    /**
     * 下载base地址
     *
     * @see FileStorageTypeEnum
     */
    Result<String> getDownloadBaseUrl() throws NotFindSubclassException;
}
