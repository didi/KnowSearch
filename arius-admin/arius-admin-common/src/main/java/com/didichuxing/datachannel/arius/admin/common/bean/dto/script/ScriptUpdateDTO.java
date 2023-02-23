package com.didichuxing.datachannel.arius.admin.common.bean.dto.script;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "脚本更新信息")
public class ScriptUpdateDTO {
    /**
     * 脚本id
     */
    @ApiModelProperty("脚本id")
    private Integer id;
    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String describe;
    /**
     * 传输文件
     */
    @ApiModelProperty("传输文件")
    private MultipartFile uploadFile;
    /**
     * 模板超时时间
     */
    @ApiModelProperty("模板超时时间")
    private Integer timeout;
}
