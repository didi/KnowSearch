package com.didichuxing.datachannel.arius.admin.common.bean.entity.app;

import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.StringResponsible;

import lombok.Data;

/**
 * @author d06679
 * @date 2019/4/16
 */
@Data
public class AppTemplateAuth extends BaseEntity implements StringResponsible {

    /**
     * 主键
     */
    private Long id;

    /**
     * APP ID
     */
    private Integer appId;

    /**
     * 模板ID
     */
    private Integer templateId;

    /**
     * 权限类型  读写  读
     * @see AppTemplateAuthEnum
     */
    private Integer type;

    /**
     * 责任人列表
     */
    private String responsible;

}
