package com.didichuxing.datachannel.arius.admin.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.ResponseException;

/**
 *  `ParsingExceptionUtils` 是一个实用程序类，提供解析异常的方法
 *
 * @author shizeying
 * @date 2022/08/17
 */
public final class ParsingExceptionUtils {
    private ParsingExceptionUtils(){}
    
    /**
     * > 如果异常是 `ResponseException`，则获取响应实体并将其解析为 JSON 对象
     *
     * @param e 请求抛出的异常
     * @return 一个 JSON 对象
     */
    public static JSONObject getResponseExceptionJsonMessageByException(Exception e) {
        final Throwable cause = e.getCause();
        if (cause instanceof ExecutionException) {
            final Throwable throwable =  cause.getCause();
            if (throwable instanceof ResponseException) {
                final HttpEntity entity = ((ResponseException) throwable).getResponse().getEntity();
                try {
                    return JSON.parseObject(EntityUtils.toString(entity));
                } catch (IOException ignore) {
                    return null;
                }
                
            }
            
        }
        return null;
    }
}