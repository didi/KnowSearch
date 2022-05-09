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
public class IndexTemplatePhyInfoWithLogic extends IndexTemplatePhyInfo {

    /**
     * 逻辑模板信息
     */
    private IndexTemplateInfo logicTemplate;

    public boolean hasLogic() {
        return getLogicTemplate() != null;
    }

}
