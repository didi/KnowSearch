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
@Deprecated
public class ClusterLogicContext {

    /*****************************************clusterLogicInfo*************************************/

    private Long         clusterLogicId;

    private String       clusterLogicName;

    private Integer      logicClusterType;

    /*****************************************clusterPhyInfo***************************************/

    private Integer      associatedPhyNumMax;

    private Integer      associatedPhyNum;

    private List<String> associatedClusterPhyNames;

    /*****************************************clusterPhyNodeInfo***********************************/

    private Integer      associatedDataNodeNum;

    private List<String> associatedDataNodeIps;

    /*****************************************regionInfo********************************************/

    private List<Long>   associatedRegionIds;
}