package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateSchemaVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liuchengxiang
 * @date 2022/2/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateMappingRecord {

    /**
     * 旧的mapping 设置
     */
    private ConsoleTemplateSchemaVO oldMapping;

    /**
     * 新的mapping 设置
     */
    private ConsoleTemplateSchemaVO newMapping;
}
