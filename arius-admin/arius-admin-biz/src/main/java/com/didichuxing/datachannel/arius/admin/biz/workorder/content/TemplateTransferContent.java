package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板转让
 * @author d06679
 * @date 2019/5/7
 */
@Data
@NoArgsConstructor
public class TemplateTransferContent extends BaseContent {

    private Integer id;

    /**
     * 名字
     */
    private String  name;

    /**
     * 目标APPID
     */
    private Integer sourceProjectId;

    /**
     * 目标APPID
     */
    private Integer tgtProjectId;

    /**
     * 目标责任人
     */
    private String  tgtResponsible;

}