package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author linyunan
 * @date 2021-03-16
 */
@Data
@ApiModel(description = "携带App权限信息的物理模板信息")
public class ConsoleTemplatePhyVO extends IndexTemplatePhysicalVO {

    @ApiModelProperty("归属项目ID")
    private Integer appId;

    @ApiModelProperty("归属项目名称")
    private String  appName;

    /**
     * @see AppTemplateAuthEnum
     */
    @ApiModelProperty("当前App拥有的权限类型（-1 无权限 ;1:管理；2:读写；3:读）")
    private Integer authType;
}
