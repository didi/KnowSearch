package com.didichuxing.datachannel.arius.admin.common.bean.dto.script;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "脚本查询分页列表查询条件")
public class ScriptQueryDTO extends PageDTO {
    /**
     * 脚本id
     */
    @ApiModelProperty("脚本id")
    private Integer scriptId;
    /**
     * 脚本名
     */
    @ApiModelProperty("脚本名，模糊查询")
    private String name;

}
