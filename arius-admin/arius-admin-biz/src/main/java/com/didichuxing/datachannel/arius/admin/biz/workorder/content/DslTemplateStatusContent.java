package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DslTemplateStatusContent {

    /**
     * 名字
     */
    private String  name;

    /**
     * 项目id
     */
    private Integer projectId;

    /**
     * 操作者
     */
    private String operator;

    /**
     * dsl模板MD5
     */
    private String dslTemplateMd5;

}
