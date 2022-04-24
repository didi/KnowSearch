package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateOperateRecord {

    /**
     * 模板操作类型，如mapping, setting, etc
     */
    protected Integer operateType;

    /**
     * 模板操作描述
     */
    protected String operateDesc;
}
