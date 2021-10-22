package com.didi.arius.gateway.remote.response;

import com.didi.arius.gateway.remote.response.BaseAdminResponse;
import lombok.Data;

@Data
public class DSLTemplateListResponse extends BaseAdminResponse {
    private DSLTemplateWrapResponse data;

}
