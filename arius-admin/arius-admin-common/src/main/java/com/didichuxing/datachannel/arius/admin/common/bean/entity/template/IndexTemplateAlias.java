package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-07-24
 */
@Data
public class IndexTemplateAlias extends BaseEntity {

    private int     id;

    private Integer logicId;

    private String  name;
}
