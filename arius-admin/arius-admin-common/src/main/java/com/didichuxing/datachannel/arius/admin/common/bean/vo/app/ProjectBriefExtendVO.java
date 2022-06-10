package com.didichuxing.datachannel.arius.admin.common.bean.vo.app;

import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目简要扩展签证官:加入项目配置相关信息
 *
 * @author shizeying
 * @date 2022/06/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "项目简要信息（包含项目配置）")
public class ProjectBriefExtendVO extends ProjectBriefVO {
	@ApiModelProperty(value = "项目配置", dataType = "ProjectSaveDTO", required = false)
	private ProjectConfigVo config;
	@ApiModelProperty(value = "是否为超级项目", dataType = "boolean", required = false)
	private Boolean isAdmin;
}