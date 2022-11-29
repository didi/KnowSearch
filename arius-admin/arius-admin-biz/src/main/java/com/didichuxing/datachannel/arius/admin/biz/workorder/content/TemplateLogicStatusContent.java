package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TemplateLogicStatusContent {
    /**
     * 索引模板名称
     */
    private String name;
    /**
     * 模板Id
     */
    private Integer templateId;
    /**
     * 读/写的启用/禁用状态
     */
    private Boolean status;
    /**
     * 操作者
     */
    private String operator;
    /**
     * 项目id
     */
    private Integer projectId;
}
