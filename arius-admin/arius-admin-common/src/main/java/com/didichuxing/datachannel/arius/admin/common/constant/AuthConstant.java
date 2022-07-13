package com.didichuxing.datachannel.arius.admin.common.constant;

import lombok.NoArgsConstructor;

/**
 * 权限相关
 *
 * @author shizeying
 * @date 2022/05/27
 * @since 0.3.0
 */
@NoArgsConstructor
public final class AuthConstant {
    /**
     * 超级项目
     */
    public static final Integer SUPER_PROJECT_ID                     = 1;

    /**
     * 管理角色id
     */
    public static final Integer ADMIN_ROLE_ID                        = 1;
    /**
     * 资源own role 角色id
     */
    public static final Integer RESOURCE_OWN_ROLE_ID                 = 2;
    public static final String  GATEWAY_GET_PROJECT_TICKET           = "xTc59aY72";
    public static final String  GATEWAY_GET_PROJECT_TICKET_NAME      = "X-ARIUS-GATEWAY-TICKET";
    public static final String  GET_USER_PROJECT_ID_LIST_TICKET      = "xTc59aY72";
    public static final String  GET_USER_PROJECT_ID_LIST_TICKET_NAME = "X-ARIUS-APP-TICKET";
}