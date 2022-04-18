package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.request;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 创建弹性云参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ElasticCloudCreateParamDTO extends BaseDTO {
    /**
     * 集群名称
     */
    private  String   name;

    /**
     * 集群场景
     */
    private  String   scene;

    /**
     * 容器信息
     */
    private ElasticCloudSpecInfoDTO specInfo;

    /**
     * 是否启用无ip池模式创建集群/false 无  true 有
     */
    private boolean   disableIpQuota;

    /**
     * 是否打开特权模式
     */
    private boolean   privileged;

    /**
     * 新增哪些特权模式
     */
    private ElasticCloudSecurityCapsDTO securityCaps;

    /**
     * 创建时的容器个数
     */
    private  int   podCount;

    /**
     * 是否启用LB端口
     */
    private boolean   useNodePort;
}
