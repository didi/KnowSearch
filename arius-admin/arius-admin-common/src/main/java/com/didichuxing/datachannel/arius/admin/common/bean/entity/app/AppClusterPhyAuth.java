package com.didichuxing.datachannel.arius.admin.common.bean.entity.app;

import com.didichuxing.datachannel.arius.admin.client.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.StringResponsible;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppClusterPhyAuth extends BaseEntity implements StringResponsible {
    /**
     * ID
     */
    private Long    id;

    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 物理集群名称
     */
    private String  clusterPhyName;

    /**
     * 权限类型  读写  读
     * @see AppClusterLogicAuthEnum
     */
    private Integer type;

    /**
     * 责任人列表
     */
    private String  responsible;
}
