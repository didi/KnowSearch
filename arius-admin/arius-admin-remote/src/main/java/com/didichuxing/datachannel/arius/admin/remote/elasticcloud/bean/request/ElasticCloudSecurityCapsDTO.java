package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class ElasticCloudSecurityCapsDTO implements Serializable {

    private static final long serialVersionUID = -4077025508410079986L;
    /**
     * 新增特权模式集合
     */
    private List<String> add;
}
