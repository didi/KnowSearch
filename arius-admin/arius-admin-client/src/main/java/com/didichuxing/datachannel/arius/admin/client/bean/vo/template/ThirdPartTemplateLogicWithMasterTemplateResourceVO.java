package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 */
@Data
@ApiModel(description = "逻辑模板信息，携带master所在的逻辑集群信息")
public class ThirdPartTemplateLogicWithMasterTemplateResourceVO extends ThirdpartTemplateLogicVO {

    /**
     * master模板
     */
    @ApiModelProperty("master模板")
    private IndexTemplatePhysicalVO masterTemplate;

    /**
     * master所在的逻辑集群信息
     */
    @ApiModelProperty("master所在的逻辑集群信息")
    private ConsoleClusterVO masterResource;

}
