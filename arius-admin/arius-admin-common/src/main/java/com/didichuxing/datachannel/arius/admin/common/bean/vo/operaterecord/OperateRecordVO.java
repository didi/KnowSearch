package com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
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

    /**
    * 主键
    */
    @ApiModelProperty("记录ID")
    private Integer id;

    /**
     * @see ModuleEnum
     */
    @ApiModelProperty("模块")
    private String  module;

    /**
     * @see OperateTypeEnum
     */
    @ApiModelProperty("操作ID")
    private String  operate;

    /**
     * 操作描述
     */
    @ApiModelProperty("操作内容")
    private String  content;

    /**
     * 操作人
     */
    @ApiModelProperty("操作人")
    private String  userOperation;

    /**
     * 操作时间
     */
    @ApiModelProperty("操作时间")
    private Date    operateTime;

    /**
     * 触发方式
     * @see com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum
     */
    @ApiModelProperty("操作方式")
    private String  triggerWay;
    /**
     * 应用id
     */
    @ApiModelProperty("项目")
    private String  projectName;
    /**
     * 应用id
     */
    @ApiModelProperty("资源所属应用")
    private String  operateProjectName;
}