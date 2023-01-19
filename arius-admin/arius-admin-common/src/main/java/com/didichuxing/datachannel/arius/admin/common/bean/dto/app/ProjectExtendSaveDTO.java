package com.didichuxing.datachannel.arius.admin.common.bean.dto.app;

import com.didiglobal.knowframework.security.common.dto.project.ProjectSaveDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目扩展dto：包含Logi中projectSaveDto和knowsearch中的projectConfigDto
 *
 * @author shizeying
 * @date 2022/06/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "项目保存dto")
public class ProjectExtendSaveDTO {
    @ApiModelProperty(value = "项目", dataType = "ProjectSaveDTO", required = false)
    private ProjectSaveDTO   project;
    @ApiModelProperty(value = "项目配置", dataType = "ProjectSaveDTO", required = false)
    private ProjectConfigDTO config;
}