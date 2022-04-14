package com.didichuxing.datachannel.arius.admin.common.bean.po.app;

import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.DigitResponsible;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/4/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppTemplateAuthPO extends BasePO implements DigitResponsible {

    /**
     * 主键
     */
    private Long id;

    /**
     * app id
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
     * 责任人列表，id列表，英文逗号分隔
     */
    private String responsible;

}
