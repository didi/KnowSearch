package com.didichuxing.datachannel.arius.admin.client.bean.dto.oprecord;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
public class OperateRecordDTO extends BaseDTO {

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
     * @see OperationEnum
     */
    @ApiModelProperty("操作ID")
    private Integer operateId;

    /**
     * 操作业务id String类型
     */
    @ApiModelProperty("业务ID")
    private String  bizId;

    /**
     * 操作描述
     */
    @ApiModelProperty("操作内容")
    private String  content;

    /**
     * 操作人  邮箱前缀
     */
    @ApiModelProperty("操作人")
    private String  operator;

    /**
     * 操作时间
     */
    @ApiModelProperty("操作时间")
    private Date    operateTime;

    /**
     * 操作起始时间 查询使用
     */
    @ApiModelProperty("开始时间")
    private Date    beginTime;

    /**
     * 操作截止时间 查询使用
     */
    @ApiModelProperty("结束时间")
    private Date    endTime;

}
