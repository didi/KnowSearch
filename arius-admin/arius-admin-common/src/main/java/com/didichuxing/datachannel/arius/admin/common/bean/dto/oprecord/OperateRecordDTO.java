package com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.OperateRecordSortEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author d06679
 * @date 2019/3/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "操作记录信息")
public class OperateRecordDTO extends PageDTO {

    /**
     * 主键
     */
    @ApiModelProperty("记录ID")
    private Integer id;

    /**
     * @see ModuleEnum
     */
    @ApiModelProperty("模块ID")
    private Integer moduleId;

    /**
     * @see OperateTypeEnum
     */
    @ApiModelProperty(value = "操作ID")
    private Integer operateId;

    /**
     * 操作描述
     */
    @ApiModelProperty("操作内容")
    private String  content;

    /**
     * 操作人  邮箱前缀
     */
    @ApiModelProperty("操作人")
    private String  userOperation;

    /**
     * 操作起始时间 查询使用
     */
    @ApiModelProperty(value = "开始时间", dataType = "Date")
    private Date    beginTime;

    /**
     * 操作截止时间 查询使用
     */
    @ApiModelProperty(value = "结束时间", dataType = "Date")
    private Date    endTime;

    /**
     * 触发方式
     * @see com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum
     */
    @ApiModelProperty("触发方式")
    private Integer triggerWayId;
    /**
     * 应用id
     */
    @ApiModelProperty("项目")
    private String  projectName;
    @ApiModelProperty("bizId")
    private String  bizId;
    /**
     * @see OperateRecordSortEnum
     */
    @ApiModelProperty(value = "排序字段", example = "id或者operateTime")
    private String  sortTerm;

    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean", required = false)
    private Boolean orderByDesc = true;
    @ApiModelProperty(hidden = true)
    private String  sortType;
}