package com.didichuxing.datachannel.arius.admin.core.service.extend.storage;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.remote.storage.s3.S3FileStorageHandle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author cjm
 */
@Transactional
@Rollback
public class FileStorageServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private FileStorageService fileStorageService;

    @MockBean
    private S3FileStorageHandle s3FileStorageHandle;

    @Test
    void getDownloadBaseUrl() {
        Mockito.when(s3FileStorageHandle.getDownloadBaseUrl()).thenReturn("");
        Assertions.assertTrue(fileStorageService.getDownloadBaseUrl().success());
    }

    @Test
    void upLoadTest() throws IOException {
        Mockito.when(s3FileStorageHandle.upload(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(Result.buildSucc());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test", new byte[10]);
        Assertions.assertTrue(fileStorageService.upload("test", "test", mockMultipartFile).success());
    }

    @Test
    void downLoadTest() {
        Mockito.when(s3FileStorageHandle.download(Mockito.anyString())).thenReturn(Result.buildSucc());
        Result<MultipartFile> download = fileStorageService.download("test");
        Assertions.assertTrue(download.success());
    }

    @Test
    void removeTest(){
        Mockito.when(s3FileStorageHandle.remove(Mockito.anyString())).thenReturn(Result.buildSucc());
        Assertions.assertTrue(fileStorageService.remove(Mockito.anyString()).success());
    }
}
