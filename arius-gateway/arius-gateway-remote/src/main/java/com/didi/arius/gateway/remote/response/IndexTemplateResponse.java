
package com.didi.arius.gateway.remote.response;

import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class IndexTemplateResponse {

    @Expose
    private BaseInfoResponse baseInfo;
    @Expose
    private MasterInfoResponse masterInfo;
    @Expose
    private List<SlaveInfoResponse> slaveInfos;

}
