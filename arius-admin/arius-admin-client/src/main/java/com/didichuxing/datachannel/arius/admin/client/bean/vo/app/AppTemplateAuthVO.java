package com.didichuxing.datachannel.arius.admin.client.bean.vo.app;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;
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
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "应用权限信息")
public class AppTemplateAuthVO extends BaseVO {
    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("应用ID")
    private Integer appId;

    @ApiModelProperty("模板ID")
    private Integer templateId;

    @ApiModelProperty("模板名称")
    private String templateName;

    /**
     * 权限类型  读写  读
     * @see AppTemplateAuthEnum
     */
    @ApiModelProperty("权限类型（-1 无权限 ;1:管理；2:读写；3:读）")
    private Integer type;

    @ApiModelProperty("所属逻辑集群ID")
    private Long logicClusterId;

    @ApiModelProperty("所属逻辑集群名称")
    private String logicClusterName;

    @ApiModelProperty("权限责任人")
    private String responsible;
}
