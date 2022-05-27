package com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/5/19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColdSrvOpenDTO extends BaseTemplateSrvOpenDTO {

    /**
     * 冷节点保存天数
     */
    private Integer coldSaveDays;
}
