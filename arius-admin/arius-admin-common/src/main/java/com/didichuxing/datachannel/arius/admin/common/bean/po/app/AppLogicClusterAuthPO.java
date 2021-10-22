package com.didichuxing.datachannel.arius.admin.common.bean.po.app;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.DigitResponsible;
import lombok.Data;

/**
 * 逻辑集群权限PO
 * @author wangshu
 * @date 2020/09/17
 */
@Data
public class AppLogicClusterAuthPO extends BasePO implements DigitResponsible {
    /**
     * 主键
     */
    private Long id;

    /**
     * app id
     */
    private Integer appId;

    /**
     * 逻辑集群ID
     */
    private Long logicClusterId;

    /**
     * 权限类型
     * @see com.didichuxing.datachannel.arius.admin.client.constant.app.AppLogicClusterAuthEnum
     */
    private Integer type;

    /**
     * 责任人列表，id列表，英文逗号分隔
     */
    private String  responsible;
}
