package com.didi.arius.gateway.common.metadata;

import com.didi.arius.gateway.common.consts.QueryConsts;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestRequest.Method;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * @author weizijun
 * @date：2016年9月20日
 * 上下文信息基类
 */
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
     * appid对应的详细信息
     */
    private AppDetail appDetail;

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
     * 用作索引存储分离获取，记录请求访问的fields信息
     */
    private FetchFields fetchFields;

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
     * 请求的索引模板信息
     */
    private IndexTemplate indexTemplate;

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
    /**
     * 项目id
     */
    private Integer projectId;

    public void setAppDetail(AppDetail appDetail) {
        this.appDetail = appDetail;
        this.searchType = appDetail.getSearchType().type;
        this.appid = appDetail.getId();
        this.projectId=appDetail.getProjectId();
    }


    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
        this.costTime = this.responseTime - this.requestTime;
    }

    /**
     * 从应用配置的集群名称，在集群模式下有效，在索引模式下访问的集群名称需要根据主索引所在集群
     *
     * @return
     */
    public String getCluster() {
        if (appDetail != null && appDetail.getCluster() != null) {
            return appDetail.getCluster();
        } else {
            return QueryConsts.DEFAULT_TRIB_CLUSTER;
        }
    }

    public String getTypeNames() {
        return typeNames;
    }

    public void setTypeNames(String typeNames) {
        this.typeNames = typeNames;
    }

    public void setTypeNames(String[] typeNames) {
        if (Objects.nonNull(typeNames)) {
            this.typeNames = StringUtils.join(typeNames, ",");
        }
    }

    public void setTypeNames(Set<String> typeNames) {
        if (Objects.nonNull(typeNames)) {
            this.typeNames = StringUtils.join(typeNames, ",");
        }
    }
}