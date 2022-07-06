package com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv;

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
public class UnavailableTemplateSrvVO extends TemplateSrvVO {

    /**
     * 不可用原因
     */
    private String unavailableReason;
}
