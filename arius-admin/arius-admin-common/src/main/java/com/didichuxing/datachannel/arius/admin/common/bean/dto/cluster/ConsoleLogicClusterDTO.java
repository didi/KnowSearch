package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Console 逻辑集群信息
 * @author d06679
 * @date 2019/3/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "集群信息")
public class ConsoleLogicClusterDTO extends BaseDTO {

    @ApiModelProperty("逻辑集群ID")
    private Long   id;

    @ApiModelProperty("责任人")
    private String responsible;

    @ApiModelProperty("成本部门ID")
    private String libraDepartmentId;

    @ApiModelProperty("成本部门名称")
    private String libraDepartment;

    @ApiModelProperty("备注")
    private String memo;

}
