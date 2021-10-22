package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;

/**
 * 模板转让
 * @author d06679
 * @date 2019/5/7
 */
@Data
public class TemplateTransferContent extends BaseContent {

    private Integer id;

    /**
     * 名字
     */
    private String  name;

    /**
     * 目标APPID
     */
    private Integer sourceAppId;

    /**
     * 目标APPID
     */
    private Integer tgtAppId;

    /**
     * 目标责任人
     */
    private String  tgtResponsible;

}
