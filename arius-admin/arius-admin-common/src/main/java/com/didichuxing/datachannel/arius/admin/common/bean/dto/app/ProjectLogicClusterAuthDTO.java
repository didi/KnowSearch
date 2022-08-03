package com.didichuxing.datachannel.arius.admin.common.bean.dto.app;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
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
public class ProjectLogicClusterAuthDTO extends BaseDTO {

    @ApiModelProperty("ID")
    private Long    id;

    @ApiModelProperty("应用ID")
    private Integer projectId;

    @ApiModelProperty("逻辑集群ID")
    private Long    logicClusterId;

    /**
     * @see ProjectClusterLogicAuthEnum
     */
    @ApiModelProperty("权限类型，0：超管权限，1：配置管理权限，2：访问权限")
    private Integer type;

    

}