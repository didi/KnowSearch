package com.didiglobal.logi.op.manager.interfaces.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author didi
 * @date 2022-07-06 2:41 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "脚本条件查询信息")
public class ScriptDTO {
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
     * 传输文件
     */
    @ApiModelProperty("传输文件")
    private MultipartFile uploadFile;
    /**
     * 模板超时时间
     */
    @ApiModelProperty("模板超时时间")
    private Integer timeOut;
}
