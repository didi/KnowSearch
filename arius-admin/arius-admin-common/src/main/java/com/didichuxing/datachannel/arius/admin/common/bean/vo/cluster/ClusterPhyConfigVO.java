package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.OpManagerConfigVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "clusterPhy 配置信息")
public class ClusterPhyConfigVO extends OpManagerConfigVO {
    @ApiModelProperty("节点列表")
    private List<ESClusterRoleHostVO> nodes;
}