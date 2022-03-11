package com.didichuxing.datachannel.arius.admin;

import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AriusClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AriusClient.class);
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");
    private static String prefix;
    private static String user = "admin";
    private static String app = "";

    public static String postForFileForm(String path, String fileFormKey, File fileFormValue, Map<String, Object> params) throws IOException {
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), fileFormValue);
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.addFormDataPart(fileFormKey, fileFormValue.getName(), fileBody);
        builder.setType(MultipartBody.FORM);
        for(String key : params.keySet()) {
            builder.addFormDataPart(key, String.valueOf(params.get(key)));
        }

        Request request = new Request.Builder()
                .url(prefix + path)
                .post(builder.build())
                .addHeader("X-SSO-USER", user)
                .addHeader("X-ARIUS-APP-ID", app)
                .build();

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(1200000, TimeUnit.MILLISECONDS).build();

        long start = System.currentTimeMillis();
        try (Response response = client.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            long end = System.currentTimeMillis();
            LOGGER.info("class=AriusClient||method=postForFileForm||msg=used time {}ms", end - start);
            LOGGER.info("class=AriusClient||method=postForFileForm||msg={}", responseBody);
            return responseBody;
        }
    }

    private static Request buildRequest(String path, String method, String requestBody) {
        if (requestBody != null) {
            LOGGER.info("class=AriusClient||method=run||msg={}", requestBody);
        }

        RequestBody body = requestBody != null ? RequestBody.create(requestBody, JSON_TYPE) : null;
        Request.Builder builder = new Request.Builder();
        builder.url(prefix + path)
                .method(method, body);
        if (!StringUtils.isBlank(user)) {
            builder.addHeader("X-SSO-USER", user);
        }
        if (!StringUtils.isBlank(app)) {
            builder.addHeader("X-ARIUS-APP-ID", app);
        }
        return builder.build();
    }

    public static String run(String path, String method, String requestBody) throws IOException {
        long start = System.currentTimeMillis();
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(1200000, TimeUnit.MILLISECONDS).build();
        Request request = buildRequest(path, method, requestBody);
        try (Response response = client.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            long end = System.currentTimeMillis();
            LOGGER.info("class=AriusClient||method=run||msg=used time {}ms", end - start);
            LOGGER.info("class=AriusClient||method=run||msg={}", responseBody);
            return responseBody;
        }
    }

    public static String runPage(String path, String method, String requestBody) throws IOException {
        long start = System.currentTimeMillis();
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(1200000, TimeUnit.MILLISECONDS).build();
        Request request = buildRequest(path, method, requestBody);
        try (Response response = client.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            long end = System.currentTimeMillis();
            LOGGER.info("class=AriusClient||method=runPage||msg=used time {}ms", end - start);
            LOGGER.info("class=AriusClient||method=runPage||msg={}", responseBody);
            return responseBody;
        }
    }

    public static String get(String path) throws IOException {
        return run(path, "GET", null);
    }

    private static String buildPathWithParams(String path, Map<String, Object> params) {
        Iterator<Map.Entry<String, Object>> var3;
        if(params != null) {
            StringBuilder builder = (new StringBuilder(path)).append('?');
            var3 = params.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry<String, Object> e = var3.next();
                builder.append(e.getKey()).append('=').append(e.getValue()).append('&');
            }

            path = builder.toString();
        }
        return path;
    }

    public static String get(String path, Map<String, Object> params) throws IOException {
        path = buildPathWithParams(path, params);
        return get(path);
    }

    public static String post(String path, Object requestBody) throws IOException {
        if (requestBody == null) {
            return run(path, "POST", null);
        }
        return run(path, "POST", JSON.toJSONString(requestBody));
    }

    public static String postPage(String path, Object requestBody) throws IOException {
        if (requestBody == null) {
            return runPage(path, "POST", null);
        }
        return runPage(path, "POST", JSON.toJSONString(requestBody));
    }

    public static String put(String path, Object requestBody) throws IOException {
        if (requestBody == null) {
            return run(path, "PUT", JSON.toJSONString(new HashMap<>()));
        }
        return run(path, "PUT", JSON.toJSONString(requestBody));
    }

    public static String put(String path, Map<String, Object> params, Object requestBody) throws IOException {
        if (params == null) {
            return delete(path, JSON.toJSONString(requestBody));
        }
        path = buildPathWithParams(path, params);
        return put(path, JSON.toJSONString(requestBody));
    }

    public static String put(String path) throws IOException {
        return put(path, new HashMap<>());
    }

    public static String delete(String path) throws IOException {
        return delete(path, null);
    }

    private static String delete(String path, Object requestBody) throws IOException {
        if (requestBody == null) {
            return run(path, "DELETE", null);
        }
        return run(path, "DELETE", JSON.toJSONString(requestBody));
    }

    public static String delete(String path, Map<String, Object> params, Object requestBody) throws IOException {
        if (params == null) {
            return delete(path, JSON.toJSONString(requestBody));
        }
        path = buildPathWithParams(path, params);
        return delete(path, JSON.toJSONString(requestBody));
    }

    public static String getPrefix() {
        return prefix;
    }

    public static void setPrefix(String prefix) {
        AriusClient.prefix = prefix;
    }

    public static String getUser() {
        return user;
    }

    public static void setUser(String user) {
        AriusClient.user = user;
    }

    public static String getApp() {
        return app;
    }

    public static void setApp(String app) {
        AriusClient.app = app;
    }
}
