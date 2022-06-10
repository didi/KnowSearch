package com.didichuxing.datachannel.arius.admin.common.bean.vo.app;

import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * projectvo 的扩展类：扩展logi侧projectvo
 *
 * @author shizeying
 * @date 2022/06/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "项目信息（包含项目配置）和是否为超级项目")
public class ProjectExtendVO extends ProjectVO {
	@ApiModelProperty(value = "项目配置", dataType = "ProjectSaveDTO", required = false)
	private ProjectConfigVo config;
	@ApiModelProperty(value = "是否为超级项目", dataType = "boolean", required = false)
	private Boolean isAdmin;
	
}