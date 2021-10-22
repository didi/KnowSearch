/*
 * Copyright (c) 2015, WINIT and/or its affiliates. All rights reserved. Use, Copy is subject to authorized license.
 */
package com.didichuxing.datachannel.arius.admin.common.bean.po.arius;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * AriusUserLoginRecord 实体类
 * 
 * @author fengqiongfeng
 * @date 2020-12-29
 */
@Data
public class UserLoginRecordPO extends BasePO {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long              id;

    /**
     * 登录人
     */
    private String            loginName;

    /**
     * 登录时间
     */
    private Date              loginTime;
}
