package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import lombok.Data;

import java.util.List;

/**
 * 绑定物理集群gateways.
 *
 * @ClassName BindGatewayClusterDTO
 * @Author gyp
 * @Date 2023/1/5
 * @Version 1.0
 */
@Data
public class BindGatewayClusterDTO {
    private Integer clusterPhyId;
    private List<Integer> gatewayClusterIds;
}