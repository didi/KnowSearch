package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateSchemaVO;
import com.didichuxing.datachannel.arius.admin.client.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.common.constant.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;

/**
 * @author liuchengxiang
 * @date 2022/2/17
 */
@Data
@NoArgsConstructor
public class TemplateSchemaOperateRecord extends TemplateOperateRecord {

    /**
     * 旧的schema 设置
     */
    private ConsoleTemplateSchemaVO oldSchema;

    /**
     * 新的schema 设置
     */
    private ConsoleTemplateSchemaVO newSchema;

    private boolean isMappingChanged = true;

    private boolean isDynamicTemplatesChanged = true;

    public TemplateSchemaOperateRecord(ConsoleTemplateSchemaVO oldSchema, ConsoleTemplateSchemaVO newSchema) {
        this.oldSchema = oldSchema;
        this.newSchema = newSchema;
        this.operateType = TemplateOperateRecordEnum.MAPPING.getCode();

        List<AriusTypeProperty> oldTypeProperties = oldSchema.getTypeProperties();
        List<AriusTypeProperty> newTypeProperties = newSchema.getTypeProperties();

        if (oldTypeProperties != null && newTypeProperties != null) {
            //确保长度相等且一一对应
            if (oldTypeProperties.size() != newTypeProperties.size()) {
                return;
            }
            oldTypeProperties.sort(Comparator.comparing(AriusTypeProperty::getTypeName));
            newTypeProperties.sort(Comparator.comparing(AriusTypeProperty::getTypeName));

            //保证所有对应的mapping & dynamic_templates 都没有变动，否则认为有改变
            isMappingChanged = false;
            isDynamicTemplatesChanged = false;
            for (int i=0; i<oldTypeProperties.size(); i++) {
                AriusTypeProperty oldTypeProperty = oldTypeProperties.get(i);
                AriusTypeProperty newTypeProperty = newTypeProperties.get(i);
                if (AriusObjUtils.isChanged(oldTypeProperty.getProperties(), newTypeProperty.getProperties())) {
                    isMappingChanged = true;
                }
                if (AriusObjUtils.isChanged(oldTypeProperty.getDynamicTemplates(), newTypeProperty.getDynamicTemplates())) {
                    isDynamicTemplatesChanged = true;
                }
            }
        }
    }
}
