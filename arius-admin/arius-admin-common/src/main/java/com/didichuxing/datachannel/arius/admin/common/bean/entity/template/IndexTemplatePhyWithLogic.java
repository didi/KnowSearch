package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexTemplatePhyWithLogic extends IndexTemplatePhy {

    /**
     * 逻辑模板信息
     */
    private IndexTemplate logicTemplate;

    public boolean hasLogic() {
        return getLogicTemplate() != null;
    }

}
