package com.didi.arius.gateway.remote.response;

import lombok.Data;

import java.util.List;

@Data
public class DynamicConfigListResponse extends BaseAdminResponse {
    private List<DynamicConfigResponse> data;

}
