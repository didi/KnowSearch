package com.didi.arius.gateway.remote.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DynamicConfigListResponse extends BaseAdminResponse {
    private List<DynamicConfigResponse> data;

}
