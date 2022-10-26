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
@ApiModel(description = "脚本新增信息")
public class ScriptAddDTO {
    /**
     * 脚本名
     */
    @ApiModelProperty("脚本名")
    private String name;
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
}
