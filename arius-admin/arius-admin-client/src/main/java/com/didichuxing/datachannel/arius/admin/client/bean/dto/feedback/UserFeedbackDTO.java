/*
 * Copyright (c) 2015, WINIT and/or its affiliates. All rights reserved. Use, Copy is subject to authorized license.
 */
package com.didichuxing.datachannel.arius.admin.client.bean.dto.feedback;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 *
 * @author fengqiongfeng
 * @date 2020-12-29
 */
@Data
@ApiModel(description = "用户反馈")
public class UserFeedbackDTO extends BaseDTO {

    /**
     * 用户反馈信息 
     */
    @ApiModelProperty("集群Id")
    private String feedback;

    /**
     * 反馈人 
     */
    private String creator;


}
