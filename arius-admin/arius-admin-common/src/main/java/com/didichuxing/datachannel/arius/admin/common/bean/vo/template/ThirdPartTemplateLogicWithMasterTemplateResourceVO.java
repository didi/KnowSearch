package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "逻辑模板信息，携带master所在的逻辑集群信息")
public class ThirdPartTemplateLogicWithMasterTemplateResourceVO extends ThirdpartTemplateLogicVO {

    @ApiModelProperty("master模板")
    private IndexTemplatePhysicalVO masterTemplate;

    @ApiModelProperty("master所在的逻辑集群信息")
    private ClusterLogicVO masterResource;

}
