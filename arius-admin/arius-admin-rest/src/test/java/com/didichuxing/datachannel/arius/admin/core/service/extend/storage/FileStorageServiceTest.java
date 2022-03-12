package com.didichuxing.datachannel.arius.admin.core.service.extend.storage;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
public class FileStorageServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    void getDownloadBaseUrl() {
        Result<String> result = fileStorageService.getDownloadBaseUrl();
        Assertions.assertEquals(0, result.getCode());
    }

    @Test
    void upLoadTest() throws IOException {
        String filePath = "/Users/didi/wpkShell/test.sh";
        String fileName = "test.sh";
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileInputStream);
        Result<String> upload = fileStorageService.upload(fileName, null, mockMultipartFile);
        Assertions.assertEquals("",upload.getData());
    }

    @Test
    void downLoadTest() {
        String fileName = "test.sh";
        Result<MultipartFile> download = fileStorageService.download(fileName);
        Assertions.assertEquals(0, download.getCode());
    }

    @Test
    void removeTest(){
        String fileName = "test.sh";
        Result<Void> remove = fileStorageService.remove(fileName);
        Assertions.assertTrue(remove.success());
    }
}
