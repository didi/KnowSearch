package com.didichuxing.datachannel.arius.admin.remote.storage;

import org.springframework.web.multipart.MultipartFile;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;

/**
 * @author linyunan
 * @date 2021-05-19
 */
public interface FileStorageHandle extends BaseHandle {
    /**
     * 上传
     * @param fileName 文件名
     * @param fileMd5 文件md5
     * @param uploadFile 文件
     * @return 上传结果
     */
    Result<String> upload(String fileName, String fileMd5, MultipartFile uploadFile);

    /**
     * 下载文件
     * @param fileName 文件名
     * @return 文件
     */
    Result<MultipartFile> download(String fileName);

    Result<Void> remove(String fileName);

    /**
     * 下载base地址
     */
    String getDownloadBaseUrl();
}
