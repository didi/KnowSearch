package com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * es集群节点的套餐
 * @author d06679
 */
@Data
@ApiModel(description = "集群节点规格信息")
public class ESClusterNodeSepcVO extends BaseDTO {

    @ApiModelProperty("主键")
    private int     id;

    @ApiModelProperty("角色")
    private String role;

    @ApiModelProperty("规格")
    private String spec;

    @ApiModelProperty("生效标识")
    private int    delete_flag;

    @ApiModelProperty("创建时间")
    private String Create_time;
}
