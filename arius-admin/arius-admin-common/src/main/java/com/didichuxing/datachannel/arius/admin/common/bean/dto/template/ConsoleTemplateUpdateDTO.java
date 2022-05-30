package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679, chengxiang
 * @date 2019/3/29
 * @update 2022/5/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模板修改信息")
public class ConsoleTemplateUpdateDTO extends BaseDTO {

    @ApiModelProperty("模板ID")
    private Integer id;

    @ApiModelProperty("数据类型（0:系统 1:日志；2:用户上报；3:RDS；6：离线导入）")
    private Integer dataType;

    @ApiModelProperty("责任人")
    private String  responsible;

    @ApiModelProperty("备注")
    private String  desc;

}
