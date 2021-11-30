package com.didi.arius.gateway.remote.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class IndexTemplateListResponse extends BaseAdminResponse {
    private Map<String, IndexTemplateResponse> data;

}
