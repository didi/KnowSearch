/*
 * Copyright (c) 2015, WINIT and/or its affiliates. All rights reserved. Use, Copy is subject to authorized license.
 */
package com.didichuxing.datachannel.arius.admin.common.bean.po.feedback;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.Data;

import java.util.Date;

/**
 * AriusUserFeedback 实体类
 * 
 * @author fengqiongfeng
 * @date 2020-12-29
 */
@Data
public class AriusUserFeedbackPO extends BasePO {

    /**
     * 用户反馈信息 
     */
    private String feedback;

    /**
     * 反馈人 
     */
    private String creator;

    /**
     * 创建时间 
     */
    private Date   createTime;

}
