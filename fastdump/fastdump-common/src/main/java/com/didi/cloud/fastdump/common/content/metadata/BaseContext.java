package com.didi.cloud.fastdump.common.content.metadata;

import java.io.Serializable;
import java.util.concurrent.Semaphore;

import org.elasticsearch.rest.RestRequest.Method;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
public abstract class BaseContext implements Serializable {
    /**
     * 上下文请求id
     */
    private String requestId;

    /**
     * http请求的方法
     */
    private Method method;

    /**
     * Deprecated
     * 一个查询模板对应的searchId
     */
    private String searchId;

    /**
     * 用户从header中传递的集群名称
     */
    private String clusterId;

    /**
     * 用户在header中传递的用户名
     */
    private String user;

    /**
     * url请求的请求参数,问好之后的内容
     */
    private String queryString;

    /**
     * http请求的消息体
     */
    private String postBody;

    /**
     * 请求客户端ip
     */
    private String remoteAddr;

    /**
     * 认证信息
     */
    private String authentication;

    /**
     * appid
     */
    private int appid;

    /**
     * 请求时刻的时间戳
     */
    private long requestTime;

    /**
     * 响应时刻的时间戳
     */
    private long responseTime;

    /**
     * 请求整体耗时
     */
    @Setter(AccessLevel.NONE)
    private long costTime;

    /**
     * 用户传递的traceid
     */
    private String traceid;

    /**
     * 用户传递的spanid
     */
    private String spanid;

    /**
     * 查询对应的dsl模板
     */
    private String dslTemplateKey;

    /**
     * 控制并发的信号量
     */
    private Semaphore semaphore;

    /**
     * 是否要记录详细log
     *
     * 非写入的Action会记录log
     * 如果admin配置了开启，则所有请求都会记录日志
     */
    private boolean detailLog;

    /**
     * 是否需要使用typedKeys功能
     */
    private boolean typedKeys;

    /**
     * 请求ES集群名称
     */
    private String clusterName;

    /**
     * 本次查询的type名称
     */
    private String typeNames;
    /**
     * 慢查询阈值时间
     */
    private long requestSlowlogThresholdMills;
    /**
     * httpResponse最大长度参考
     */
    private int maxHttpResponseLength;

    /**
     * 日志上下文
     */
    private JoinLogContext joinLogContext;

    /**
     * 查询模式
     */
    private Integer searchType;
}