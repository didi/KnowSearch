package com.didichuxing.datachannel.arius.admin.common.mapping;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.DEFAULT_INDEX_MAPPING_TYPE;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * {
 *   "type_name": {
 *     "properties": {
 *       "key1": {
 *         "type": "integer"
 *       },
 *       "key2": {
 *         "type": "long"
 *       }
 *     }
 *   }
 * }
 *
 * @author d06679
 * @date 2019-08-27
 * @see 0.3.2下线
 */
@Data
@ApiModel(description = "索引type信息")
@NoArgsConstructor
@Deprecated()
public class AriusTypeProperty {

    /**
     * type_name
     */
    @ApiModelProperty("名称")
    private String             typeName              = DEFAULT_INDEX_MAPPING_TYPE;

    /**
     * id字段
     */
    @ApiModelProperty("主键字段")
    private String             idField;

    /**
     * routing字段
     */
    @ApiModelProperty("routing字段")
    private String             routingField;

    /**
     * 时间字段
     */
    @ApiModelProperty("时间字段")
    private String             dateField;

    /**
     * 时间字段的格式
     */
    @ApiModelProperty("时间字段格式")
    private String             dateFieldFormat;

    /**
     *      {
     *       "key1": {
     *         "type": "integer"
     *       },
     *       "key2": {
     *         "type": "long"
     *       }
     *     }
     */
    @ApiModelProperty("属性（json格式）")
    private JSONObject         properties;

    /**
     * [{"key1":{}},{"key2":{}}]
     */
    @ApiModelProperty("dynamic_templates（jsonArray格式）")
    private JSONArray          dynamicTemplates;

    public static final String PROPERTIES_STR        = "properties";
    public static final String DYNAMIC_TEMPLATES_STR = "dynamic_templates";

    public JSONObject toMappingJSON() {
        JSONObject obj = new JSONObject();
        obj.put(PROPERTIES_STR, properties);

        if (null != dynamicTemplates && !dynamicTemplates.isEmpty()) {
            obj.put(DYNAMIC_TEMPLATES_STR, dynamicTemplates);
        }

        JSONObject root = new JSONObject();
        root.put(typeName, obj);

        return root;
    }
    /**
     * 它接受一个 AriusTypeProperty 对象，检查它是否有 properties 字段，如果有，它检查该 properties 字段是否有 properties 字段和 dynamicTemplates 字段。如果是，则将
     * AriusTypeProperty 对象的 properties 字段设置为 properties 字段的 properties 字段，并将 AriusTypeProperty 对象的 dynamicTemplates 字段设置为
     * properties 字段的 dynamicTemplates 字段
     *
     * @param ariusTypeProperty 包含属性和动态模板的对象。
     */
    public static AriusTypeProperty buildPropertiesAndDynamicTemplates(AriusTypeProperty ariusTypeProperty) {
        final JSONObject properties = ariusTypeProperty.getProperties();
    
        if (Objects.nonNull(properties) && properties.containsKey(PROPERTIES_STR)) {
            if (properties.containsKey(PROPERTIES_STR)) {
                ariusTypeProperty.setProperties(properties.getJSONObject(PROPERTIES_STR));
            }
            if (properties.containsKey(DYNAMIC_TEMPLATES_STR)) {
                ariusTypeProperty.setDynamicTemplates(properties.getJSONArray(DYNAMIC_TEMPLATES_STR));
            }
            
        }
        return ariusTypeProperty;
    }
}