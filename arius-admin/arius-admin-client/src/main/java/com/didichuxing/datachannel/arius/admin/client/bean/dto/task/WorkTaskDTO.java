/*
 * Copyright (c) 2015, WINIT and/or its affiliates. All rights reserved. Use, Copy is subject to authorized license.
 */
package com.didichuxing.datachannel.arius.admin.client.bean.dto.task;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * WorkTask Vo 对象
 * 
 * @author fengqiongfeng
 * @date 2020-12-21
 */
@Data
public class WorkTaskDTO extends BaseDTO {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Integer id;

    /**
     * 标题 
     */
    @ApiModelProperty("标题")
    private String title;

    /**
     * 任务类型
     */
    @ApiModelProperty("任务类型")
    private Integer taskType;

    /**
     * 业务数据主键 
     */
    @ApiModelProperty("业务数据主键")
    private Integer businessKey;

    /**
     * 任务状态
     * success:成功 failed:失败
     * running:执行中 waiting:等待
     * cancel:取消 pause:暂停
     */
    @ApiModelProperty("任务状态")
    private String status;

    /**
     * 创建人 
     */
    @ApiModelProperty("创建人")
    private String creator;

    /**
     * 创建时间 
     */
    @ApiModelProperty("创建时间")
    private Date createTime;

    /**
     * 更新时间 
     */
    @ApiModelProperty("更新时间")
    private Date updateTime;

    /**
     * 标记删除 
     */
    @ApiModelProperty("标记删除 ")
    private Boolean deleteFlag;

    /**
     * expandData 
     */
    @ApiModelProperty("expandData")
    private String expandData;

    /**
     * 数据中心
     */
    @ApiModelProperty("数据中心")
    private String  dataCenter;

}

