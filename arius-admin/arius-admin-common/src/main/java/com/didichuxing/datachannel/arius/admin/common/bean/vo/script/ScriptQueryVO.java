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
@ApiModel(description = "脚本信息")
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
     * 内容地址
     */
    @ApiModelProperty("内容地址")
    private String contentUrl;
    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String describe;
    /**
     * 模板超时时间
     */
    @ApiModelProperty("模板超时时间")
    private Integer timeout;
}
