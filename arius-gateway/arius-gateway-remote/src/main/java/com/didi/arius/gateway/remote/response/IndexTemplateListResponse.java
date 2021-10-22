package com.didi.arius.gateway.remote.response;

import lombok.Data;

import java.util.Map;

@Data
public class IndexTemplateListResponse extends BaseAdminResponse {
    private Map<String, IndexTemplateResponse> data;

}
