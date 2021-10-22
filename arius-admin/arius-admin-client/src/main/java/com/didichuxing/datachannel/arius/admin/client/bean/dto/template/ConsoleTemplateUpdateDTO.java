package com.didichuxing.datachannel.arius.admin.client.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.template.DataTypeEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@ApiModel(description = "模板修改信息")
public class ConsoleTemplateUpdateDTO extends BaseDTO {

    @ApiModelProperty("索引ID")
    private Integer id;

    /**
     * 用户数据类型
     * @see DataTypeEnum
     */
    @ApiModelProperty("数据类型（1:日志；2:用户上报；3:RDS；6：离线导入）")
    private Integer dataType;

    /**
     * 模板shard数量设置
     */
    @ApiModelProperty("shard数量")
    private Integer shardNum;

    /**
     * 成本部门
     */
    @ApiModelProperty("成本部门ID")
    private String  libraDepartmentId;

    /**
     * 成本部门
     */
    @ApiModelProperty("成本部门名称")
    private String  libraDepartment;
    /**
     * 责任人
     */
    @ApiModelProperty("责任人")
    private String  responsible;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String  desc;

}
