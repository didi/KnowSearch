package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linyunan
 * @date 2021-03-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "携带App权限信息的物理模板信息")
public class ConsoleTemplatePhyVO extends IndexTemplatePhysicalVO {

    @ApiModelProperty("归属项目ID")
    private Integer projectId;

    @ApiModelProperty("归属项目名称")
    private String projectName;

    /**
     * @see ProjectTemplateAuthEnum
     */
    @ApiModelProperty("当前App拥有的权限类型（-1 无权限 ;1:管理；2:读写；3:读）")
    private Integer authType;
}