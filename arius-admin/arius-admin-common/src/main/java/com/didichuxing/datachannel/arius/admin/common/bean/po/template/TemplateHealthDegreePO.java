package com.didichuxing.datachannel.arius.admin.common.bean.po.template;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateHealthDegreePO extends BaseESPO {


    /**
     * 索引模板id
     */
    private Integer logicTemplateId;

    /**
     * 健康分
     */
    private Integer healthDegree;


    /**
     * 标签
     */
    List<TemplateLabelPO> labelPOS;

    /**
     * 获取主键key
     *
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public String getKey() {
        return String.valueOf(logicTemplateId);
    }

    @Override
    public String getRoutingValue() {
        return null;
    }
}
