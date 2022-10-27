package com.didichuxing.datachannel.arius.admin.common.bean.vo.script;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "查询列表脚本信息")
public class ScriptQueryVO extends BaseVO {
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
    /**
     * 模板id
     */
    @ApiModelProperty("模板id")
    private String templateId;
    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String describe;
    /**
     * 创建者
     */
    @ApiModelProperty("创建者")
    private String creator;
}
