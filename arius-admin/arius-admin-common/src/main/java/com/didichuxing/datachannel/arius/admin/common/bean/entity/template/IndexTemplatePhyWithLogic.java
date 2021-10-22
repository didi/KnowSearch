package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
public class IndexTemplatePhyWithLogic extends IndexTemplatePhy {

    /**
     * 逻辑模板信息
     */
    private IndexTemplateLogic logicTemplate;

    public boolean hasLogic() {
        return getLogicTemplate() != null;
    }

}
