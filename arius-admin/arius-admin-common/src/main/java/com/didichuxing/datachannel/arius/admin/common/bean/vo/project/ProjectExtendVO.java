package com.didichuxing.datachannel.arius.admin.common.bean.vo.project;

import java.util.List;

import com.didiglobal.knowframework.security.common.vo.project.ProjectVO;
import com.didiglobal.knowframework.security.common.vo.user.UserBriefVO;

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
    private ProjectConfigVO config;
    @ApiModelProperty(value = "是否为超级项目", dataType = "boolean", required = false)
    private Boolean           isAdmin = false;
    @ApiModelProperty(value = "持有管理员角色的项目成员", dataType = "List<UserBriefVO>", required = false)
    private List<UserBriefVO> userListWithAdminRole;
    
    @ApiModelProperty(value = "具有管理员角色和持有项目用户的项目成员", dataType = "List<UserBriefVO>", required = false)
    private List<UserBriefVO> userListWithBelongProjectAndAdminRole;
    

}