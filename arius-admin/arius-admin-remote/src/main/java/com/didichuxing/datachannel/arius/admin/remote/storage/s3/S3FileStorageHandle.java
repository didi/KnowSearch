package com.didichuxing.datachannel.arius.admin.remote.storage.s3;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.remote.storage.FileStorageHandle;
import io.minio.*;
import io.minio.messages.Item;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author linyunan
 * @date 2021-05-19
 */
@Component
@NoArgsConstructor
public class S3FileStorageHandle implements FileStorageHandle {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3FileStorageHandle.class);

    @Value("${s3.endpoint:}")
    private String              endpoint;

    @Value("${s3.access-key:}")
    private String              accessKey;

    @Value("${s3.secret-key:}")
    private String              secretKey;

    @Value("${s3.bucket:}")
    private String              bucket;

    private MinioClient         minioClient;

    @PostConstruct
    public void init() {
        try {
            if (AriusObjUtils.anyBlank(this.endpoint, this.accessKey, this.secretKey, this.bucket)) {
                // without config s3
                return;
            }
            minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
        } catch (Exception e) {
            LOGGER.error("class=S3FileStorageHandle||method=init||fields={}||errMsg={}", this, e.getMessage());
        }
    }

    @Override
    public synchronized Result<String> upload(String fileName, String fileMd5, MultipartFile uploadFile) {
        InputStream inputStream = null;
        try {
            if (!createBucketIfNotExist()) {
                return Result.build(Boolean.FALSE, "");
            }

            if (this.getFileNames().contains(fileName)) {
                return Result.buildFail("fileName has existed,please modify!");
            }

            inputStream = uploadFile.getInputStream();
            minioClient.putObject(PutObjectArgs.builder().bucket(this.bucket).object(fileName)
                .stream(inputStream, inputStream.available(), -1).build());
            String url = minioClient.getObjectUrl(bucket, fileName);
            return Result.build(Boolean.TRUE, url);
        } catch (Exception e) {
            LOGGER.error("class=S3FileStorageHandle||method=upload||fileName={}||errMsg={}||msg=upload failed",
                fileName, e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error(
                        "class=S3FileStorageHandle||method=upload||fileName={}||errMsg={}||msg=inputStream close failed",
                        fileName, e.getMessage());
                }
            }
        }
        return Result.build(Boolean.FALSE, "");
    }

    @Override
    public synchronized Result<Void> remove(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(this.bucket).object(fileName).build());
            return Result.buildSucc();
        } catch (Exception e) {
            LOGGER.error("class=S3FileStorageHandle||method=remove||fileName={}||errMsg={}||msg=upload failed",
                fileName, e.getMessage());
        }
        return Result.buildFail();
    }

    @Override
    public Result<MultipartFile> download(String fileName) {
        try {
            final ObjectStat stat = minioClient
                .statObject(StatObjectArgs.builder().bucket(this.bucket).object(fileName).build());

            InputStream is = minioClient
                .getObject(GetObjectArgs.builder().bucket(this.bucket).object(fileName).build());
            return Result.buildSucc(new MockMultipartFile(fileName, fileName, stat.contentType(), is));
        } catch (Exception e) {
            LOGGER.error("class=S3FileStorageHandle||method=download||fileName={}||errMsg={}||msg=download failed",
                fileName, e.getMessage());
        }
        return Result.build(ResultType.STORAGE_DOWNLOAD_FILE_FAILED);
    }

    @Override
    public String getDownloadBaseUrl() {
        if (this.endpoint.startsWith("http://")) {
            return this.endpoint + "/" + this.bucket;
        }
        return "http://" + this.endpoint + "/" + this.bucket;
    }

    private boolean createBucketIfNotExist() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(this.bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(this.bucket).build());
            }

            LOGGER.info(
                "class=S3FileStorageHandle||method=createBucketIfNotExist||bucket={}||msg=check and create bucket success",
                this.bucket);
            return true;
        } catch (Exception e) {
            LOGGER.error(
                "class=S3FileStorageHandle||method=createBucketIfNotExist||bucket={}||errMsg={}||msg=create bucket failed",
                this.bucket, e.getMessage());
        }
        return false;
    }

    @Override
    public String toString() {
        return "S3Service{" + "endpoint='" + endpoint + '\'' + ", accessKey='" + accessKey + '\'' + ", secretKey='"
               + secretKey + '\'' + ", bucket='" + bucket + '\'' + '}';
    }

    /**
     * 获取指定存储桶下的文件名称列表
     * @return fileNamesOfBucket List<String>
     */
    private List<String> getFileNames() {
        Iterable<io.minio.Result<Item>> results = minioClient
            .listObjects(ListObjectsArgs.builder().bucket(this.bucket).build());
        List<String> fileNamesOfBucket = Lists.newArrayList();
        results.forEach(itemResult -> {
            try {
                fileNamesOfBucket.add(itemResult.get().objectName());
            } catch (Exception e) {
                LOGGER.warn("class=S3FileStorageHandle||method=getFileNames||bucket={}||warnMsg={}", this.bucket,
                    e.getMessage());
            }
        });
        return fileNamesOfBucket;
    }
}
