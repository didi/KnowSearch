package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Label;

import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
public class IndexTemplateLogicWithLabels extends IndexTemplateLogic {

    private List<Label> labels;

}
