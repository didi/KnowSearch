package com.didichuxing.datachannel.arius.admin.common.bean.entity.login;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.Data;

/**
 * @author linyunan
 * @date 2021-04-21
 */
@Data
public class UserLoginRecord extends BaseEntity {
    /**
     * 主键
     */
    private Long   id;

    /**
     * 登录人
     */
    private String loginName;

    /**
     * 登录时间
     */
    private Date   loginTime;
}
