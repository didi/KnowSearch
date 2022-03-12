package com.didichuxing.datachannel.arius.admin;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AriusClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AriusClient.class);
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");
    private static String prefix;
    private static String user = "Tester";
    private static String app = "";

    public <T> Result<T> run(String path, String method, String requestBody) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().build();
        if (requestBody != null) {
            LOGGER.info("class=AriusClient||method=run||msg={}", requestBody);
        }
        long start = System.currentTimeMillis();
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
        Request request = builder.build();
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            long end = System.currentTimeMillis();
            LOGGER.info("class=AriusClient||method=run||msg=used time {}ms", end - start);
            LOGGER.info("class=AriusClient||method=run||msg={}", responseBody);
            return JSON.parseObject(responseBody, Result.class);
        }
    }

    public <T> Result<T> get(String path) throws IOException {
        return run(path, "GET", null);
    }

    public <T> Result<T> post(String path, Object requestBody) throws IOException {
        if (requestBody == null) {
            return run(path, "POST", null);
        }
        return run(path, "POST", JSON.toJSONString(requestBody));
    }

    public <T> Result<T> put(String path, Object requestBody) throws IOException {
        if (requestBody == null) {
            return run(path, "PUT", null);
        }
        return run(path, "PUT", JSON.toJSONString(requestBody));
    }

    public <T> Result<T> delete(String path, Object requestBody) throws IOException {
        if (requestBody == null) {
            return run(path, "DELETE", null);
        }
        return run(path, "DELETE", JSON.toJSONString(requestBody));
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
