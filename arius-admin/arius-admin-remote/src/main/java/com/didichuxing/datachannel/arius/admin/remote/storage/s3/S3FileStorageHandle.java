package com.didichuxing.datachannel.arius.admin.remote.storage.s3;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import com.didichuxing.datachannel.arius.admin.remote.storage.FileStorageHandle;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.PutObjectArgs;

/**
 * @author linyunan
 * @date 2021-05-19
 */
@Component
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
            if (ValidateUtils.anyBlank(this.endpoint, this.accessKey, this.secretKey, this.bucket)) {
                // without config s3
                return;
            }
            minioClient = new MinioClient(endpoint, accessKey, secretKey);
        } catch (Exception e) {
            LOGGER.error("class=S3FileStorageHandle||method=init||fields={}||errMsg={}", this.toString(),
                e.getMessage());
        }
    }

    @Override
    public Result<String> upload(String fileName, String fileMd5, MultipartFile uploadFile) {
        InputStream inputStream = null;
        try {
            if (!createBucketIfNotExist()) {
                return Result.build(Boolean.FALSE, "");
            }

            inputStream = uploadFile.getInputStream();
            minioClient.putObject(PutObjectArgs.builder().bucket(this.bucket).object(fileName)
                .stream(inputStream, inputStream.available(), -1).build());
            return Result.build(Boolean.TRUE, "");
        } catch (Exception e) {
            LOGGER.error("class=S3FileStorageHandle||method=upload||fileName={}||errMsg={}||msg=upload failed",
                fileName, e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    ; // ignore
                }
            }
        }
        return Result.build(Boolean.FALSE, "");
    }

    @Override
    public Result<MultipartFile> download(String fileName, String fileMd5) {
        try {
            final ObjectStat stat = minioClient.statObject(this.bucket, fileName);

            InputStream is = minioClient.getObject(this.bucket, fileName);

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
}
