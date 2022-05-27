package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-06-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterPhyContext {

    private Long         clusterPhyId;

    private String       clusterName;

    /*****************************************appId***********************************/
    private List<String> associatedProjectNames;

    private List<Integer> associatedProjectIds;

    /*****************************************clusterLogicInfo***********************************/

    private Integer      associatedLogicNum;

    private Integer      associatedLogicNumMax;

    private List<Long>   associatedClusterLogicIds;

    /*****************************************clusterPhyNodeInfo***********************************/

    private Integer      associatedDataNodeNum;

    private List<String> associatedDataNodeIps;

    private List<String> associatedNodeIps;

    /*****************************************regionInfo********************************************/

    private List<Long>   associatedRegionIds;
}