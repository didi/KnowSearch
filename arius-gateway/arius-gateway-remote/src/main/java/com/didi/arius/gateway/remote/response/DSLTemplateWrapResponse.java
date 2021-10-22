package com.didi.arius.gateway.remote.response;

import lombok.Data;

import java.util.List;

@Data
public class DSLTemplateWrapResponse {
    private List<DSLTemplateResponse> dslTemplatePoList;

    private String scrollId;

    public List<DSLTemplateResponse> getData() {
        return dslTemplatePoList;
    }

    public void setData(List<DSLTemplateResponse> data) {
        this.dslTemplatePoList = data;
    }

}
