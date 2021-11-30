package com.didichuxing.datachannel.arius.admin.client.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.template.DataTypeEnum;

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
@ApiModel(description = "模板修改信息")
public class ConsoleTemplateUpdateDTO extends BaseDTO {

    @ApiModelProperty("索引ID")
    private Integer id;

    /**
     * @see DataTypeEnum
     */
    @ApiModelProperty("数据类型（0:系统 1:日志；2:用户上报；3:RDS；6：离线导入）")
    private Integer dataType;

    @ApiModelProperty("shard数量")
    private Integer shardNum;

    @ApiModelProperty("成本部门ID")
    private String  libraDepartmentId;

    @ApiModelProperty("成本部门名称")
    private String  libraDepartment;

    @ApiModelProperty("责任人")
    private String  responsible;

    @ApiModelProperty("备注")
    private String  desc;

}
