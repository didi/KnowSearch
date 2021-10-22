package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ElasticCloudSecurityCapsDTO {

    /**
     * 新增特权模式集合
     */
    private List<String> add;
}
