package com.didichuxing.datachannel.arius.admin.client.bean.vo.operaterecord;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/14
 */
@Data
public class OperateRecordVO extends BaseVO {

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

    @ApiModelProperty("模块")
    private String  module;

    /**
     * @see OperationEnum
     */
    @ApiModelProperty("操作ID")
    private Integer operateId;

    @ApiModelProperty("操作")
    private String  operate;

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

}
