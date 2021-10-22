package com.didichuxing.datachannel.arius.admin.common.bean.entity.app;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.StringResponsible;
import lombok.Data;

import java.util.Date;

/**
 * .
 * @author wangshu
 * @date 2020/09/19
 */
@Data
public class AppLogicClusterAuth extends BaseEntity implements StringResponsible {
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
    private String  logicClusterId;

    /**
     * 权限类型  读写  读
     * @see com.didichuxing.datachannel.arius.admin.client.constant.app.AppLogicClusterAuthEnum
     */
    private Integer type;

    /**
     * 责任人列表
     */
    private String  responsible;

    /**
     * 创建时间
     */
    private Date    createTime;

    /**
     * 更新时间
     */
    private Date    updateTime;
}
