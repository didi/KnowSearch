package com.didi.cloud.fastdump.common.content.metadata;

import java.util.List;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QueryContext extends BaseContext {
    /**
     * http request
     */
    private RestRequest request;

    /**
     * http channel
     */
    private RestChannel channel;

    /**
     * http response
     */
    private RestResponse response;

    /**
     * 本次查询的索引列表
     */
    private List<String> indices;

    /**
     * 用户名
     */
    private String xUserName;

    /**
     * rest请求的名称
     */
    private String restName;

    /**
     * 请求的client版本号
     */
    private String clientVersion;

    /**
     * 是否来自于kibana
     */
    private boolean isFromKibana;

    /**
     * 是否来着于新版本
     */
    private boolean isNewKibana;

    /**
     * 请求ES前的时间点
     */
    private long preQueryEsTime;

    public String getUri() {
        return request.rawPath();
    }


    public void addIndex(String index) {
        this.indices.add(index);
    }

}
