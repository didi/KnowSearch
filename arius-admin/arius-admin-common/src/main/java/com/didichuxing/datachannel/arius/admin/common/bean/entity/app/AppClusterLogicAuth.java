package com.didichuxing.datachannel.arius.admin.common.bean.entity.app;

import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.StringResponsible;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppClusterLogicAuth extends BaseEntity implements StringResponsible {
    /**
     * ID
     */
    private Long    id;

    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 逻辑集群ID
     */
    private Long logicClusterId;

    /**
     * 权限类型  管理、访问、无权限
     * @see ProjectClusterLogicAuthEnum
     */
    private Integer type;

    /**
     * 责任人列表
     */
    private String  responsible;
}