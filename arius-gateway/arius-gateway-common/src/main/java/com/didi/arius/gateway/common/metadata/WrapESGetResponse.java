package com.didi.arius.gateway.common.metadata;


import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESGetResponse;
import lombok.Data;

@Data
public class WrapESGetResponse {
    private ESGetResponse esGetResponse;

    private ResultType resultType;

    public static enum ResultType {
        ALL,
        SOURCE,
        HEAD
    }
}
