package com.didichuxing.datachannel.arius.admin.client.bean.common;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/5/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模板type信息")
public class TemplateType {

    @ApiModelProperty("type名称")
    private String       name;

    @ApiModelProperty("主键字段")
    private String       idField;

    @ApiModelProperty("routing字段")
    private String       routing;

    @ApiModelProperty("source是否存储")
    private Boolean      source;

    @ApiModelProperty("主键字段列表")
    private List<String> idFieldList;

    public void setIdField(String idField) {
        this.idField = idField;
        if (StringUtils.isNotBlank(idField)) {
            idFieldList = Lists.newArrayList(idField.split(","));
        }
    }
}
