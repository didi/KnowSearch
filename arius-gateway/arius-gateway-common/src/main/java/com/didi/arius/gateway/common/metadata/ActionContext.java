package com.didi.arius.gateway.common.metadata;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.transport.TransportChannel;
import org.elasticsearch.transport.TransportRequest;

/**
 * @author weizijun
 * @date：2016年9月20日
 * 记录tcp请求的上下文信息
 */
@Data
@NoArgsConstructor
public class ActionContext extends BaseContext {
    /**
     * tcp request
     */
    private TransportRequest request;

    /**
     * tcp channal
     */
    private TransportChannel channel;

    /**
     * 请求接口名称
     */
    private String actionName;

    /**
     * 请求内容长度
     */
    private int requestLength;

    /**
     * 响应内容长度
     */
    private int responseLength;

}
