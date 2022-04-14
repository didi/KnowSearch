package com.didichuxing.datachannel.arius.admin.common.bean.entity.login;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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