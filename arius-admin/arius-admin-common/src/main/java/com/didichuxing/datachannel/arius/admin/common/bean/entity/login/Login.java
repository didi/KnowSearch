package com.didichuxing.datachannel.arius.admin.common.bean.entity.login;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.Data;

@Data
public class Login extends BaseEntity {

    /**
     * 账号
     */
    private String domainAccount;

    /**
     * 密码
     */
    private String password;
}