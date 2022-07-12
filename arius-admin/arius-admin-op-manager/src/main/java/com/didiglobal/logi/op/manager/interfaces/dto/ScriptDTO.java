package com.didiglobal.logi.op.manager.interfaces.dto;

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
public class ScriptDTO {
    /**
     * 脚本id
     */
    private Integer id;
    /**
     * 脚本名
     */
    private String name;
    /**
     * 模板id
     */
    private String templateId;
    /**
     * 内容地址
     */
    private String contentUrl;
    /**
     * 描述
     */
    private String describe;
    /**
     * 传输文件
     */
    private MultipartFile uploadFile;
}
