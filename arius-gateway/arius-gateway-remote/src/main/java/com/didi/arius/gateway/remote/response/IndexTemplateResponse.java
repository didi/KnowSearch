
package com.didi.arius.gateway.remote.response;

import com.google.gson.annotations.Expose;
import lombok.Data;

import java.util.List;

@Data
public class IndexTemplateResponse {

    @Expose
    private BaseInfoResponse baseInfo;
    @Expose
    private MasterInfoResponse masterInfo;
    @Expose
    private List<SlaveInfoResponse> slaveInfos;

}
