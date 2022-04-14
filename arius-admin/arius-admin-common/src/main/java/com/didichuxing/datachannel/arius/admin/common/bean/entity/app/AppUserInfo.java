package com.didichuxing.datachannel.arius.admin.common.bean.entity.app;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author d06679
 * @date 2019/5/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserInfo extends BaseEntity {

    private Long    id;

    private Integer appId;

    private String  userName;

    private Date    lastLoginTime;

    private Integer loginCount;

}
