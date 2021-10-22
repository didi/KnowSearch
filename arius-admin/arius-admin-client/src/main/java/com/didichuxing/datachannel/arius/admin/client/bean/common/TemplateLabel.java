package com.didichuxing.datachannel.arius.admin.client.bean.common;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/5/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateLabel {

    private Integer     indexTemplateId;

    private List<Label> labels;

}
