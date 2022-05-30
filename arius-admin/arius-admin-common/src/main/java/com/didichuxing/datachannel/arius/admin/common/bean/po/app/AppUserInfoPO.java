package com.didichuxing.datachannel.arius.admin.common.bean.po.app;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;

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
@Deprecated
public class AppUserInfoPO extends BasePO {

    private Long    id;

    private Integer appId;

    private String  userName;

    private Date    lastLoginTime;

    private Integer loginCount;

}