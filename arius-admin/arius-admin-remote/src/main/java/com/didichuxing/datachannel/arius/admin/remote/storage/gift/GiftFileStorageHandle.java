package com.didichuxing.datachannel.arius.admin.remote.storage.gift;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import com.didichuxing.datachannel.arius.admin.remote.storage.FileStorageHandle;
import com.didichuxing.datachannel.arius.admin.remote.storage.gift.bean.GiftResponseDTO;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * @author linyunan
 * @date 2021-05-21
 */
@Component
public class GiftFileStorageHandle implements FileStorageHandle {
    private static final Logger     LOGGER         = LoggerFactory.getLogger(GiftFileStorageHandle.class);

    @Value("${fs.gift.upload-url}")
    private String                  uploadUrl;

    @Value("${fs.gift.download-url}")
    private String                  downloadUrl;

    @Value("${fs.gift.namespace}")
    private String                  namespace;

    private static final String     FILE_PARAM     = "filecontent";

    private static final Integer    RETRY_COUNT    = 3;

    private static final String     PATH_SEPARATOR = "/";

    private static final Integer    STATUS_CODE    = 200;

    private static final HttpClient HTTP_CLIENT    = HttpClients.createDefault();

    private static final String     GET            = "GET";

    private static final String     UTF8           = "UTF8";

    private static final String     CHARSET        = "CHARSET";

    @Override
    public Result<String> upload(String fileMd5, String fileName, MultipartFile uploadFile) {
        String url = uploadUrl + namespace + PATH_SEPARATOR + fileName;
        int retryCount = 0;
        // 重试
        GiftResponseDTO giftResponseDTO;
        while (retryCount < RETRY_COUNT) {
            String response = uploadFile(url, uploadFile, FILE_PARAM, null, null);
            try {
                giftResponseDTO = ConvertUtil.str2ObjByJson(response, GiftResponseDTO.class);
                if (!giftResponseDTO.getStatus_code().equals(STATUS_CODE)) {
                    continue;
                }
            } catch (Exception e) {
                LOGGER.error("parse json failed, response:{}", response, e);
                continue;
            }
            return Result.build(Boolean.TRUE, giftResponseDTO.getDownload_url());
        }
        return Result.build(Boolean.FALSE, "");
    }

    @Override
    public Result<MultipartFile> download(String fileName, String fileMd5) {
        InputStream inputStream = null;
        MultipartFile multipartFile = null;
        try {
            URL url = new URL(downloadUrl + namespace + PATH_SEPARATOR + fileName);
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod(GET);
            httpURLConnection.setRequestProperty(CHARSET, UTF8);
            inputStream = httpURLConnection.getInputStream();
            multipartFile = new MockMultipartFile(fileName, inputStream);
        } catch (Exception e) {
            LOGGER.error("download file filed, fileName:{}", fileName, e);
        } finally {
            if (!ValidateUtils.isNull(inputStream)) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("close input stream failed", e);
                }
            }
        }

        return Result.buildSucc(multipartFile);
    }

    @Override
    public String getDownloadBaseUrl() {
        return null;
    }

    private String uploadFile(String url, MultipartFile file, String fileParamName, Map<String, String> headerParams,
                              Map<String, String> otherParams) {
        HttpPost post = new HttpPost(url);
        String response = "";
        try {
            String fileName = file.getOriginalFilename();
            if (!ValidateUtils.isNull(headerParams)) {
                for (Map.Entry<String, String> e : headerParams.entrySet()) {
                    post.addHeader(e.getKey(), e.getValue());
                }
            }
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(Charset.forName(UTF8));
            //加上此行代码解决返回中文乱码问题
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            // 文件流
            builder.addBinaryBody(fileParamName, file.getInputStream(), ContentType.MULTIPART_FORM_DATA, fileName);
            if (!ValidateUtils.isNull(otherParams)) {
                for (Map.Entry<String, String> e : otherParams.entrySet()) {
                    builder.addTextBody(e.getKey(), e.getValue());
                }
            }
            HttpEntity postEntity = builder.build();
            post.setEntity(postEntity);
            HttpEntity entity = HTTP_CLIENT.execute(post).getEntity();
            response = EntityUtils.toString(entity, UTF8);
        } catch (Exception e) {
            throw new RuntimeException("error post data to " + url, e);
        } finally {
            post.releaseConnection();
        }
        return response;
    }
}
