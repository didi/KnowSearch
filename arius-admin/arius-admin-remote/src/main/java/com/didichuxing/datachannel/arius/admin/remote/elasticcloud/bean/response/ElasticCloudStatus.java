package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
