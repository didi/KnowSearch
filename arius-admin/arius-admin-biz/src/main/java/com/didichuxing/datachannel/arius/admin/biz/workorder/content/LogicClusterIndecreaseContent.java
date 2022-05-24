package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class LogicClusterIndecreaseContent extends BaseContent {
    /**
     * 逻辑集群名称
     */
    private String logicClusterName;

    /**
     * 逻辑集群的id
     */
    private Long   logicClusterId;

    /**
     * dataNode的规格
     */
    private String dataNodeSpec;

    /**
     * dataNode的个数
     */
    private int    dataNodeNu;
    /**
     * 备注
     */
    private String memo;

    /**
     * 扩缩容的region信息
     */
    private List<ClusterRegionWithNodeInfoDTO> regionWithNodeInfo;

}
