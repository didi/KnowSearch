package com.didichuxing.datachannel.arius.admin.client.bean.dto.app;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/4/16
 */
@Data
@ApiModel(description = "应用逻辑集群权限信息")
@AllArgsConstructor
@NoArgsConstructor
public class AppLogicClusterAuthDTO extends BaseDTO {
    /**
     * 主键
     */
    @ApiModelProperty("ID")
    private Long id;

    /**
     * 应用ID
     */
    @ApiModelProperty("应用ID")
    private Integer appId;

    /**
     * 逻辑集群ID
     */
    @ApiModelProperty("逻辑集群ID")
    private Long logicClusterId;

    /**
     * 权限类型  读写  读
     * @see com.didichuxing.datachannel.arius.admin.client.constant.app.AppLogicClusterAuthEnum
     */
    @ApiModelProperty("权限类型，0：超管权限，1：配置管理权限，2：访问权限")
    private Integer type;

    /**
     * 责任人列表
     */
    @ApiModelProperty("责任人")
    private String responsible;

}
