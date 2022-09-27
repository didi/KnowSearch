package com.didichuxing.datachannel.arius.admin.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NullESClientException;
import com.didiglobal.logi.elasticsearch.client.model.exception.ESAlreadyExistsException;
import com.didiglobal.logi.elasticsearch.client.model.exception.ESIndexNotFoundException;
import com.didiglobal.logi.elasticsearch.client.model.exception.ESIndexTemplateMissingException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;

/**
 *  `ParsingExceptionUtils` 是一个实用程序类，提供解析异常的方法
 *
 * @author shizeying
 * @date 2022/08/17
 */
public final class ParsingExceptionUtils {
    private final static String CONNECTION_REFUSED="Connection refused";
    public final static String CLUSTER_ERROR = "当前操作集群异常";
    public final static String CONNECT_EXCEPTION = "connect_exception";
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
    
    /**
     * 如果异常不是解析异常，则抛出新异常
     *
     * @param e ES 客户端抛出的异常对象。
     */
    public static void abnormalTermination(Exception e) throws ESOperateException {
        String exception = ParsingExceptionUtils.getESErrorMessageByException(e);
        if (StringUtils.isNotBlank(exception)&&!StringUtils.equals(exception,CLUSTER_ERROR)) {
            throw new ESOperateException(exception);
        }
        if (StringUtils.equals(exception,CLUSTER_ERROR)){
            throw new NullESClientException(exception, ResultType.ES_CLIENT_NUL_ERROR);
        }
    }
    
    
    /**
     * > 它返回一个字符串，该字符串是异常的错误消息
     *
     * @param e 抛出的异常。
     * @return 错误信息
     */
    public static String getESErrorMessageByException(Exception e) {
        if (e instanceof ESIndexNotFoundException) {
            return getESIndexNotFoundErrorMessageByException((ESIndexNotFoundException) e);
        } else if (e instanceof ESIndexTemplateMissingException) {
            return getESIndexTemplateMissingExceptionErrorMessageByException((ESIndexTemplateMissingException) e);
        } else if (e instanceof ESAlreadyExistsException) {
            return getESAlreadyExistsExceptionErrorMessageByException((ESAlreadyExistsException) e);
        }else if (e instanceof  UncategorizedExecutionException){
             return getErrorMessagePassUncategorizedExecutionFindResponseExceptionByException((UncategorizedExecutionException) e);
        }else if (e instanceof ExecutionException){
             return getExecutionExceptionErrorMessageByException((ExecutionException) e);
        }
        return null;
        
    }
    
    
    /**
     * > 该函数返回Elasticsearch在找不到索引时抛出的异常的错误信息
     *
     * @param indexNotFoundException Elasticsearch 客户端抛出的异常。
     * @return 来自 ResponseException 的错误消息。
     */
    private static String getESIndexNotFoundErrorMessageByException(ESIndexNotFoundException indexNotFoundException) {
        Throwable throwable = indexNotFoundException.getT();
        if (Objects.nonNull(throwable) && throwable instanceof ResponseException) {
            return getErrorMessageByResponseException((ResponseException) throwable).orElse(null);
            
        }
        return null;
    }
    
    /**
     * > 该函数返回索引模板缺失时Elasticsearch抛出的异常的错误信息
     *
     * @param esIndexTemplateMissingException 抛出的异常对象。
     * @return 来自 ResponseException 的错误消息。
     */
    private static String getESIndexTemplateMissingExceptionErrorMessageByException(ESIndexTemplateMissingException esIndexTemplateMissingException) {
        Throwable throwable = esIndexTemplateMissingException.getT();
        if (Objects.nonNull(throwable) && throwable instanceof ResponseException) {
            return getErrorMessageByResponseException((ResponseException) throwable).orElse(null);
            
        }
        return null;
    }
    
    /**
     * > 该函数用于从 ESAlreadyExistsException 中获取错误信息
     *
     * @param esAlreadyExistsException 抛出的异常对象。
     * @return 来自 ResponseException 的错误消息。
     */
    private static String getESAlreadyExistsExceptionErrorMessageByException(ESAlreadyExistsException esAlreadyExistsException) {
        Throwable throwable = esAlreadyExistsException.getT();
        if (Objects.nonNull(throwable) && throwable instanceof ResponseException) {
            return getErrorMessageByResponseException((ResponseException) throwable).orElse(null);
            
        }
        return null;
    }
    
    /**
     * > 如果未分类的执行异常的原因是执行异常，则返回执行异常的错误信息
     *
     * @param uncategorizedExecutionException 抛出的异常。
     * @return 异常的错误信息。
     */
    private static String getErrorMessagePassUncategorizedExecutionFindResponseExceptionByException(
            UncategorizedExecutionException uncategorizedExecutionException) {
        Throwable throwable = uncategorizedExecutionException.getCause();
        if (Objects.nonNull(throwable) && throwable instanceof ExecutionException) {
            return getExecutionExceptionErrorMessageByException((ExecutionException) throwable);
            
        }
        return null;
    }
    
    /**
     * > 如果`ExecutionException`的原因是`ResponseException`，则返回`ResponseException`的错误信息；否则，返回 `null`
     *
     * @param executionException 执行任务时抛出的异常。
     * @return 响应异常的错误信息。
     */
    private static String getExecutionExceptionErrorMessageByException(ExecutionException executionException) {
        Throwable cause = executionException.getCause();
        if (Objects.nonNull(cause) && cause instanceof ResponseException) {
            
            return getErrorMessageByResponseException((ResponseException) cause).orElse(null);
        }
        if (Objects.nonNull(cause) && cause instanceof ConnectException) {
            return StringUtils.equals(cause.getMessage(), CONNECTION_REFUSED) ? CLUSTER_ERROR : cause.getMessage();
        }
        return null;
    }
    
    
    /**
     * 它尝试从响应正文中获取错误消息，如果失败，则返回一个空的 Optional
     *
     * @param e 请求抛出的异常
     * @return 可选<字符串>
     */
    private static Optional<String> getErrorMessageByResponseException(ResponseException e) {
        HttpEntity entity = e.getResponse().getEntity();
        try {
            String error = EntityUtils.toString(entity, "UTF-8");
            if (StringUtils.equals(error,"{}")){
                
                return Optional.of(String.format("%s is not found",e.getResponse().getRequestLine().getUri()));
            }
            
            return Optional.ofNullable(EntityUtils.toString(entity, "UTF-8")).map(JSONObject::parseObject)
                    .map(json -> json.getJSONObject("error")).map(json -> json.getString("reason"));
        } catch (IOException ignore) {
            return Optional.empty();
        }
    }
}