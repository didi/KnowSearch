package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

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
@ApiModel(description = "索引基本信息（包含配额利用信息）")
public class ConsoleTemplateSampleVO extends ConsoleTemplateVO {

    @ApiModelProperty("所属应用名称")
    private String     appName;

}
