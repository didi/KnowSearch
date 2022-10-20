package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLabelPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateHealthDegreeRecord {

    /**
     * 统计时间
     */
    private Long          timestamp;

    /**
     * 索引模板id
     */
    private Integer       logicTemplateId;

    /**
     * 健康分
     */
    private Integer       healthDegree;

    /**
     * 标签
     */
    List<TemplateLabelPO> labelPOS;
}
