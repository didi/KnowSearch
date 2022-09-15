package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import lombok.Builder;
import lombok.Data;

/**
 * 物理集群和逻辑集群对应关系
 *
 * @author shizeying
 * @date 2022/09/15
 */
@Data
@Builder
public class ClusterPhyWithLogicClusterVO {
	private String clusterPhy;
	private String clusterLogic;
	private Long   clusterLogicId;
}