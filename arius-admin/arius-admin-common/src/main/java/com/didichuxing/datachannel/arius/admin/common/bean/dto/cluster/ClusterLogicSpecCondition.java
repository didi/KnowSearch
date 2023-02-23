package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Authoer: zyl
 * @Date: 2022/11/15
 * @Version: 1.0
 */
@Data
@Builder()
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "逻辑集群规格信息")
public class ClusterLogicSpecCondition {

    /**
     * 节点数
     */
    private Integer hostNum;

    /**
     * 机器规格
     */
    private String machineSpec;
}
