package com.didichuxing.datachannel.arius.admin.client.bean.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/5/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "标签信息")
public class Label {

    @ApiModelProperty("标签ID")
    private String labelId;

    @ApiModelProperty("标签名字")
    private String labelName;

    @ApiModelProperty("标签等级(green/yellow/red)")
    private String level;

    public String getLevel() {
        String levelCode = labelId.substring(2, 3);
        if ("3".equals(levelCode)) {
            return "red";
        }

        if ("2".equals(levelCode)) {
            return "yellow";
        }

        return "green";
    }
}
