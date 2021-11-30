package com.didichuxing.datachannel.arius.admin.client.bean.vo.operaterecord;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperateRecordVO extends BaseVO {

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

    @ApiModelProperty("业务ID")
    private String  bizId;

    @ApiModelProperty("操作内容")
    private String  content;

    /**
     * 操作人  邮箱前缀
     */
    @ApiModelProperty("操作人")
    private String  operator;

    @ApiModelProperty("操作时间")
    private Date    operateTime;

}
