package com.didichuxing.datachannel.arius.admin.common.bean.vo.script;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "查询脚本名称-提供给新增安装包下拉框")
public class ScriptListVO {
    /**
     * 脚本id
     */
    @ApiModelProperty("脚本id")
    private Integer id;
    /**
     * 脚本名
     */
    @ApiModelProperty("脚本名")
    private String name;
}
