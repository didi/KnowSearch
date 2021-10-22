package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Created by linyunan on 2021-06-08
 */
@Data
@Builder
public class ESClusterLogicContext {

    /*****************************************clusterLogicInfo*************************************/

    private Long          clusterLogicId;

    private Integer       logicClusterType;

    /*****************************************clusterPhyInfo***************************************/

    private Integer       associatedPhyNumMax;

    private Integer       associatedPhyNum;

    private List<String>  associatedClusterPhyNames;

    /*****************************************clusterPhyNodeInfo***********************************/

    private Integer       associatedDataNodeNum;

    private List<String>  associatedDataNodeIps;

    /*****************************************regionInfo********************************************/

    private List<Long> associatedRegionIds;
}
