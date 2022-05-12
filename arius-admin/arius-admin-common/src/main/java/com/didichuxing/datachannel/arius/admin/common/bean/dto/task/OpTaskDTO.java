/*
 * Copyright (c) 2015, WINIT and/or its affiliates. All rights reserved. Use, Copy is subject to authorized license.
 */
package com.didichuxing.datachannel.arius.admin.common.bean.dto.task;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpTask Vo 对象
 * 
 * @author fengqiongfeng
 * @date 2020-12-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpTaskDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    private Integer id;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("任务类型")
    private Integer taskType;

    @ApiModelProperty("业务数据主键")
    private String businessKey;

    /**
     * 任务状态
     * success:成功 failed:失败
     * running:执行中 waiting:等待
     * cancel:取消 pause:暂停
     */
    @ApiModelProperty("任务状态")
    private String status;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("标记删除 ")
    private Boolean deleteFlag;

    @ApiModelProperty("expandData")
    private String expandData;

    @ApiModelProperty("数据中心")
    private String  dataCenter;

}