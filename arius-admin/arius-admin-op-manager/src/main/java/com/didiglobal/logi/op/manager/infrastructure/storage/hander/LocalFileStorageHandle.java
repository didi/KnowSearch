package com.didiglobal.logi.op.manager.infrastructure.storage.hander;

import com.didiglobal.logi.op.manager.infrastructure.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author didi
 * @date 2022-07-06 10:13 上午
 */
@Component
public class LocalFileStorageHandle implements FileStorageHandle {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileStorageHandle.class);

    @Value("${file.upload.path:upload/}")
    private String path;

    @Override
    public String upload(String fileName, MultipartFile uploadFile) throws FileStorageException {
        try {
            File file = new File(new File(path).getAbsolutePath(), fileName);
            if (file.exists()) {
                throw new FileStorageException("文件已经存在");
            }
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
            // 文件保存
            FileCopyUtils.copy(uploadFile.getBytes(), file);
            StringBuffer url = new StringBuffer();
            url.append(request.getScheme()).append("://").
                    append(request.getLocalAddr()).append(":").
                    append(request.getServerPort()).append("/").
                    append(fileName);
            return url.toString();
        } catch (Exception e) {
            LOGGER.error("class=LocalFileStorageHandle||method=upload||fileName={}||errMsg={}||msg=upload failed",
                    fileName, e.getMessage());
            throw new FileStorageException(e);
        }

    }

    @Override
    public MultipartFile download(String fileName) throws FileStorageException {
        try {
            InputStream inputStream = Files.newInputStream(Paths.get(path,fileName));
            return new MockMultipartFile(fileName, inputStream);
        } catch (Exception e) {
            LOGGER.error("class=LocalFileStorageHandle||method=download||fileName={}||errMsg={}||msg=download failed",
                    fileName, e.getMessage());
            throw new FileStorageException(e);
        }

    }

    @Override
    public void remove(String fileName) throws FileStorageException {
        try {
            Files.delete(Paths.get(path, fileName));
        } catch (Exception e) {
            LOGGER.error("class=LocalFileStorageHandle||method=remove||fileName={}||errMsg={}||msg=remove failed",
                    fileName, e.getMessage());
            throw new FileStorageException(e);
        }

    }
}
