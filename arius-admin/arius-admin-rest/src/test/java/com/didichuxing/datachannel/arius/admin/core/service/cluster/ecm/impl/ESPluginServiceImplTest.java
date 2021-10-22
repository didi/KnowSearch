package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPluginDTO;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ESPluginServiceImplTest extends AriusAdminApplicationTests {

    @Autowired
    private ESPluginService esPluginService;

    @Test
    public void addESPlugins() throws IOException {
        // 1. 读取文件
        final ClassPathResource classPathResource = new ClassPathResource("elasticsearch-analysis-ik-7.6.1.zip");
        final File file = classPathResource.getFile();
        FileInputStream fileInputStream = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(),
                ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);

        final ClassPathResource classPathResource_1 = new ClassPathResource("elasticsearch-analysis-pinyin-7.6.1.zip");
        final File file_1 = classPathResource_1.getFile();
        FileInputStream fileInputStream_1 = new FileInputStream(file_1);
        MultipartFile multipartFile_1 = new MockMultipartFile(file_1.getName(), file_1.getName(),
                ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream_1);

        // 2. 构建ESPluginDTO
        final ESPluginDTO esPluginDTO = new ESPluginDTO();
//        esPluginDTO.setId(1);
        esPluginDTO.setName("ik分词器");
        esPluginDTO.setPhysicClusterId("57");
        esPluginDTO.setVersion("7.6.1");
        esPluginDTO.setUrl(file.getName());
        esPluginDTO.setMd5("b8ff08a5b6e0b9ac04925d8811e99098");
        esPluginDTO.setDesc("ik分词器，用于xxxxxxxxx");
        esPluginDTO.setCreator("zhangshuo_i");
        esPluginDTO.setPDefault(false);
        esPluginDTO.setFileName(file.getName());
        esPluginDTO.setUploadFile(multipartFile);
        System.out.println(esPluginDTO);

        final ESPluginDTO esPluginDTO_1 = new ESPluginDTO();
//        esPluginDTO.setId(1);
        esPluginDTO_1.setName("pinyin分词器");
        esPluginDTO_1.setPhysicClusterId("57");
        esPluginDTO_1.setVersion("7.6.1");
        esPluginDTO_1.setUrl(file_1.getName());
        esPluginDTO_1.setMd5("1db9e083f94471be98838b9e79bcde70");
        esPluginDTO_1.setDesc("pinyin分词器，用于xxxxxxxxx");
        esPluginDTO_1.setCreator("zhangshuo_i");
        esPluginDTO_1.setPDefault(false);
        esPluginDTO_1.setFileName(file_1.getName());
        esPluginDTO_1.setUploadFile(multipartFile_1);
        System.out.println(esPluginDTO_1);

        // 3. 上传文件
        final ArrayList<ESPluginDTO> esPluginDTOS = new ArrayList<>();
        esPluginDTOS.add(esPluginDTO);
        esPluginDTOS.add(esPluginDTO_1);

        final Result result = esPluginService.addESPlugins(esPluginDTOS);
        System.out.println(result);
    }
}