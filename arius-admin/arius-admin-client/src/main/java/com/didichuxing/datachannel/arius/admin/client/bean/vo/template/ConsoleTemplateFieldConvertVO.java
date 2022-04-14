package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "索引mapping转换信息")
public class ConsoleTemplateFieldConvertVO extends BaseVO {

    @ApiModelProperty("时间字段")
    private String dateField;

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

    @ApiModelProperty("routing字段")
    private String routingField;

}
