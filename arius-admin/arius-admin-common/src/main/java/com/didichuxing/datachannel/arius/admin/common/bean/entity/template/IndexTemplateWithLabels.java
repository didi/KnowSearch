package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Label;

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
public class IndexTemplateWithLabels extends IndexTemplate {

    private List<Label> labels;

}
