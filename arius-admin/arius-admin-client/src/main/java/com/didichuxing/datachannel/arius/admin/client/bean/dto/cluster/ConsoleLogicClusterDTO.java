package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Console 逻辑集群信息
 * @author d06679
 * @date 2019/3/22
 */
@Data
@ApiModel(description = "集群信息")
public class ConsoleLogicClusterDTO extends BaseDTO {

    /**
     * 主键
     */
    @ApiModelProperty("集群ID")
    private Long   id;

    /**
     * 责任人
     */
    @ApiModelProperty("责任人")
    private String responsible;

    /**
     * 成本部门
     */
    @ApiModelProperty("成本部门ID")
    private String libraDepartmentId;

    /**
     * 成本部门
     */
    @ApiModelProperty("成本部门名称")
    private String libraDepartment;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String memo;

}
