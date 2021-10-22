package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class ElasticCloudStatus implements Serializable {

    /**
     * 版本号
     */
    private String apiVersion ;

    /**
     * 任务状态
     */
    private ElasticCloudAppStatus status;
}
