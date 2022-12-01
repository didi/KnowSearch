package com.didichuxing.datachannel.arius.admin.common.bean.dto.app;

import com.didiglobal.knowframework.security.common.dto.project.ProjectQueryDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目查询扩展dto
 *
 * @author shizeying
 * @date 2022/06/13
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "项目查找条件扩展信息")
public class ProjectQueryExtendDTO extends ProjectQueryDTO {
    @ApiModelProperty(value = "查询模式（0:集群模式；1:索引模式）", dataType = "Integer", required = false)
    private Integer searchType;
}