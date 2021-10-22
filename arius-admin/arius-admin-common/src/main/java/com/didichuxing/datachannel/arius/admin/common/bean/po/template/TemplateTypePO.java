package com.didichuxing.datachannel.arius.admin.common.bean.po.template;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;

import lombok.Data;

/**
 * 
 * @author d06679
 * @date 2019-07-24
 */
@Data
public class TemplateTypePO extends BasePO {

    private Integer id;

    private String  name;

    private String  idField;

    private Boolean source;

    private String  routing;

    private String  indexTemplateName;

    private Integer indexTemplateId;

}
