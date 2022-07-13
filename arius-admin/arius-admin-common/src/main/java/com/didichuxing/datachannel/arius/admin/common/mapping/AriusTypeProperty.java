package com.didichuxing.datachannel.arius.admin.common.mapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
 */
@Data
@ApiModel(description = "索引type信息")
public class AriusTypeProperty {

    /**
     * type_name
     */
    @ApiModelProperty("名称")
    private String             typeName              = "type";

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
}
