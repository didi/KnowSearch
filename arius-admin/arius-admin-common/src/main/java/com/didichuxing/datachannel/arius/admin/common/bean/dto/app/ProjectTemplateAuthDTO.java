package com.didichuxing.datachannel.arius.admin.common.bean.dto.app;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;
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
@ApiModel(description ="应用权限信息")
@AllArgsConstructor
@NoArgsConstructor
public class ProjectTemplateAuthDTO extends BaseDTO {

    @ApiModelProperty("ID")
    private Long    id;

    @ApiModelProperty("应用ID")
    private Integer projectId;

    @ApiModelProperty("模板ID")
    private Integer templateId;

    /**
     * @see ProjectTemplateAuthEnum
     */
    @ApiModelProperty("权限类型（1:管理；2:读写；3:读）")
    private Integer type;

    @ApiModelProperty("责任人：后续进行下线，无需再使用")
    @Deprecated
    private String  responsible;

}