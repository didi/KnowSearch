package com.didichuxing.datachannel.arius.admin.common.bean.po.arius;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUserStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AriusUserInfoPO extends BasePO {
    /**
     * 主键
     */
    private Long    id;

    /**
     * 用户名
     */
    private String  name;

    /**
     * 密码
     */
    private String  password;

    /**
     * EP域账号
     */
    private String  domainAccount;

    /**
     * 邮箱
     */
    private String  email;

    /**
     * 手机号
     */
    private String  mobile;

    /**
     * 状态
     * @see AriusUserStatusEnum
     */
    private Integer status;

    /**
     * 权限
     */
    private Integer role;
}
