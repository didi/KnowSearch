package com.didichuxing.datachannel.arius.admin.common.bean.vo.template.manage;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chengxiang
 * @date 2022/5/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "「模板管理」信息")
public class TemplateWithManageVO extends BaseVO {

    @ApiModelProperty("模板ID")
    private Integer      id;

    @ApiModelProperty("模板名称")
    private String       name;

    @ApiModelProperty("业务等级")
    private Integer      level;

    @ApiModelProperty("模板类型")
    private Integer      dataType;

    @ApiModelProperty("所属集群")
    private List<String> cluster;

    @ApiModelProperty("模板描述")
    private String       desc;
}
