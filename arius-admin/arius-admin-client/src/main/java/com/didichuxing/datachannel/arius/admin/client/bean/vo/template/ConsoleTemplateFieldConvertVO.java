package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@ApiModel(description = "索引mapping转换信息")
public class ConsoleTemplateFieldConvertVO extends BaseVO {

    /**
     * 时间字段
     */
    @ApiModelProperty("时间字段")
    private String dateField;


    /**
     * 时间字段的格式
     */
    @ApiModelProperty("时间字段格式")
    private String dateFieldFormat;

    /**
     * mapping信息 mapping导入的
     *
     *      {
     *       "key1": {
     *         "type": "integer"
     *       },
     *       "key2": {
     *         "type": "long"
     *       }
     *     }
     *
     */
    @ApiModelProperty("mapping信息")
    private String mapping;

    /**
     * id地钻
     */
    @ApiModelProperty("主键子弹")
    private String idField;

    /**
     * routing字段
     */
    @ApiModelProperty("routing字段")
    private String routingField;

}
