package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Created by linyunan on 2021-06-08
 */
@Data
@Builder
public class ESClusterPhyContext {

    private Long         clusterPhyId;

    private String       clusterName;

    /*****************************************clusterLogicInfo***********************************/

    private Integer      associatedLogicNum;

    private Integer      associatedLogicNumMax;

    private List<Long>   associatedClusterLogicIds;

    /*****************************************clusterPhyNodeInfo***********************************/

    private Integer      associatedDataNodeNum;

    private List<String> associatedDataNodeIps;

    /*****************************************regionInfo********************************************/

    private List<Long>   associatedRegionIds;
}
