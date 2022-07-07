package com.didichuxing.datachannel.arius.admin.common.mapping;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-04
 */
@Data
@ApiModel(description = "索引mapping字段信息")
public class Field {

    @ApiModelProperty("字段名称")
    private String  name;

    @ApiModelProperty("字段类型")
    private String  type;

    @ApiModelProperty("索引类型（1:关闭分词，关闭倒排索引；2:关闭分词，开启倒排索引；3:开启分词，开启倒排索引）")
    private Integer indexType;

    @ApiModelProperty("分词类型（1:标准分词；2:ik分词）")
    private Integer analyzerType;

    @ApiModelProperty("排序类型（0:不排序；1:排序）")
    private Integer sortType;

    @ApiModelProperty("是否是分区字段")
    private Boolean dateField;

    @ApiModelProperty("分区字段格式")
    private String  dateFieldFormat;

    @ApiModelProperty("是否是主键子弹")
    private Boolean idField;

    @ApiModelProperty("是否是routing字段")
    private Boolean routingField;

    public boolean esTypeEquals(Object o) {
        if (o == null || !(o instanceof Field)) {
            return false;
        }

        Field field = (Field) o;


        if(!isEqual(type, field.type)) {
            return false;
        }

        if(!isEqual(indexType, field.indexType)) {
            return false;
        }

        if(!isEqual(analyzerType, field.analyzerType)) {
            return false;
        }

        return isEqual(sortType, field.sortType);
    }

    private boolean isEqual(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }

        return o1.equals(o2);
    }
}
