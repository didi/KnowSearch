package com.didichuxing.datachannel.arius.admin.remote.storage.gift;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.didichuxing.datachannel.arius.admin.common.exception.FileUploadException;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
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
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.remote.storage.FileStorageHandle;
import com.didichuxing.datachannel.arius.admin.remote.storage.gift.bean.GiftResponseDTO;

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
        GiftResponseDTO giftResponseDTO;
        while (retryCount < RETRY_COUNT) {
            String response = "";
            try {
                response = uploadFile(url, uploadFile, FILE_PARAM, null, null);
                giftResponseDTO = ConvertUtil.str2ObjByJson(response, GiftResponseDTO.class);
                if (!giftResponseDTO.getStatusCode().equals(STATUS_CODE)) {
                    retryCount++;
                    continue;
                }
            } catch (Exception e) {
                LOGGER.error("class=GiftFileStorageHandle||method=upload||errMsg=parse json failed, response:{}", response, e);
                retryCount++;
                continue;
            }
            return Result.build(Boolean.TRUE, giftResponseDTO.getDownloadUrl());
        }
        return Result.build(Boolean.FALSE, "");
    }

    @Override
    public Result<Void> remove(String fileName) {
        String url = uploadUrl + namespace + PATH_SEPARATOR + fileName;
        int retryCount = 0;
        GiftResponseDTO giftResponseDTO;
        while (retryCount < RETRY_COUNT) {
            String response = removeFile(url);
            try {
                giftResponseDTO = ConvertUtil.str2ObjByJson(response, GiftResponseDTO.class);
                if (!giftResponseDTO.getStatusCode().equals(STATUS_CODE)) {
                    retryCount++;
                    continue;
                }
            } catch (Exception e) {
                retryCount++;
                LOGGER.error("class=GiftFileStorageHandle||method=remove||errMsg=parse json failed, response:{}", response, e);
                continue;
            }
            return Result.buildSucc();
        }
        return Result.buildFail();
    }

    @Override
    public Result<MultipartFile> download(String fileName) {
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
            LOGGER.error("class=GiftFileStorageHandle||method=download||errMsg=download file filed, fileName:{}", fileName, e);
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("class=GiftFileStorageHandle||method=download||errMsg=close input stream failed", e);
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
                              Map<String, String> otherParams) throws FileUploadException {
        HttpPost post = new HttpPost(url);
        String response = "";
        try {
            String fileName = file.getOriginalFilename();
            if (!AriusObjUtils.isNull(headerParams)) {
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
            if (!AriusObjUtils.isNull(otherParams)) {
                for (Map.Entry<String, String> e : otherParams.entrySet()) {
                    builder.addTextBody(e.getKey(), e.getValue());
                }
            }
            HttpEntity postEntity = builder.build();
            post.setEntity(postEntity);
            HttpEntity entity = HTTP_CLIENT.execute(post).getEntity();
            response = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new FileUploadException(String.format("error post data to %s", url), e);
        } finally {
            post.releaseConnection();
        }
        return response;
    }

    private String removeFile(String url) {
        String response = "";
        HttpDelete httpDelete = new HttpDelete(url);
        try {
            HttpEntity entity = HTTP_CLIENT.execute(httpDelete).getEntity();
            response = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("class=GiftFileStorageHandle||method=removeFile||url={}||errMsg={}", url, e.getMessage());
        }
        return response;
    }
}
