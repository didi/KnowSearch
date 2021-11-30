package com.didi.arius.gateway.common.metadata;

import com.didi.arius.gateway.elasticsearch.client.ESClient;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;

import java.util.List;

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
     * gateway请求的cluster
     */
    private ESClient client;

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

    public String getUri() {
        return request.rawPath();
    }


    public void addIndex(String index) {
        this.indices.add(index);
    }

}
