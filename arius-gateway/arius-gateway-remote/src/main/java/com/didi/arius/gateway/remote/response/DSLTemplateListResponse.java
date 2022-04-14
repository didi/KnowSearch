package com.didi.arius.gateway.remote.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DSLTemplateListResponse extends BaseAdminResponse {
    private DSLTemplateWrapResponse data;

}
