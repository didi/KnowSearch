package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wuxuan
 * @date 2022/11/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateLogicStatusDetail extends AbstractOrderDetail {
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
