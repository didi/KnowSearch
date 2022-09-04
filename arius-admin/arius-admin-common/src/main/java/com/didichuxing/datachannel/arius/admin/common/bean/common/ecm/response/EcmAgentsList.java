package com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response;

import lombok.Data;

import java.util.List;

@Data
public class EcmAgentsList {
    private List<EcmDat> dats;

    private Integer total;
}
