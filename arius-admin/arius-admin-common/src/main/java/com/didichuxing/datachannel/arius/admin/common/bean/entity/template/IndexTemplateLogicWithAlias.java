package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import java.util.List;

import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
public class IndexTemplateLogicWithAlias extends IndexTemplateLogic {

    private List<IndexTemplateAlias> aliases;

}
