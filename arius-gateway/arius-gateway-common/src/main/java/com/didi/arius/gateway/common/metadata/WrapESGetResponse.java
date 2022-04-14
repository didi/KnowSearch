package com.didi.arius.gateway.common.metadata;


import com.didi.arius.gateway.elasticsearch.client.gateway.document.ESGetResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WrapESGetResponse {
    private ESGetResponse esGetResponse;

    private ResultType resultType;

    public enum ResultType {
        ALL,
        SOURCE,
        HEAD
    }
}
